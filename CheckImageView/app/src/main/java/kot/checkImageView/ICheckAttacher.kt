package kot.checkImageView

/**
 * @author deadline
 * @time 2017/10/13
 */

val DEFAULT_CHECKED_SCALE = 1.2f
val DEFAULT_UNCHECKED_SCALE = 1.0f
val ZOOM_DURATION = 300L

interface ICheckAttacher {

    fun isChecked(): Boolean

    fun setChecked(checked: Boolean)

    fun setCheckedScale(scale: Float)

    fun getScale(): Float

    fun setZoomAnimationDuration(duration: Long)

    fun setOnCheckChangeListener(listener: OnCheckedChangeListener)
}

interface OnCheckedChangeListener{

    fun onChange(checked: Boolean)
}