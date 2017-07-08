package deadline;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import deadline.checkImageView.CheckImageView;
import deadline.checkImageView.FrescoCheckImageView;

public class MainActivity extends AppCompatActivity {

    CheckImageView checkImageView;
    FrescoCheckImageView frescoCheckImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ImageView
        checkImageView = (CheckImageView) findViewById(R.id.checkImageView);
        checkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkImageView.setChecked(!checkImageView.isChecked());
            }
        });

        //SimpleDraweeView
        frescoCheckImageView = (FrescoCheckImageView) findViewById(R.id.frescoImageView);
        frescoCheckImageView.setPhotoUri(Uri.parse("http://img.pconline.com.cn/images/upload/upc/tx/photoblog/1303/28/c2/19305988_19305988_1364441924437.jpg"));
        frescoCheckImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frescoCheckImageView.setChecked(!frescoCheckImageView.isChecked());
            }
        });
    }
}
