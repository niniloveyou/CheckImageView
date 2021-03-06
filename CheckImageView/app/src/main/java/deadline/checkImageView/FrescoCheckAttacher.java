package deadline.checkImageView;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.DraweeView;

import java.lang.ref.WeakReference;

/**
 * @author deadline
 * 辅助类，方便移植功能
 */

public class FrescoCheckAttacher implements ICheckAttacher {

    private float mScale;
    private float mCheckedScale;
    private float mUnCheckedScale;
    private boolean mChecked;
    private long mZoomDuration;
    private Matrix mMatrix;
    private RectF mDisplayRect = new RectF();
    private float[] mMatrixValues = new float[9];
    private WeakReference<DraweeView<GenericDraweeHierarchy>> weakImageView;
    private OnCheckedChangeListener mListener;
    private Interpolator mInterpolator;
    private int mImageInfoHeight = -1, mImageInfoWidth = -1;

    public FrescoCheckAttacher(DraweeView<GenericDraweeHierarchy> imageView) {
        mMatrix = new Matrix();
        mZoomDuration = ZOOM_DURATION;
        mScale = DEFAULT_UNCHECKED_SCALE;
        mCheckedScale = DEFAULT_CHECKED_SCALE;
        mUnCheckedScale = DEFAULT_UNCHECKED_SCALE;
        mInterpolator = new AccelerateDecelerateInterpolator();
        weakImageView = new WeakReference<DraweeView<GenericDraweeHierarchy>>(imageView);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            setScale(checked ? mCheckedScale : mUnCheckedScale, true);
        }
    }

    @Override
    public void setCheckedScale(float scale) {
        if (scale < 1f || scale < mUnCheckedScale) {
            throw new IllegalArgumentException("checked scale can not small than unchecked scale!");
        }

        this.mCheckedScale = scale;
    }

    @Override
    public float getScale() {
        return mScale;
    }

    private void setScale(float scale, boolean animate) {
        DraweeView<GenericDraweeHierarchy> imageView = getImageView();
        if (imageView != null) {
            setScale(scale,
                    imageView.getRight() / 2,
                    imageView.getBottom() / 2,
                    animate);
        }
    }

    private void setScale(float scale, float focalX, float focalY, boolean animate) {
        DraweeView<GenericDraweeHierarchy> imageView = getImageView();

        if (null != imageView) {
            imageView.post(new AnimatedZoomRunnable(mScale, scale, focalX, focalY));
        }
    }

    @Override
    public void setZoomAnimationDuration(long duration) {
        this.mZoomDuration = duration;
    }

    @Override
    public void setOnCheckChangeListener(OnCheckedChangeListener listener) {
        this.mListener = listener;
    }

    private DraweeView<GenericDraweeHierarchy> getImageView() {

        DraweeView<GenericDraweeHierarchy> imageView = null;

        if (null != weakImageView) {
            imageView = weakImageView.get();
        }

        if (null == imageView) {
            weakImageView = null;
        }

        return imageView;
    }

    public Matrix getDrawMatrix() {
        return mMatrix;
    }

    private void onScale(float deltaScale, float focalX, float focalY) {
        mMatrix.postScale(deltaScale, deltaScale, focalX, focalY);
        update();
    }

    public void moveToCenter(int imageInfoHeight, int imageInfoWidth){
        final DraweeView<GenericDraweeHierarchy> imageView = getImageView();
        if(imageView == null){
            return;
        }
        this.mImageInfoWidth = imageInfoWidth;
        this.mImageInfoHeight = imageInfoHeight;
        update();
    }

    public boolean update() {
        final DraweeView<GenericDraweeHierarchy> imageView = getImageView();
        if (imageView == null) {
            return false;
        }

        final RectF rect = getDisplayRect();
        if (rect == null) {
            return false;
        }

        int viewHeight = imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
        int viewWidth = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();

        float drawableHeight = rect.height();
        float drawableWidth = rect.width();

        float deltaX = 0, deltaY = 0;

        deltaY = (viewHeight - drawableHeight) / 2f - rect.top;
        deltaX = (viewWidth - drawableWidth) / 2f - rect.left;
        mMatrix.postTranslate(deltaX, deltaY);
        imageView.invalidate();
        return true;
    }

    private RectF getDisplayRect() {
        DraweeView<GenericDraweeHierarchy> imageView = getImageView();

        if (null != imageView) {
            mDisplayRect.set(0.0F, 0.0F, mImageInfoWidth, mImageInfoHeight);
            imageView.getHierarchy().getActualImageBounds(mDisplayRect);
            mMatrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    private float getMatrixScale() {
        return (float) Math.sqrt((float) Math.pow(getMatrixValue(mMatrix, Matrix.MSCALE_X), 2)
                + (float) Math.pow(getMatrixValue(mMatrix, Matrix.MSKEW_Y), 2));
    }

    private float getMatrixValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    public void release() {
        weakImageView = null;
    }

    private class AnimatedZoomRunnable implements Runnable {

        private static final int SIXTY_FPS_INTERVAL = 1000 / 60;
        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            DraweeView<GenericDraweeHierarchy> imageView = getImageView();
            if (imageView == null) {
                return;
            }

            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getMatrixScale();

            onScale(deltaScale, mFocalX, mFocalY);

            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                postOnAnimation(imageView, this);
            } else {
                mChecked = !mChecked;
                mScale = mChecked ? mCheckedScale : mUnCheckedScale;
                if (mListener != null) {
                    mListener.onChange(mChecked);
                }
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }

        private void postOnAnimation(View view, Runnable runnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.postOnAnimation(runnable);
            } else {
                view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
            }
        }
    }
}

