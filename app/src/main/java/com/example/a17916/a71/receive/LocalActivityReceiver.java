package com.example.a17916.a71.receive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.a17916.a71.util.normal.AddJsonParameterUtil;
import com.example.a17916.a71.util.normal.AppUtil;
import com.example.a17916.a71.util.normal.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 主要用来播放Intent，输入，点击事件。
 * 抽取，传递页面信息
 */
public class LocalActivityReceiver extends BroadcastReceiver implements CallBackToServer{
    private Activity selfActivity;
    public static final String intent = "intent";
    public static final String currentActivity = "currentActivity";
    public static final String openTargetActivityByIntent = "openTargetActivityByIntent";
    public static final String INPUT_TEXT = "INPUT_TEXT";
    public static final String TEXT_KEY = "TEXT_KEY";
    public static final String INPUT_EVENT = "INPUT_EVENT";
    public static final String EVENTS = "EVENTS";
    public static final String DEEP_LINK_KEY = "DEEP_LINK_KEY";

    public static final String GenerateIntentData = "GenerateIntentData";
    public static final String GenerateDeepLink = "GenerateDeepLink";

    public static final String fromAppStart = "fromAppStart";
    public static final String fromActivityStart ="fromActivityStart";
    public static final String fromActivityPlay = "fromActivityPlay";
    public static final String TARGET_INTENT = "targetIntent";

    private String showActivityName = "";
    private String selfActivityName = "";
    private String selfPackageName;
    private String selfAppName;
    private String curPackageName = "";
    private String curAppName;
    private String textKey ;
    private byte[] eventBytes;
    private String startActivityFrom;
    private String startActivityFromApp;

    private int eventTime = 0;
    public LocalActivityReceiver(Activity activity){
        selfActivity = activity;
        selfActivityName = activity.getComponentName().getClassName();
        selfPackageName = activity.getPackageName();
        selfAppName = AppUtil.getAppName(selfActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case LocalActivityReceiver.currentActivity:
                Bundle bundle = intent.getBundleExtra("currentActivity");
                showActivityName = (String) bundle.get("showActivity");
                curPackageName = (String)bundle.get("curPackage");
                curAppName = (String) bundle.get("curApp");
                break;
            case LocalActivityReceiver.openTargetActivityByIntent:
                Intent tarIntent = intent.getParcelableExtra(LocalActivityReceiver.TARGET_INTENT);
                startActivityFrom = intent.getStringExtra(LocalActivityReceiver.fromActivityStart);
                startActivityFromApp = intent.getStringExtra(LocalActivityReceiver.fromAppStart);
                Log.i("LZH","self: "+selfAppName+"start: "+startActivityFromApp);

                if(selfAppName.compareTo(startActivityFromApp)!=0||showActivityName.compareTo(selfActivityName)!=0){
                    break;
                }
                Log.i("LZH","从"+selfActivityName+"打开"+startActivityFrom);

                selfActivity.startActivity(tarIntent);
                break;
            case LocalActivityReceiver.INPUT_TEXT:
                textKey = intent.getStringExtra(LocalActivityReceiver.TEXT_KEY);
                startActivityFrom = intent.getStringExtra(LocalActivityReceiver.fromActivityPlay);
                if(startActivityFrom.compareTo(selfActivityName)!=0){
                    break;
                }
                Log.i("LZH"," to InputText");
                inputText(textKey);
                break;
            case LocalActivityReceiver.INPUT_EVENT:
                Log.i("LZH","intput_event: "+eventTime++);
                eventBytes = intent.getByteArrayExtra(LocalActivityReceiver.EVENTS);
                //在指定的页面播放点击事件
                startActivityFrom = intent.getStringExtra(LocalActivityReceiver.fromActivityPlay);
                if(startActivityFrom.compareTo(selfActivityName)!=0){
                    break;
                }
                playMotionEvent(eventBytes);
                break;
            case LocalActivityReceiver.GenerateIntentData:
                if(selfActivityName.equals(showActivityName)){

                    analyseJSON();

                }
                break;
            case LocalActivityReceiver.GenerateDeepLink:
                if(selfActivityName.equals(showActivityName)) {
                    Log.i("ycx", "receive GenerateDeepLink command.");
                    String randomKey = intent.getStringExtra(DEEP_LINK_KEY);
                    AddJsonParameterUtil addJsonParameterUtil = new AddJsonParameterUtil(selfActivity);
                    addJsonParameterUtil.generateDeepLink(selfActivityName, randomKey, selfActivity.getIntent());
                }
                break;

        }
    }


    private void inputText(String textKey) {
        View view = selfActivity.getWindow().getDecorView();
        EditText editText = findEditText(view);
        if(editText==null){
            Log.i("LZH","未找到EditText");
        }
        Log.i("LZH","输入text: "+textKey);
        editText.setText(textKey);
    }
    private EditText findEditText(View view){
        ArrayList<View> list = new ArrayList<>();
        list.add(view);
        View cur;
        ViewGroup viewGroup;
        while (!list.isEmpty()){
            cur = list.remove(0);
            if(cur instanceof ViewGroup){
                viewGroup = (ViewGroup) cur;
                for(int i=0;i<viewGroup.getChildCount();i++){
                    list.add(viewGroup.getChildAt(i));
                }
            }else if(cur instanceof EditText){
                return (EditText) cur;
            }
        }
        return null;
    }


    private void playMotionEvent(byte[] bytes) {
        //延时1s，回放点击事件，保证view已经被刷新出来
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MotionEvent[] motionEvents = tranformtoMotionEvent(bytes);
        MotionEvent curEvent;
        for(int i=0;i<motionEvents.length;i++){
            curEvent = MotionEvent.obtain(motionEvents[i]);
            selfActivity.dispatchTouchEvent(curEvent);
//            targetActivity.dispatchTouchEvent(motionEvents[i]);
            Log.i("LZH","x: "+curEvent.getRawX()+" y: "+curEvent.getRawY());
        }
    }
    private MotionEvent[] tranformtoMotionEvent(byte[] bytes){
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes,0,bytes.length);
        parcel.setDataPosition(0);
        int size = parcel.readInt();
        MotionEvent[] motionEvents = new MotionEvent[size];
        parcel.readTypedArray(motionEvents,MotionEvent.CREATOR);
        return motionEvents;
    }
    private void analyseJSON(){
        String specialKey = DateUtil.getHMS();
        Log.i("LZH","special Key: "+specialKey);
        AddJsonParameterUtil addJsonParameterUtil = new AddJsonParameterUtil(selfActivity);
        addJsonParameterUtil.addParameter(selfActivityName,specialKey,selfActivity.getIntent());
    }

    @Override
    public String getContent() {
        return content;
    }

}
