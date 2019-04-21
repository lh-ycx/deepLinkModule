package com.example.sei.deeplinkserver.xposed;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import com.example.sei.deeplinkserver.monitorService.MonitorActivityService;
import com.example.sei.deeplinkserver.receive.LocalActivityReceiver;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by vector on 16/8/4.
 * 只用来配合工具，主要是用来查看页面结构和打印intent序列
 */
public class ActivityOnCreateHook extends XC_MethodHook {

    XC_LoadPackage.LoadPackageParam loadPackageParam;
    
    public ActivityOnCreateHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
    }
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);

        final Context context = (Context) param.thisObject;

        Activity activity = (Activity) param.thisObject;
        ComponentName componentName = activity.getComponentName();
        Intent intent = new Intent();
        intent.setAction(MonitorActivityService.ON_CREATE_STATE);
        intent.putExtra(MonitorActivityService.CREATE_PACKAGE_NAME,componentName.getPackageName());
        intent.putExtra(MonitorActivityService.CREATE_ACTIVITY_NAME,componentName.getClassName());
        activity.sendBroadcast(intent);

//        KLog.v("liuyi","=======onCreate========: " + activityName);


//        Log.i("LZH",activity.getComponentName().getPackageName());
//        print(activity.getComponentName().getClassName(),intent);


//        KLog.v(BuildConfig.GETVIEW, "#*#*#*#*#*#*# enable receiver in: " + activityName);
        injectReceiver(context, activity);

    }



    private void injectReceiver(Context context, Activity activity) {
        //注册一个广播接收器，可以用来接收指令，这里是用来回去指定view的xpath路径的



        ComponentName componentName = activity.getComponentName();
        Intent intent = activity.getIntent();
//        Log.i("LZH","packageName: "+componentName.getPackageName()+" intent "+getIntentInfo(intent));
//        Log.i("LZH","openActivity: "+componentName.getClassName());

        LocalActivityReceiver receiver = new LocalActivityReceiver(activity);
        IntentFilter filter = new IntentFilter();

        filter.addAction(LocalActivityReceiver.intent);

        filter.addAction(LocalActivityReceiver.currentActivity);
        filter.addAction(LocalActivityReceiver.openTargetActivityByIntent);

        filter.addAction(LocalActivityReceiver.INPUT_TEXT);
        filter.addAction(LocalActivityReceiver.INPUT_EVENT);
        filter.addAction(LocalActivityReceiver.GenerateIntentData);
        filter.addAction(LocalActivityReceiver.GenerateDeepLink);
        filter.addAction(LocalActivityReceiver.openTargetActivityByDeepLink);
        XposedHelpers.setAdditionalInstanceField(activity, "iasReceiver", receiver);
        activity.registerReceiver(receiver,filter);
//        Log.i("LZH","register activity: "+componentName.getClassName());
    }



}
