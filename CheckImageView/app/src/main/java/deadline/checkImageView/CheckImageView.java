package deadline.checkImageView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author deadline
 */
public class CheckImageView extends ImageView implements ICheckAttacher{

    private CheckAttacher mAttacher;

    public CheckImageView(Context context) {
        this(context, null);
    }

    public CheckImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        setScaleType(ScaleType.MATRIX);
        mAttacher = new CheckAttacher(this);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mAttacher.moveToCenter();
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (null != mAttacher) {
            mAttacher.moveToCenter();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (null != mAttacher) {
            mAttacher.moveToCenter();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (null != mAttacher) {
            mAttacher.moveToCenter();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (null != mAttacher) {
            mAttacher.moveToCenter();
        }
        return changed;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttacher.release();
    }
}
