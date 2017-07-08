package deadline.checkImageView;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

/**
 * @author deadline
 * 辅助类，方便移植功能
 */

public class CheckAttacher implements ICheckAttacher {

    private float mScale;
    private float mCheckedScale;
    private float mUnCheckedScale;
    private boolean mChecked;
    private long mZoomDuration;
    private Matrix mMatrix;
    private RectF mDisplayRect = new RectF();
    private float[] mMatrixValues = new float[9];
    private WeakReference<ImageView> weakImageView;
    private OnCheckedChangeListener mListener;
    private Interpolator mInterpolator;

    public CheckAttacher(ImageView imageView){
        mMatrix = new Matrix();
        mZoomDuration = ZOOM_DURATION;
        mScale = DEFAULT_UNCHECKED_SCALE;
        mCheckedScale = DEFAULT_CHECKED_SCALE;
        mUnCheckedScale = DEFAULT_UNCHECKED_SCALE;
        mInterpolator = new AccelerateDecelerateInterpolator();
        weakImageView = new WeakReference<ImageView>(imageView);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if(mChecked != checked){
            setScale(checked ? mCheckedScale : mUnCheckedScale, true);
        }
    }

    @Override
    public void setCheckedScale(float scale) {
        if(scale < 1f || scale < mUnCheckedScale){
            throw new IllegalArgumentException("checked scale can not small than unchecked scale!");
        }

        this.mCheckedScale = scale;
    }

    @Override
    public float getScale() {
        return mScale;
    }

    private void setScale(float scale, boolean animate) {
        ImageView imageView = getImageView();
        if(imageView != null) {
            setScale(scale,
                    imageView.getRight() / 2,
                    imageView.getBottom() / 2,
                    animate);
        }
    }

    private void setScale(float scale, float focalX, float focalY, boolean animate) {
        ImageView imageView = getImageView();

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

    private ImageView getImageView(){

        ImageView imageView = null;

        if(null != weakImageView){
            imageView = weakImageView.get();
        }

        if(null == imageView){
            weakImageView = null;
        }

        return imageView;
    }

    private void onScale(float deltaScale, float focalX, float focalY){
        mMatrix.postScale(deltaScale, deltaScale, focalX, focalY);
        ImageView imageView = getImageView();
        if(imageView != null && update()){
            imageView.setImageMatrix(mMatrix);
        }
    }

    public boolean update(){
        final ImageView imageView = getImageView();
        if(imageView == null){
            return false;
        }

        final RectF rect = getDisplayRect();
        if (rect == null) {
            return false;
        }

        int viewHeight = imageView.getMeasuredHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
        int viewWidth = imageView.getMeasuredWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();

        float drawableHeight = rect.height();
        float drawableWidth = rect.width();

        float deltaX = 0, deltaY = 0;

        deltaY = (viewHeight - drawableHeight) / 2f - rect.top;
        deltaX = (viewWidth - drawableWidth) / 2f - rect.left;
        mMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    public void moveToCenter(){
        final ImageView imageView = getImageView();
        if(imageView == null){
            return;
        }
        update();
        imageView.setImageMatrix(mMatrix);
    }

    private RectF getDisplayRect() {
        ImageView imageView = getImageView();

        if (null != imageView) {
            Drawable d = imageView.getDrawable();
            if (null != d) {
                mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                        d.getIntrinsicHeight());
                mMatrix.mapRect(mDisplayRect);
                return mDisplayRect;
            }
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

    public void release(){
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
            ImageView imageView = getImageView();
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
            }else{
                mChecked = !mChecked;
                mScale = mChecked ? mCheckedScale : mUnCheckedScale;
                if(mListener != null) {
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

        private void postOnAnimation(View view, Runnable runnable){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.postOnAnimation(runnable);
            } else {
                view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
            }
        }
    }
}
