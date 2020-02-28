package com.hancle.umengplus_example;

import com.hancle.umengplus.UmengplusPlugin;

import io.flutter.app.FlutterApplication;

public class App extends FlutterApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        UmengplusPlugin.init(this, "5e4c90b04ca357e03c00019e", "ceshi", "e214bf89a3549170899c8d0c8b6b0522");
    }
}
