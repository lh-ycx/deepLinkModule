package com.example.a17916.a71.util.normal;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppUtil {
    public static String getAppName(Activity activity){
        String pkName = activity.getPackageName();
        PackageManager pm = activity.getPackageManager();

        try {
            PackageInfo packageInfo = pm.getPackageInfo(pkName,0);
            int labelRes  = packageInfo.applicationInfo.labelRes;
//            Log.i("LZH","appName: "+selfActivity.getResources().getString(labelRes));
            return activity.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getAppVersion(Activity activity){
        int version = -1;
        PackageInfo packageInfo;
        PackageManager pm = activity.getPackageManager();
        try {
            packageInfo = pm.getPackageInfo(activity.getPackageName(),PackageManager.GET_CONFIGURATIONS);
            version = packageInfo.versionCode;
            return version+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
