package org.sogrey.gvr.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一、初识GVR （Google VR for Android） http://www.jianshu.com/p/09c0822b9d1e
 * 二、制作VR全景图播放器 （Google VR for Android） http://www.jianshu.com/p/104251a3153d
 * 三、制作VR视频播放器 （Google VR for Android） http://www.jianshu.com/p/82163453ed30
 */
public class SimpleVrPanoramaActivity extends Activity {

    private static final String TAG = "VrPanorama";
    private VrPanoramaView panoWidgetView;//上面说的Google提供给我们现实全景图片的View
    private String fileUri = "testRoom1_2kStereo.jpg";//assets文件夹下的文件名

    private Options panoOptions = new Options();//VrPanoramaView需要的设置
    private ImageLoaderTask backgroundImageLoaderTask;//异步加载图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_vr_panorama);//布局上面贴了

        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);//初始化VrPanoramaView
        panoWidgetView.setEventListener(new ActivityEventListener());//为VrPanoramaView添加监听

        //如果有任务在执行则停止它
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        //设置inputType 为TYPE_STEREO_OVER_UNDER. 在后面会介绍TYPE_STEREO_OVER_UNDER的，暂时当做一个图片的显示类型就行
        panoOptions.inputType = Options.TYPE_STEREO_OVER_UNDER;
        //创建一个任务
        backgroundImageLoaderTask = new ImageLoaderTask();
        //执行任务。将图片名（根据项目实际情况传吧）和设置传入
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }
    //异步任务
    class ImageLoaderTask extends AsyncTask<Pair<String, Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<String, Options>... fileInformation) {//真正写项目根据情况添加条件判断吧

            InputStream istr = null;
            try {
                istr = getAssets().open(fileInformation[0].first);//获取图片的输入流
            } catch (IOException e) {
                Log.e(TAG, "Could not decode default bitmap: " + e);
                return false;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(istr);//创建bitmap
            panoWidgetView.loadImageFromBitmap(bitmap, fileInformation[0].second);//参数一为图片的bitmap，参数二为 VrPanoramaView 所需要的设置

            try {
                istr.close();//关闭InputStream
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }

    private class ActivityEventListener extends VrPanoramaEventListener {

        @Override
        public void onLoadSuccess() {//图片加载成功
            Log.e(TAG, "onLoadSuccess");
        }


        @Override
        public void onLoadError(String errorMessage) {//图片加载失败
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }

        @Override
        public void onClick() {//当我们点击了VrPanoramaView 时候出发
            super.onClick();
            Log.e(TAG, "onClick");
        }

        @Override
        public void onDisplayModeChanged(int newDisplayMode) {//改变显示模式时候出发（全屏模式和纸板模式）
            super.onDisplayModeChanged(newDisplayMode);
            Log.e(TAG, "onDisplayModeChanged");
        }
    }


    @Override
    protected void onPause() {
        panoWidgetView.pauseRendering();//暂停3D渲染和跟踪
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();//恢复3D渲染和跟踪
    }

    @Override
    protected void onDestroy() {
        panoWidgetView.shutdown();//关闭渲染下并释放相关的内存

        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);//停止异步任务
        }
        super.onDestroy();
    }
}
