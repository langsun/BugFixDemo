package com.sun.hotfixdemo;

import android.app.Application;

/**
 * Created by sun on 2019/04/01.
 */
public class MyApplication extends Application {

    private static MyApplication mApplication;

    public static Application getContext() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mApplication = this;
    }
}
