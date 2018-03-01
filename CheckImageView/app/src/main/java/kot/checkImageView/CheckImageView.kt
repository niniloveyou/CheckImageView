package kot.checkImageView

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.annotation.Nullable
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @author deadline
 * @time 2017/10/14
 */
class CheckImageView : ImageView, ICheckAttacher {

    private var mAttacher: CheckAttacher = CheckAttacher(this)

    constructor(context: Context?) : super(context, null)

    constructor(context: Context?, @Nullable attrs: AttributeSet) : super(context, attrs, 0)

    /* command + option + / 注释 */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
    }

    override fun isChecked(): Boolean {
        return mAttacher.isChecked()
    }

    override fun setChecked(checked: Boolean) {
        mAttacher.setChecked(checked)
    }

    override fun setCheckedScale(scale: Float) {
        mAttacher.setCheckedScale(scale)
    }

    override fun getScale(): Float {
        return mAttacher.getScale()
    }

    override fun setZoomAnimationDuration(duration: Long) {
        mAttacher.setZoomAnimationDuration(duration)
    }

    override fun setOnCheckChangeListener(listener: OnCheckedChangeListener) {
        mAttacher.setOnCheckChangeListener(listener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mAttacher.moveToCenter()
    }

    override // setImageBitmap calls through to this method
    fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        mAttacher.moveToCenter()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        mAttacher.moveToCenter()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        mAttacher.moveToCenter()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        mAttacher.moveToCenter()
        return changed
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAttacher.release()
    }
}