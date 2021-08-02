package com.example.mobilevideo.ui.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alivc.live.pusher.AlivcAudioAACProfileEnum;
import com.alivc.live.pusher.AlivcAudioChannelEnum;
import com.alivc.live.pusher.AlivcAudioSampleRateEnum;
import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStats;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcQualityModeEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.example.mobilevideo.R;

import java.util.List;

import floatwindowpermission.FloatWindowManager;
/*
* 1 新增3个参数变量 （ mRtmpUrl,mIndexOfSize ,mIndexOfOrientation)
* 2 创建3个参数设置的函数 setPushConfig_Size , setPushConfig_Orientation , setPushConfig_RtmpUrl
* 3 新增5个直播相关的对象
    AlivcLivePushConfig mAlivcLivePushConfig;   //配置
    AlivcLivePusher mAlivcLivePusher;           //推流
    AlivcLivePushInfoListener mOnPushInfo;      //推流状态监听器
    AlivcLivePushNetworkListener mOnPushNet;    //网络状态监听器
    AlivcLivePushErrorListener mOnPushError;    //异常监听器
  4 初始化 推流 函数 initScreenPusher
* */

public class TScreenLive {
    public final static String TAG = "TScreenLive";


    @SuppressLint("StaticFieldLeak")
    private static TScreenLive screenLive = null;

    public static TScreenLive getScreenLive() {
        if (screenLive == null) {
            screenLive = new TScreenLive();
        }
        return screenLive;
    }

    public boolean inited = false;
    public Activity mCtx;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mFloatToolbar_Param;
    private View mFlowWin;

    ImageButton mBtnStart;
    ImageButton mBtnPause;
    ImageButton mBtnResume;
    ImageButton mBtnStop;
    ImageButton mBtnClose;
    TextView mTxtStatus;

    private AlivcLivePushConfig mAlivcLivePushConfig;
    private AlivcLivePusher mAlivcLivePusher;
    private AlivcLivePushInfoListener mOnPushInfo;
    private AlivcLivePushNetworkListener mOnPushNet;
    private AlivcLivePushErrorListener mOnPushError;
    Handler mHandler;

    private int mIndexOfSize = 1;
    private int mIndexOfOrientation = 1;
    private String mRtmpUrl = "";


    @SuppressLint("HandlerLeak")
    public void init(Activity ctx) {
        try {
            mCtx = ctx;
            // windowManage
            mWindowManager = (WindowManager) mCtx.getSystemService(Context.WINDOW_SERVICE);
            // LayoutParam
            mFloatToolbar_Param = new WindowManager.LayoutParams();
            int aType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                aType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            //aType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            mFloatToolbar_Param.type = aType;
            mFloatToolbar_Param.format = PixelFormat.RGBA_8888;
            mFloatToolbar_Param.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            mFloatToolbar_Param.gravity = Gravity.CENTER | Gravity.TOP;
            mFloatToolbar_Param.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mFloatToolbar_Param.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mFloatToolbar_Param.x = 0;
            mFloatToolbar_Param.y = 0;
            //View
            mFlowWin = LayoutInflater.from(mCtx).inflate(R.layout.layout_screen_capture, null);
            mTxtStatus = mFlowWin.findViewById(R.id.txtScreenCaptureStatus);
            mBtnStart = mFlowWin.findViewById(R.id.btnScreenCaptureStart);
            mBtnStop = mFlowWin.findViewById(R.id.btnScreenCaptureStop);
            mBtnPause = mFlowWin.findViewById(R.id.btnScreenCapturePause);
            mBtnResume = mFlowWin.findViewById(R.id.btnScreenCaptureResume);
            mBtnClose = mFlowWin.findViewById(R.id.btnScreenCaptureClose);

            mBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acShowRequestCaptureScreen();
                }
            });
            mBtnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acStop();
                    acClose();
                }
            });
            mBtnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acPause();
                }
            });
            mBtnResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acResume();
                }
            });
            mBtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acStop();
                    acClose();
                }
            });

            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 1001) {
                        acRefresh();
                    }
                }
            };

            //Config 初始化直播配置资源
            mAlivcLivePushConfig = new AlivcLivePushConfig();
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_540P);
            mAlivcLivePushConfig.setQualityMode(AlivcQualityModeEnum.QM_RESOLUTION_FIRST);
            mAlivcLivePushConfig.setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT);//竖屏推流
            mAlivcLivePushConfig.setFps(AlivcFpsEnum.FPS_20); //帧率20
            mAlivcLivePushConfig.setAudioSamepleRate(AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_44100);
            mAlivcLivePushConfig.setAudioProfile(AlivcAudioAACProfileEnum.AAC_LC);
            mAlivcLivePushConfig.setAudioChannels(AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO);
            mAlivcLivePushConfig.setAudioOnly(false);
            //回调
            mOnPushError = new AlivcLivePushErrorListener() {
                @Override
                public void onSystemError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {
                    acStop();
                }

                @Override
                public void onSDKError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {

                }
            };
            mOnPushNet = new AlivcLivePushNetworkListener() {
                @Override
                public void onNetworkPoor(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onNetworkRecovery(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onReconnectStart(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onConnectionLost(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onReconnectFail(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onReconnectSucceed(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onSendDataTimeout(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onConnectFail(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public String onPushURLAuthenticationOverdue(AlivcLivePusher alivcLivePusher) {
                    return null;
                }

                @Override
                public void onSendMessage(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onPacketsLost(AlivcLivePusher alivcLivePusher) {

                }
            };
            mOnPushInfo = new AlivcLivePushInfoListener() {
                @Override
                public void onPreviewStarted(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onPreviewStoped(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onPushStarted(AlivcLivePusher alivcLivePusher) {
                    mHandler.sendEmptyMessage(1001);
                }

                @Override
                public void onFirstAVFramePushed(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onPushPauesed(AlivcLivePusher alivcLivePusher) {
                    mHandler.sendEmptyMessage(1001);
                }

                @Override
                public void onPushResumed(AlivcLivePusher alivcLivePusher) {
                    mHandler.sendEmptyMessage(1001);
                }

                @Override
                public void onPushStoped(AlivcLivePusher alivcLivePusher) {
                    mHandler.sendEmptyMessage(1001);
                }

                @Override
                public void onPushRestarted(AlivcLivePusher alivcLivePusher) {
                    mHandler.sendEmptyMessage(1001);
                }

                @Override
                public void onFirstFramePreviewed(AlivcLivePusher alivcLivePusher) {

                }

                @Override
                public void onDropFrame(AlivcLivePusher alivcLivePusher, int i, int i1) {

                }

                @Override
                public void onAdjustBitRate(AlivcLivePusher alivcLivePusher, int i, int i1) {

                }

                @Override
                public void onAdjustFps(AlivcLivePusher alivcLivePusher, int i, int i1) {

                }
            };

            inited = true;
        } catch (Exception er) {
            Log.e(TAG, "init: " + er.getMessage());
        }
    }

    public void showFlowWin(int indexOfSize, int indexOfOrientation, String rtmpUrl) {
        try {
            mIndexOfSize = indexOfSize;
            mIndexOfOrientation = indexOfOrientation;
            mRtmpUrl = rtmpUrl;
            boolean aFlag = FloatWindowManager.getInstance().applyFloatWindow(mCtx);
            if (aFlag) {
                if (!mFlowWin.isShown()) {
                    mWindowManager.addView(mFlowWin, mFloatToolbar_Param);
                }
            }
            mBtnStart.setVisibility(View.VISIBLE);
            mBtnPause.setVisibility(View.GONE);
            mBtnResume.setVisibility(View.GONE);
            mBtnStop.setVisibility(View.GONE);
        } catch (Exception er) {
            Log.e(TAG, "showFlowWin: " + er.getMessage());
        }
    }

    private void hideFlowWin() {
        try {
            if (Build.VERSION.SDK_INT >= 24) { // honeycomb
                mWindowManager.removeView(mFlowWin);
                final ActivityManager activityManager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
                final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
                for (int i = 0; i < recentTasks.size(); i++) {
                    Log.d("Executed app", "Application executed : "
                            + recentTasks.get(i).baseActivity.toShortString()
                            + "\t\t ID: " + recentTasks.get(i).id + "");
                    String className = recentTasks.get(i).baseActivity.toShortString();
                    Log.d(TAG, "class activity: " + className);
                    // bring to front
                    if (recentTasks.get(i).baseActivity.toShortString().contains("MainApp")) {
                        activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                    }
                }
            }
        } catch (Exception er) {
            Log.e(TAG, "hideFlowWin: " + er.getMessage());
        }

    }

    private void acRefresh() {
        try {
            mBtnStart.setVisibility(View.GONE);
            mBtnPause.setVisibility(View.GONE);
            mBtnResume.setVisibility(View.GONE);
            mBtnStop.setVisibility(View.GONE);
            if (mAlivcLivePusher != null) {
                AlivcLivePushStats status = mAlivcLivePusher.getCurrentStatus();
                int canStart = status == AlivcLivePushStats.PREVIEWED ? View.VISIBLE : View.GONE;
                int canPause = status == AlivcLivePushStats.PUSHED ? View.VISIBLE : View.GONE;
                int canResume = status == AlivcLivePushStats.PAUSED ? View.VISIBLE : View.GONE;
                int canStop = (status == AlivcLivePushStats.PUSHED || status == AlivcLivePushStats.PAUSED) ? View.VISIBLE : View.GONE;
                mBtnStart.setVisibility(canStart);
                mBtnPause.setVisibility(canPause);
                mBtnResume.setVisibility(canResume);
                mBtnStop.setVisibility(canStop);
                if (status == AlivcLivePushStats.PREVIEWED)
                    mTxtStatus.setText("等待直播");
                else if (status == AlivcLivePushStats.PUSHED)
                    mTxtStatus.setText("直播中");
                else if (status == AlivcLivePushStats.PAUSED)
                    mTxtStatus.setText("直播暂停");
                else
                    mTxtStatus.setText("直播结束");
            } else {
                mBtnStart.setVisibility(View.VISIBLE);
                mBtnPause.setVisibility(View.VISIBLE);
                mBtnResume.setVisibility(View.VISIBLE);
                mBtnStop.setVisibility(View.VISIBLE);
                mTxtStatus.setText("等待直播");
            }
        } catch (Exception er) {
            Log.e(TAG, "acRefresh: " + er.getMessage());
        }
    }

    private void acShowRequestCaptureScreen() {
        try {
            FloatWindowManager.RequestPermissions(mCtx, true);
        } catch (Exception er) {
            Log.e(TAG, "acShowRequestCaptureScreen: " + er.getMessage());
        }
    }

    public void acStart(Intent data) {
        try {
            //判断权限
            //停止正在进行的推流
            //根据应用需求，重新设置 直播参数（size , orientation , rtmpUrl）
            //初始化推流器
            //预览
            //推流

            AlivcLivePushConfig.setMediaProjectionPermissionResultData(data);
            if (mAlivcLivePushConfig.getMediaProjectionPermissionResultData() != null) {
                try {
                    acStop();
                    mAlivcLivePusher = new AlivcLivePusher();
                    AlivcPreviewOrientationEnum[] orientations = new AlivcPreviewOrientationEnum[]{
                            AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT,
                            AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT
                    };
                    AlivcResolutionEnum[] sizes = new AlivcResolutionEnum[]{
                            AlivcResolutionEnum.RESOLUTION_1080P,
                            AlivcResolutionEnum.RESOLUTION_720P,
                            AlivcResolutionEnum.RESOLUTION_540P
                    };
                    mAlivcLivePushConfig.setPreviewOrientation(orientations[mIndexOfOrientation]);
                    mAlivcLivePushConfig.setResolution(sizes[mIndexOfSize]);

                    mAlivcLivePusher.init(mCtx, mAlivcLivePushConfig);
                    mAlivcLivePusher.setLivePushErrorListener(mOnPushError);
                    mAlivcLivePusher.setLivePushNetworkListener(mOnPushNet);
                    mAlivcLivePusher.setLivePushInfoListener(mOnPushInfo);
                    mAlivcLivePusher.startPreview(null);
                    mAlivcLivePusher.startPush(mRtmpUrl);
                } catch (Exception er) {
                    Log.e(TAG, "start error " + er.getMessage());
                }
            }

        } catch (Exception er) {
            Log.e(TAG, "start: " + er.getMessage());
        }
    }

    private void acResume() {
        try {
            boolean isPause = mAlivcLivePusher != null && mAlivcLivePusher.getCurrentStatus() == AlivcLivePushStats.PAUSED;
            if (isPause) {
                mAlivcLivePusher.resume();
                mAlivcLivePusher.setMute(false);
            }
        } catch (Exception er) {
            Log.e(TAG, "acResume: " + er.getMessage());
        }
    }

    private void acPause() {
        try {
            if (mAlivcLivePusher != null && mAlivcLivePusher.getCurrentStatus() == AlivcLivePushStats.PUSHED) {
                mAlivcLivePusher.pause();
                mAlivcLivePusher.setMute(true);
            }
        } catch (Exception er) {
            Log.e(TAG, "pause: " + er.getMessage());
        }
    }

    private void acStop() {
        try {
            if (mAlivcLivePusher != null) {
                try {
                    mAlivcLivePusher.stopPush();
                } catch (Exception err) {
                    Log.e(TAG, "stopPush: " + err.getMessage());
                }
                try {
                    mAlivcLivePusher.stopPreview();
                } catch (Exception err) {
                    Log.e(TAG, "stopPreview: " + err.getMessage());
                }
                try {
                    mAlivcLivePusher.destroy();
                } catch (Exception err) {
                    Log.e(TAG, "destroy: " + err.getMessage());
                }
                try {
                    mAlivcLivePusher = null;
                } catch (Exception err) {
                    Log.e(TAG, "null: " + err.getMessage());
                }
                mHandler.sendEmptyMessage(1001);
            }
        } catch (Exception er) {
            Log.e(TAG, "acStop: " + er.getMessage());
        }
    }

    private void acClose() {
        try {
            hideFlowWin();
        } catch (Exception er) {
            Log.e(TAG, "close: " + er.getMessage());
        }
    }

}
