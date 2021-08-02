package com.example.mobilevideo;

import android.app.Application;

public class MainApplication extends Application {

    public static com.example.mobilevideo.MainApplication mainApplication;
    public static String app_uid = null;
    public static String app_nickName = null;
    public static String app_tel = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
    }

}
