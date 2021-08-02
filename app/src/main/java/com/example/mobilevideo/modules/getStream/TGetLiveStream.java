package com.example.mobilevideo.modules.getStream;

import android.content.Context;
import android.util.Log;

import com.example.mobilevideo.R;
import com.example.mobilevideo.modules.bean.JsonBean;
import com.example.mobilevideo.modules.bean.Streams;
import com.example.mobilevideo.modules.utils.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TGetLiveStream {
    private final static String TAG = "TGetLiveStream";

    public static List<Streams> getLiveStream(Context context) {
        List<Streams> resStreams= new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.streamUrl);
            String resp = HttpUtil.httpGet(url);
            JsonBean res = (new Gson().fromJson(resp, JsonBean.class));
            if (res != null)
                resStreams = res.getStreams();
        } catch (Exception er) {
            Log.e(TAG, "loginUser: " + er.getMessage());
        }
        return resStreams;
    }
}
