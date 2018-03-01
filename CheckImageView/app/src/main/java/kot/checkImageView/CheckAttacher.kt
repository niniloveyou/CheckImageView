package kot.checkImageView

import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import java.lang.ref.WeakReference

/**
 * @author deadline
 * @time 2017/10/13
 */
open class CheckAttacher(imageView: ImageView) : ICheckAttacher {

    private var mScale: Float = 0.toFloat()
    private var mCheckedScale: Float = 0.toFloat()
    private var mUnCheckedScale: Float = 0.toFloat()
    private var mChecked = false
    private var mZoomDuration = 200L
    private var mMatrix = Matrix()
    private var mDisplayRect = RectF()
    private var mInterpolator = AccelerateDecelerateInterpolator()
    private var mListener: OnCheckedChangeListener? = null
    private var weakImageView: WeakReference<ImageView>? = null
    private var mMatrixValues = FloatArray(9)
    /*constructor(imageView: ImageView, scale: Float) : this(imageView){
        thils.mScale = scale
    }*/

    init {
        mZoomDuration = ZOOM_DURATION
        mCheckedScale = DEFAULT_CHECKED_SCALE
        mUnCheckedScale = DEFAULT_UNCHECKED_SCALE
        weakImageView = WeakReference<ImageView>(imageView)
    }

    override fun isChecked(): Boolean {

        return mChecked
    }

    override fun setChecked(checked: Boolean) {
        if (mChecked != checked) {
            setScale(if (checked) mCheckedScale else mUnCheckedScale, true)
        }
    }

    override fun setCheckedScale(scale: Float) {
        if (scale < 1f || scale < mUnCheckedScale) {
            throw IllegalArgumentException("checked scale can not small than unchecked scale!")
        }

        this.mCheckedScale = scale
    }

    override fun getScale(): Float {
        return mScale
    }

    override fun setZoomAnimationDuration(duration: Long) {
        this.mZoomDuration = duration
    }

    override fun setOnCheckChangeListener(listener: OnCheckedChangeListener) {
        this.mListener = listener
    }

    fun setScale(scale: Float, animation: Boolean){
        var imageView = getImageView()
        imageView ?: return
        setScale(scale, imageView.right / 2f, imageView.bottom / 2f, animation)
    }

    fun setScale(scale: Float, focalX: Float, focalY: Float, animation: Boolean) {
        var imageView: ImageView? = getImageView()
        if (null != imageView){
            imageView.post { AnimatedZoomRunnable(mScale, scale, focalX, focalY) }
        }
    }

    private fun getImageView(): ImageView? {
        var imageView: ImageView? = null
        if(null != weakImageView) {
            imageView = weakImageView?.get()
        }

        if (null == imageView) {
            weakImageView == null
        }

        return imageView
    }

    private fun getMatrixScale(): Float {
        return Math.sqrt(Math.pow(getMatrixValue(mMatrix, Matrix.MSCALE_X).toDouble(),
                2.00)).toFloat() + Math.pow(getMatrixValue(mMatrix, Matrix.MSKEW_Y).toDouble(), 2.toDouble()).toFloat()
    }

    private fun getMatrixValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    private fun onScale(deltaScale: Float, focalX: Float, focalY: Float){
        mMatrix.postScale(deltaScale, deltaScale, focalX, focalY)
        var imageView = getImageView()
        if(imageView != null && update()){
            imageView.imageMatrix = mMatrix
        }
    }

    fun update(): Boolean {
        var imageView = getImageView()
        imageView ?: return false
        var rect = getDisplayRect()
        rect ?: return false

        var viewHeight = imageView.measuredHeight - imageView.paddingTop - imageView.paddingBottom
        var viewWidth = imageView.measuredWidth - imageView.paddingLeft - imageView.paddingRight

        var drawableHeight = rect.height()
        var drawableWidth = rect.width()
        var deltaX = 0.toFloat()
        var deltaY = 0.toFloat()

        deltaY = (viewHeight - drawableHeight) / 2f - rect.top
        deltaX = (viewWidth - drawableWidth) / 2f - rect.left
        mMatrix.postTranslate(deltaX, deltaY)
        return true
    }

    fun moveToCenter(){
        var imageView = getImageView()
        imageView ?: return
        update()
        imageView.imageMatrix = mMatrix
    }

    private fun getDisplayRect(): RectF? {
        var imageView = getImageView()
        imageView ?: return null
        var drawable = imageView.drawable
        drawable ?: return null
        mDisplayRect.set(0.toFloat(), 0.toFloat(), drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        return mDisplayRect
    }

    fun release() {
        weakImageView = null
    }


    inner class AnimatedZoomRunnable(val mZoomStart: Float,
                                     val mZoomEnd: Float,
                                     val mFocalX: Float,
                                     val mFocalY: Float) : Runnable {

        private var mStartTime: Long = System.currentTimeMillis()
        val SIXTY_FPS_INTERVAL = 1000 / 60

        override fun run() {
            var imageView = getImageView()
            imageView ?: return
            var t: Float = interpolate()
            var scale: Float = mZoomStart + t * (mZoomEnd - mZoomStart)
            var deltaScale = scale / getMatrixScale()
            onScale(deltaScale, mFocalX, mFocalY)

            if(t < 1f){
                postOnAnimation(imageView, this)
            }else {
                mChecked = !mChecked
                mScale = if (mChecked) mCheckedScale else mUnCheckedScale
                mListener?.onChange(mChecked)
            }
        }

        private fun interpolate(): Float {
            var t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration
            t = Math.min(1f, t)
            t = mInterpolator.getInterpolation(t)
            return t
        }

        private fun postOnAnimation(view: View, runnable: Runnable){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.postOnAnimation(runnable)
            } else {
                view.postDelayed(runnable, SIXTY_FPS_INTERVAL.toLong())
            }
        }
    }
}