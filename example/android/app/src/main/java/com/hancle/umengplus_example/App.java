package com.hancle.umengplus_example;

import com.hancle.umengplus.UmengplusPlugin;

import io.flutter.app.FlutterApplication;

public class App extends FlutterApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        UmengplusPlugin.init(this, "xxx", "umeng", "xxxx");
    }
}
