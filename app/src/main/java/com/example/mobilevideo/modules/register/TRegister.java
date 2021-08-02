package com.example.mobilevideo.modules.register;

import android.content.Context;
import android.util.Log;

import com.example.mobilevideo.R;
import com.example.mobilevideo.modules.utils.HttpUtil;

public class TRegister {
    private final static String TAG = "TRegister";

    public static int registerUser(Context context, String uid, String nickName, String pwd, String tel) {
        int res = 0;
        try {
            String url = context.getResources().getString(R.string.apiUrl);
            url += "/userRegister?";
            url += "uid=" + uid;
            url += "&nickName=" + nickName;
            url += "&pwd=" + pwd;
            url += "&tel=" + tel;
            String resp = HttpUtil.httpGet(url);
            if (resp.contains("\"status\": \"1\""))
                res = 1;
            else if (resp.contains("\"status\": \"2\""))
                res = 2;
        } catch (Exception er) {
            Log.e(TAG, "registerUser: " + er.getMessage());
        }
        return res;
    }
}
