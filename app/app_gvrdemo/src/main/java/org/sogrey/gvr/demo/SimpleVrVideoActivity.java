package org.sogrey.gvr.demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

/**
 * 一、初识GVR （Google VR for Android） http://www.jianshu.com/p/09c0822b9d1e
 * 二、制作VR全景图播放器 （Google VR for Android） http://www.jianshu.com/p/104251a3153d
 * 三、制作VR视频播放器 （Google VR for Android） http://www.jianshu.com/p/82163453ed30
 */
public class SimpleVrVideoActivity extends Activity {
    private static final String TAG = "SimpleVrVideoActivity";
    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    private String fileUri = "testRoom1_1080Stereo.mp4";//assets文件夹下的文件名
    private VideoLoaderTask backgroundVideoLoaderTask;//异步加载视频

    private VrVideoView videoWidgetView;//Google提供给我们现实播放VR视频的View
    private SeekBar seekBar;//进度条
    private TextView statusText;

    private boolean isPaused = false;//标记是否暂停

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_vr_video);

        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);//初始化VrVideoView
        videoWidgetView.setEventListener(new ActivityEventListener());//为VrVideoView添加监听
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());
        statusText = (TextView) findViewById(R.id.status_text);

        //如果有任务在执行则停止它
        if (backgroundVideoLoaderTask != null) {
            backgroundVideoLoaderTask.cancel(true);
        }
        //创建一个任务
        backgroundVideoLoaderTask = new VideoLoaderTask();
        //执行任务。将视频文件名（根据项目实际情况传吧）传入
        backgroundVideoLoaderTask.execute(fileUri);
    }


    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();//播放
        } else {
            videoWidgetView.pauseVideo();//暂停
        }
        isPaused = !isPaused;
    }


    private class ActivityEventListener extends VrVideoEventListener {

        @Override
        public void onLoadSuccess() {//加载成功
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekBar.setMax((int) videoWidgetView.getDuration());
            updateStatusText();
        }

        @Override
        public void onLoadError(String errorMessage) {//加载失败
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {//当我们点击了VrVideoView时候触发
            togglePause();
        }

        @Override
        public void onNewFrame() {//一个新的帧被绘制到屏幕上。
            updateStatusText();
            seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
        }

        @Override
        public void onCompletion() {//视频播放完毕。
            videoWidgetView.seekTo(0);//移动到视频开始
        }
    }

    class VideoLoaderTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... uri) {
            try {
                videoWidgetView.loadVideoFromAsset(uri[0]);//加载视频文件
            } catch (IOException e) {//视频文件打开失败
                Log.e(TAG, "Could not open video: " + e);
            }
            return true;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);
        seekBar.setMax((int) savedInstanceState.getLong(STATE_VIDEO_DURATION));
        seekBar.setProgress((int) progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoWidgetView.pauseRendering();//暂停3D渲染和跟踪
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoWidgetView.resumeRendering();//恢复3D渲染和跟踪，但官方文档上面没有写
    }

    @Override
    protected void onDestroy() {
        videoWidgetView.shutdown();//关闭渲染并释放相关的内存
        super.onDestroy();
    }


    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        status.append(isPaused ? "Paused: " : "Playing: ");
        status.append(String.format("%.2f", videoWidgetView.getCurrentPosition() / 1000f));
        status.append(" / ");
        status.append(videoWidgetView.getDuration() / 1000f);
        status.append(" seconds.");
        statusText.setText(status.toString());
    }

    /**
     * When the user manipulates the seek bar, update the video position.
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoWidgetView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}