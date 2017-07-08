package deadline.checkImageView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

/**
 * @author deadline
 */

public class FrescoCheckImageView extends SimpleDraweeView implements ICheckAttacher{

    private FrescoCheckAttacher mAttacher;

    private boolean mEnableDraweeMatrix = true;

    public FrescoCheckImageView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        setup();
    }

    public FrescoCheckImageView(Context context) {
        super(context);
        setup();
    }

    public FrescoCheckImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public FrescoCheckImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    public FrescoCheckImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }


    private void setup() {
        getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        mAttacher = new FrescoCheckAttacher(this);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        if (mEnableDraweeMatrix) {
            canvas.concat(mAttacher.getDrawMatrix());
        }
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public boolean isChecked() {
        return mAttacher.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        mAttacher.setChecked(checked);
    }

    @Override
    public void setCheckedScale(float scale) {
        mAttacher.setCheckedScale(scale);
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public void setZoomAnimationDuration(long duration) {
        mAttacher.setZoomAnimationDuration(duration);
    }

    @Override
    public void setOnCheckChangeListener(CheckAttacher.OnCheckedChangeListener listener) {
        mAttacher.setOnCheckChangeListener(listener);
    }


    public void setPhotoUri(Uri uri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setCallerContext(null)
                .setUri(uri)
                .setOldController(getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override public void onFailure(String id, Throwable throwable) {
                        super.onFailure(id, throwable);
                        mEnableDraweeMatrix = false;
                    }

                    @Override public void onFinalImageSet(String id, ImageInfo imageInfo,
                                                          Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        mEnableDraweeMatrix = true;
                        if(imageInfo != null) {
                            mAttacher.moveToCenter(imageInfo.getWidth(), imageInfo.getHeight());
                        }
                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {
                        super.onIntermediateImageFailed(id, throwable);
                        mEnableDraweeMatrix = false;
                    }

                    @Override public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        super.onIntermediateImageSet(id, imageInfo);
                        mEnableDraweeMatrix = true;
                        if(imageInfo != null) {
                            mAttacher.moveToCenter(imageInfo.getWidth(), imageInfo.getHeight());
                        }
                    }
                })
                .build();
        setController(controller);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttacher.release();
    }
}
