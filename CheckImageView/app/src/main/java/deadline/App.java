package deadline;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * @author deadline
 * @time 2017/7/8
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(getApplicationContext());
    }
}
