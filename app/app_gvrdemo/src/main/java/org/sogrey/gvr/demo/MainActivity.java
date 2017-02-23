package org.sogrey.gvr.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * 一、初识GVR （Google VR for Android） http://www.jianshu.com/p/09c0822b9d1e
 * 二、制作VR全景图播放器 （Google VR for Android） http://www.jianshu.com/p/104251a3153d
 * 三、制作VR视频播放器 （Google VR for Android） http://www.jianshu.com/p/82163453ed30
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * GVR 全景图
     *
     * @param v
     */
    public void GVRPano(View v) {
        startActivity(new Intent(this, SimpleVrPanoramaActivity.class));
    }

    /**
     * GVR 全景视频
     *
     * @param v
     */
    public void GVRVideo(View v) {
        startActivity(new Intent(this, SimpleVrVideoActivity.class));
    }
}
