package com.example.mobilevideo.ui.videolist;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.CompleteView;
import com.dueeeke.videocontroller.component.ErrorView;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videocontroller.component.TitleView;
import com.dueeeke.videoplayer.player.VideoView;
import com.dueeeke.videoplayer.player.VideoViewManager;

import com.example.mobilevideo.R;
import com.example.mobilevideo.modules.adapter.VideoRecyclerViewAdapter;
import com.example.mobilevideo.modules.adapter.listener.OnItemChildClickListener;
import com.example.mobilevideo.modules.bean.Streams;
import com.example.mobilevideo.modules.bean.VideoBean;
import com.example.mobilevideo.modules.getStream.TGetLiveStream;
import com.example.mobilevideo.modules.utils.DataUtil;
import com.example.mobilevideo.modules.utils.Utils;
import com.example.mobilevideo.modules.widget.controller.PortraitWhenFullScreenController;

import java.util.ArrayList;
import java.util.List;

public class VideoList extends Fragment implements OnItemChildClickListener {
    private final static String TAG = "VideoList";
    @SuppressLint("StaticFieldLeak")
    public static VideoView smVideoView;
    private View root;
    private boolean mIsInitData = false;
    protected List<VideoBean> mVideos = new ArrayList<>();
    protected VideoRecyclerViewAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    protected VideoView mVideoView;
    protected StandardVideoController mController;
    protected ErrorView mErrorView;
    protected CompleteView mCompleteView;
    protected TitleView mTitleView;
    protected int mCurPos = -1;//当前播放的位置
    protected int mLastPos = mCurPos;//上次播放的位置，用于页面切回来之后恢复播放
    Handler mHandler;
    List<VideoBean> videoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        }
        initView();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchData();
    }

    @SuppressLint("HandlerLeak")
    protected void initView() {

        initVideoView();
        //保存进度
//        mVideoView.setProgressManager(new ProgressManagerImpl());

        mRecyclerView = root.findViewById(R.id.rv);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new VideoRecyclerViewAdapter(mVideos);
        mAdapter.setOnItemChildClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                FrameLayout playerContainer = view.findViewById(R.id.player_container);
                View v = playerContainer.getChildAt(0);
                if (v != null && v == mVideoView && !mVideoView.isFullScreen()) {
                    releaseVideoView();
                }
            }
        });

        View view = root.findViewById(R.id.add);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acGetLiveStream();
                //mVideos.addAll(videoList);
                //mAdapter.notifyDataSetChanged();
                //mAdapter.addData(videoList);
            }
        });
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1000) {
                    try {
                        mVideos.clear();
                        mVideos.addAll(videoList);
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception er) {
                        Log.e(TAG, ": " + er.getMessage());
                    }
                }
            }
        };
    }

    protected void initVideoView() {
        mVideoView = new VideoView(getActivity());
        smVideoView = mVideoView;
        mVideoView.setOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                //监听VideoViewManager释放，重置状态
                if (playState == VideoView.STATE_IDLE) {
                    Utils.removeViewFormParent(mVideoView);
                    mLastPos = mCurPos;
                    mCurPos = -1;
                }
            }
        });
        mController = new PortraitWhenFullScreenController(getActivity());
        mErrorView = new ErrorView(getActivity());
        mController.addControlComponent(mErrorView);
        mCompleteView = new CompleteView(getActivity());
        mController.addControlComponent(mCompleteView);
        mTitleView = new TitleView(getActivity());
        mController.addControlComponent(mTitleView);
        mController.addControlComponent(new GestureView(getActivity()));
        mController.setEnableOrientation(true);
        mVideoView.setVideoController(mController);
    }

    void acGetLiveStream() {
        try {
            Thread acGetLiveStreamThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    List<Streams> GetRes = TGetLiveStream.getLiveStream(getActivity());
                    videoList = DataUtil.getVideoList();
                    if (!GetRes.isEmpty()) {
                        for (int i = 0; i < GetRes.size(); i++) {
                            String streamName = GetRes.get(i).getName();
                            String StreamUrl = "rtmp://919129850.top/live/" + streamName;
                            videoList.add(new VideoBean("用户直播",
                                    "https://cms-bucket.nosdn.127.net/cb37178af1584c1588f4a01e5ecf323120180418133127.jpeg",
                                    StreamUrl));
                        }
                    }
                    mHandler.sendEmptyMessage(1000);
                }
            };
            acGetLiveStreamThread.setDaemon(true);
            acGetLiveStreamThread.start();
        } catch (Exception er) {
            Log.e(TAG, "acGetLiveStream: " + er.getMessage());
        }
    }

    protected void initData() {
        acGetLiveStream();

    }

    /**
     * 由于onPause必须调用super。故增加此方法，
     * 子类将会重写此方法，改变onPause的逻辑
     */
    public void onPause() {
        super.onPause();
        pause();
    }

    protected void pause() {
        releaseVideoView();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
        resume();
    }

    /**
     * 由于onResume必须调用super。故增加此方法，
     * 子类将会重写此方法，改变onResume的逻辑
     */
    protected void resume() {
        if (mLastPos == -1)
            return;
        //恢复上次播放的位置
        startPlay(mLastPos);
    }

    /**
     * PrepareView被点击
     */

    public void onItemChildClick(int position) {
        mVideoView.startFullScreen();
        startPlay(position);
    }

    /**
     * 开始播放
     *
     * @param position 列表位置
     */
    protected void startPlay(int position) {
        if (mCurPos == position) return;
        if (mCurPos != -1) {
            releaseVideoView();
        }
        VideoBean videoBean = mVideos.get(position);
        //边播边存
//        String proxyUrl = ProxyVideoCacheManager.getProxy(getActivity()).getProxyUrl(videoBean.getUrl());
//        mVideoView.setUrl(proxyUrl);

        mVideoView.setUrl(videoBean.getUrl());
        mTitleView.setTitle(videoBean.getTitle());
        View itemView = mLinearLayoutManager.findViewByPosition(position);
        if (itemView == null) return;
        VideoRecyclerViewAdapter.VideoHolder viewHolder = (VideoRecyclerViewAdapter.VideoHolder) itemView.getTag();
        //把列表中预置的PrepareView添加到控制器中，注意isPrivate此处只能为true。
        mController.addControlComponent(viewHolder.mPrepareView, true);
        Utils.removeViewFormParent(mVideoView);
        viewHolder.mPlayerContainer.addView(mVideoView, 0);
        //播放之前将VideoView添加到VideoViewManager以便在别的页面也能操作它
        VideoViewManager.instance().add(mVideoView, "list");
        mVideoView.start();
        mCurPos = position;

    }

    private void releaseVideoView() {
        mVideoView.release();
        if (mVideoView.isFullScreen()) {
            mVideoView.stopFullScreen();
        }
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mCurPos = -1;
    }

    private void fetchData() {
        if (mIsInitData)
            return;
        initData();
        mIsInitData = true;
    }

}