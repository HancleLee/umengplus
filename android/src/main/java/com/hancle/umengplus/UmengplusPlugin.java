package com.hancle.umengplus;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.commonsdk.statistics.common.DeviceConfig;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterUmplusPlugin
 */
public class UmengplusPlugin implements MethodCallHandler {
  private Activity activity;
  private static MethodChannel mChannel;

  private UmengplusPlugin(Activity activity) {
    this.activity = activity;
  }

  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    mChannel =
            new MethodChannel(registrar.messenger(), "hancle/umengplus");
    mChannel.setMethodCallHandler(new UmengplusPlugin(registrar.activity()));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("init")) {
      initSetup(call, result);
    } else if (call.method.equals("beginPageView")) {
      beginPageView(call, result);
    } else if (call.method.equals("endPageView")) {
      endPageView(call, result);
    } else if (call.method.equals("logPageView")) {
      logPageView(call, result);
    } else if (call.method.equals("event")) {
      event(call, result);
    } else {
      result.notImplemented();
    }
  }

  private static String getMetadata(Context context, String name) {
    try {
      ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
              context.getPackageName(), PackageManager.GET_META_DATA);
      if (appInfo.metaData != null) {
        return appInfo.metaData.getString(name);
      }
    } catch (PackageManager.NameNotFoundException e) {
    }

    return null;
  }


  private void initSetup(MethodCall call, Result result) {
    Boolean logEnable = (Boolean) call.argument("logEnable");
    Boolean encrypt = (Boolean) call.argument("encrypt");
    Boolean reportCrash = (Boolean) call.argument("reportCrash");
    UMConfigure.setLogEnabled(logEnable);
    UMConfigure.setEncryptEnabled(encrypt);
    MobclickAgent.setSessionContinueMillis(30000L);
    MobclickAgent.setCatchUncaughtExceptions(reportCrash);
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // 大于等于4.4选用AUTO页面采集模式
      MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    } else {
      MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
    }
    result.success(true);
  }

  public void beginPageView(MethodCall call, Result result) {
    String name = (String) call.argument("name");
    Log.d("umengplus_log", "beginPageView: " + name);
    MobclickAgent.onPageStart(name);
    MobclickAgent.onResume(activity);
    result.success(null);
  }

  public void endPageView(MethodCall call, Result result) {
    String name = (String) call.argument("name");
    Log.d("umengplus_log", "endPageView: " + name);
    MobclickAgent.onPageEnd(name);
    MobclickAgent.onPause(activity);
    result.success(null);
  }

  public void logPageView(MethodCall call, Result result) {
    // MobclickAgent.onProfileSignIn((String)call.argument("name"));
    // Session间隔时长,单位是毫秒，默认Session间隔时间是30秒,一般情况下不用修改此值
    // Long seconds = Double.valueOf(call.argument("seconds")).longValue();
    // MobclickAgent.setSessionContinueMillis(seconds);
    result.success(null);
  }

  public void event(MethodCall call, Result result) {
    String name = (String) call.argument("name");
    String label = (String) call.argument("label");
    if (label == null) {
      MobclickAgent.onEvent(activity, name);
    } else {
      MobclickAgent.onEvent(activity, name, label);
    }
    result.success(null);
  }

  public static String[] getTestDeviceInfo(Context context) {
    String[] deviceInfo = new String[2];
    try {
      if (context != null) {
        deviceInfo[0] = DeviceConfig.getDeviceIdForGeneral(context);
        deviceInfo[1] = DeviceConfig.getMac(context);
        Log.d("umengplus_log", deviceInfo[0]);
        Log.d("umengplus_log", deviceInfo[1]);
      }
    } catch (Exception e) {
    }
    return deviceInfo;
  }

  public static void init(Context ctx, String appKey, String channel, String pushSecret) {
    UMConfigure.init(ctx, appKey, channel, UMConfigure.DEVICE_TYPE_PHONE,
            pushSecret);
    UMConfigure.setLogEnabled(true);
    MobclickAgent.setSessionContinueMillis(30000L);

    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // 大于等于4.4选用AUTO页面采集模式
      MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    } else {
      MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
    }

    //获取消息推送代理示例
    PushAgent mPushAgent = PushAgent.getInstance(ctx);
    mPushAgent.register(new IUmengRegisterCallback() {
      @Override
      public void onSuccess(String deviceToken) {
        Log.i("umengplus_log", "推送注册成功：deviceToken：-------->  " + deviceToken + " " + (mChannel == null));
      }

      @Override
      public void onFailure(String s, String s1) {
        Log.e("umengplus_log", "推送注册失败：-------->  " + "s:" + s + ",s1:" + s1);
      }
    });

    UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
      //      @Override
      //      public void dealWithCustomAction(Context context, UMessage msg) {
      //        Log.i("UM_log", "推送消息title：-------->  " + msg.title);
      //      }
    };
    mPushAgent.setNotificationClickHandler(notificationClickHandler);
    UmengMessageHandler messageHandler = new UmengMessageHandler() {
      @Override
      public Notification getNotification(Context context, final UMessage msg) {
        Log.i("umengplus_log", "收到推送消息 title：-------->  " + msg.title + msg.custom);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
          @Override
          public void run() {
            mChannel.invokeMethod("getNotification", msg.extra, new Result() {
              @Override
              public void success(Object o) {
                Log.i("umengplus_log", "推送flutter消息成功：-------->  " + o.toString());
              }

              @Override
              public void error(String s, String s1, Object o) {
                Log.i("umengplus_log", "推送flutter消息失败：-------->  " + o.toString());
              }

              @Override
              public void notImplemented() {
                Log.i("umengplus_log", "推送flutter消息失败：notImplemented  ");
              }
            });
          }
        });
        return super.getNotification(context, msg);
      }
    };
    mPushAgent.setMessageHandler(messageHandler);
  }
}
