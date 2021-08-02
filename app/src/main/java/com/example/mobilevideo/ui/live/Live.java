package com.example.mobilevideo.ui.live;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mobilevideo.R;

import java.util.UUID;

public class Live extends Fragment {
    private final static String TAG = "Live";
    private View root;
    private RadioGroup rgCamera;
    private RadioGroup rgOrientation;
    private RadioGroup rgSize;
    private int mCameraIndex = 1;//0-前摄 1-后摄
    private int mOrientationIndex = 1;//0-横向 1-纵向
    private int mSizeIndex = 1;//0-1080P 1-720P 2-540P

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_live, container, false);
        init();
        return root;
    }

    private void init() {
        try {
            rgCamera = root.findViewById(R.id.rgCamera);
            rgOrientation = root.findViewById(R.id.rgOrientation);
            rgSize = root.findViewById(R.id.rgSize);
            Button btnCamera = root.findViewById(R.id.btnLiveCamera);
            Button btnScreen = root.findViewById(R.id.btnLiveScreen);

            ((RadioButton) rgCamera.getChildAt(1)).setChecked(true);
            ((RadioButton) rgOrientation.getChildAt(1)).setChecked(true);
            ((RadioButton) rgSize.getChildAt(1)).setChecked(true);

            btnCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnClick(v);
                }
            });
            btnScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnClick(v);
                }
            });


        } catch (Exception er) {
            Log.e(TAG, "init" + er.getMessage());
        }
    }

    private void onBtnClick(View v) {
        try {
            //0-前摄 1-后摄
            mCameraIndex = Integer.parseInt(root.findViewById(rgCamera.getCheckedRadioButtonId()).getTag().toString());
            //0-横向 1-纵向
            mOrientationIndex = Integer.parseInt(root.findViewById(rgOrientation.getCheckedRadioButtonId()).getTag().toString());
            //0-1080P 1-720P 2-540P
            mSizeIndex = Integer.parseInt(root.findViewById(rgSize.getCheckedRadioButtonId()).getTag().toString());
            int btnId = v.getId();
            if (btnId == R.id.btnLiveCamera) {
                // camera live
                acLiveCamera();
            } else if (btnId == R.id.btnLiveScreen) {
                // screen live
                acLiveScreen();
            }
        } catch (Exception er) {
            Log.e(TAG, "onClick: " + er.getMessage());
        }
    }

    private void acLiveCamera() {
        try {
            boolean inited = TCameraLive.getCameraLive().inited;
            if (!inited) {
                TCameraLive.getCameraLive().init((Activity) root.getContext());
            }
            String rtmpUrl = root.getContext().getResources().getString(R.string.rtmpUrl) + "/" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
            TCameraLive.getCameraLive().showFlowWin(mCameraIndex, mSizeIndex, mOrientationIndex, rtmpUrl);
        } catch (Exception er) {
            Log.e(TAG, "acLiveCamera: " + er.getMessage());
        }
    }

    private void acLiveScreen() {
        try {
            //获取权限
            boolean inited = TScreenLive.getScreenLive().inited;
            if (!inited) {
                TScreenLive.getScreenLive().init((Activity) root.getContext());
            }
            String rtmpUrl = root.getContext().getResources().getString(R.string.rtmpUrl) + "/" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
            TScreenLive.getScreenLive().showFlowWin(mSizeIndex, mOrientationIndex, rtmpUrl);
        } catch (Exception er) {
            Log.e(TAG, "acLiveCamera: " + er.getMessage());
        }
    }
}
