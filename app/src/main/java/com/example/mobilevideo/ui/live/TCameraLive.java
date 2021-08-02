package com.example.mobilevideo.ui.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.alivc.live.pusher.AlivcLivePushCameraTypeEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStats;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.example.mobilevideo.R;

import java.util.List;

import floatwindowpermission.FloatWindowManager;


public class TCameraLive extends Fragment {
    public final static String TAG = "TCameraLive";

    @SuppressLint("StaticFieldLeak")
    private static TCameraLive cameraLive = null;

    public static TCameraLive getCameraLive() {
        if (cameraLive == null) {
            cameraLive = new TCameraLive();
        }
        return cameraLive;
    }

    public boolean inited = false;
    public Activity mCtx;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mFloatToolbar_Param;
    private View mFlowWin;

    SurfaceView mSurfaceView;
    ImageButton mBtnStart;
    ImageButton mBtnPause;
    ImageButton mBtnResume;
    ImageButton mBtnStop;
    ImageButton mBtnSwitchCamera;
    ImageButton mBtnClose;

    private AlivcLivePushConfig mAlivcLivePushConfig;
    private AlivcLivePusher mAlivcLivePusher;
    private AlivcLivePushInfoListener mOnPushInfo;
    private AlivcLivePushNetworkListener mOnPushNet;
    private AlivcLivePushErrorListener mOnPushError;
    Handler mHandler;

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
            mFloatToolbar_Param.width = WindowManager.LayoutParams.MATCH_PARENT;
            mFloatToolbar_Param.height = WindowManager.LayoutParams.MATCH_PARENT;
            mFloatToolbar_Param.x = 0;
            mFloatToolbar_Param.y = 0;
            //View
            mFlowWin = LayoutInflater.from(mCtx).inflate(R.layout.layout_camera_live, null);
            mSurfaceView = mFlowWin.findViewById(R.id.surfaceCameraLive);
            mBtnStart = mFlowWin.findViewById(R.id.btnCameraLiveStart);
            mBtnPause = mFlowWin.findViewById(R.id.btnCameraLivePause);
            mBtnResume = mFlowWin.findViewById(R.id.btnCameraLiveResume);
            mBtnStop = mFlowWin.findViewById(R.id.btnCameraLiveStop);
            mBtnSwitchCamera = mFlowWin.findViewById(R.id.btnCameraLiveCamera);
            mBtnClose = mFlowWin.findViewById(R.id.btnCameraLiveClose);

            mBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acStart();
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
            mBtnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acStop();
                    acClose();
                }
            });
            mBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SwitchCamera();
                    } catch (Exception er) {
                        Log.e(TAG, "switchCamera error " + er.getMessage());
                    }
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
                    if (msg.what == 1000) {
                        try {
                            mAlivcLivePusher.startPreviewAysnc(mSurfaceView);
                        } catch (Exception er) {
                            Log.e(TAG, "startPreviewAysnc: " + er.getMessage());
                        }
                    } else if (msg.what == 1001) {
                        acRefresh();
                    }
                }
            };
            //Config 初始化直播配置资源
            mAlivcLivePushConfig = new AlivcLivePushConfig();
            mAlivcLivePushConfig.setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT);
            mAlivcLivePushConfig.setPreviewDisplayMode(AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL);
            mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_540P);
            mAlivcLivePushConfig.setCameraType(AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT);
            //回调
            mOnPushError = new AlivcLivePushErrorListener() {
                @Override
                public void onSystemError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {
                    acStop();
                    acStart();
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
                    mHandler.sendEmptyMessage(1001);
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
            Log.e(TAG, "uiInit: " + er.getMessage());
        }
    }

    void acRefresh() {
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
            } else {
                mBtnStart.setVisibility(View.VISIBLE);
                mBtnPause.setVisibility(View.VISIBLE);
                mBtnResume.setVisibility(View.VISIBLE);
                mBtnStop.setVisibility(View.VISIBLE);
            }

        } catch (Exception er) {
            Log.e(TAG, "acRefresh: " + er.getMessage());
        }
    }

    public void showFlowWin(int indexOfCamera, int indexOfSize, int indexOfOrientation, String rtmpUrl) {
        try {
            mRtmpUrl = rtmpUrl;
            boolean aFlag = FloatWindowManager.getInstance().applyFloatWindow(mCtx);
            if (aFlag) {
                if (!mFlowWin.isShown()) {
                    mWindowManager.addView(mFlowWin, mFloatToolbar_Param);
                }
            }

            //判断权限
            //停止正在进行的推流
            //根据应用需求，重新设置 直播参数（size , orientation , rtmpUrl）
            //初始化推流器
            //预览
            //推流
            acStop();
            mAlivcLivePusher = new AlivcLivePusher();
            AlivcLivePushCameraTypeEnum[] cameraTypes = new AlivcLivePushCameraTypeEnum[]{
                    AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT,
                    AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK
            };
            AlivcPreviewOrientationEnum[] orientations = new AlivcPreviewOrientationEnum[]{
                    AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT,
                    AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT
            };
            AlivcResolutionEnum[] sizes = new AlivcResolutionEnum[]{
                    AlivcResolutionEnum.RESOLUTION_1080P,
                    AlivcResolutionEnum.RESOLUTION_720P,
                    AlivcResolutionEnum.RESOLUTION_540P
            };
            mAlivcLivePushConfig.setCameraType(cameraTypes[indexOfCamera]);
            mAlivcLivePushConfig.setPreviewOrientation(orientations[indexOfOrientation]);
            mAlivcLivePushConfig.setResolution(sizes[indexOfSize]);
            mAlivcLivePushConfig.setPreviewDisplayMode(AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL);

            mAlivcLivePusher.init(mCtx, mAlivcLivePushConfig);
            mAlivcLivePusher.setLivePushErrorListener(mOnPushError);
            mAlivcLivePusher.setLivePushNetworkListener(mOnPushNet);
            mAlivcLivePusher.setLivePushInfoListener(mOnPushInfo);
            mHandler.sendEmptyMessage(1000);

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

    private void acStart() {
        try {
            mAlivcLivePusher.startPush(mRtmpUrl);
        } catch (Exception er) {
            Log.e(TAG, "start error " + er.getMessage());
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
            Log.e(TAG, "resume error " + er.getMessage());
        }
    }


    private void acPause() {
        try {
            if (mAlivcLivePusher != null && mAlivcLivePusher.getCurrentStatus() == AlivcLivePushStats.PUSHED) {
                mAlivcLivePusher.pause();
                mAlivcLivePusher.setMute(true);
            }
        } catch (Exception er) {
            Log.e(TAG, "pause error " + er.getMessage());
        }
    }

    private void SwitchCamera() {
        try {
            mAlivcLivePusher.switchCamera();
        } catch (Exception er) {
            Log.e(TAG, "switchCamera error " + er.getMessage());
        }
    }

    private void acStop() {
        try {
            if (mAlivcLivePusher != null) {
                try {
                    mAlivcLivePusher.stopPreview();
                } catch (Exception er) {
                    Log.e(TAG, "stopPreview: " + er.getMessage());
                }
                try {
                    mAlivcLivePusher.stopPush();
                } catch (Exception er) {
                    Log.e(TAG, "stopPush: " + er.getMessage());
                }
                try {
                    mAlivcLivePusher.destroy();
                } catch (Exception er) {
                    Log.e(TAG, "destroy: " + er.getMessage());
                }
                try {
                    mAlivcLivePusher = null;
                } catch (Exception er) {
                    Log.e(TAG, "null: " + er.getMessage());
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
