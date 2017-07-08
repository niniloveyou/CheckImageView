package deadline.checkImageView;

/**
 * @author deadline
 */

public interface ICheckAttacher {

    float DEFAULT_CHECKED_SCALE = 1.2f;
    float DEFAULT_UNCHECKED_SCALE = 1.0f;
    long ZOOM_DURATION = 300L;


    interface OnCheckedChangeListener{

        void onChange(boolean checked);
    }


    boolean isChecked();

    void setChecked(boolean checked);

    void setCheckedScale(float scale);

    float getScale();

    void setZoomAnimationDuration(long duration);

    void setOnCheckChangeListener(OnCheckedChangeListener listener);
}
