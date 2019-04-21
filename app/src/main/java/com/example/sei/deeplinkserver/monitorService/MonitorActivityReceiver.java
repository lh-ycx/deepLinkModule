package com.example.sei.deeplinkserver.monitorService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;
import android.util.Log;

import com.example.sei.deeplinkserver.receive.LocalActivityReceiver;

import java.util.ArrayList;

public class MonitorActivityReceiver extends BroadcastReceiver implements Operation{
    private String targetActivityName;
    private ArrayList<String> liveActivity;// 表示当前应用存活的Activity

    private Service service;

    private ArrayMap<String,Boolean> mapIsOpened;

    private boolean isInputedText = false;
    private boolean recycle = false;


    //延时打开
    private OnResumeRunnable onResumeRunnable = null;
    private Thread thread = null;

    private MyActivityHandler myHandler;
    public MonitorActivityReceiver(Service service){
        this.service = service;

        liveActivity = new ArrayList<>();
        mapIsOpened = new ArrayMap<>();
        myHandler = MyActivityHandler.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case MonitorActivityService.ON_CREATE_STATE:
                myHandler.onCreateActivity(this,intent);
                break;
            case MonitorActivityService.ON_RESUME_STATE:
                //Log.i("LZH","收到显示的页面");
                //由于每个APP的打开方式不一样，所以使用一个线程，等打开的页面稳定后，在线程中打开
                if(recycle){
                    onResumeRunnable = null;
                    recycle = false;
                }
                if(onResumeRunnable == null){
                    onResumeRunnable = new OnResumeRunnable(this,myHandler,this,intent);
                    thread = new Thread(onResumeRunnable);
                    thread.start();
                }else{
                    onResumeRunnable.setUpdate(true,this,intent);
                }

                break;
            case MonitorActivityService.ON_DESTROY_STATE:
                myHandler.onDestroyActivity(this,intent);
                break;
            case MonitorActivityService.INPUTED_TEXT:
                isInputedText = true;
            case MonitorActivityService.ON_DRAW:
                if(isInputedText){
                    myHandler.onDrawView(this,intent);
                }
        }

    }



    private void resetState(){
        liveActivity.clear();
    }

    public void setTargetIntent(Intent intent){

        ComponentName componentName = intent.getComponent();
        targetActivityName = componentName.getClassName();
        resetState();
        mapIsOpened.put(targetActivityName,false);

    }

    @Override
    public void operationStartActivity(Intent intent,String fromActivity) {
        Intent broadIntent = new Intent();
        broadIntent.putExtra(LocalActivityReceiver.TARGET_INTENT,intent);
        broadIntent.putExtra(LocalActivityReceiver.fromActivityStart,fromActivity);
        broadIntent.setAction(LocalActivityReceiver.openTargetActivityByIntent);
        service.sendBroadcast(broadIntent);
    }

    @Override
    public void operationStartActivity(Intent intent, String fromActivity, String fromApp) {
        Intent broadIntent = new Intent();
        broadIntent.putExtra(LocalActivityReceiver.TARGET_INTENT, intent);
        broadIntent.putExtra(LocalActivityReceiver.fromAppStart,fromApp);
        broadIntent.setAction(LocalActivityReceiver.openTargetActivityByIntent);
        Log.i("LZH","打开应用对应页面");
        service.sendBroadcast(broadIntent);
    }

    @Override
    public void operationReplayInputEvent(String text,String fromActivity) {
        Intent broadIntent = new Intent();
        broadIntent.setAction(LocalActivityReceiver.INPUT_TEXT);
        broadIntent.putExtra(LocalActivityReceiver.TEXT_KEY,text);
        broadIntent.putExtra(LocalActivityReceiver.fromActivityPlay,fromActivity);
        service.sendBroadcast(broadIntent);
    }

    @Override
    public void operationReplayMotionEvent(byte[] events,String fromActivity) {
        Intent broadIntent = new Intent();
        broadIntent.setAction(LocalActivityReceiver.INPUT_EVENT);
        broadIntent.putExtra(LocalActivityReceiver.EVENTS,events);
        broadIntent.putExtra(LocalActivityReceiver.fromActivityPlay,fromActivity);
        service.sendBroadcast(broadIntent);
    }

    @Override
    public void operationStartActivityByDeepLink(String deepLink, String fromActivity, String tarPackageName) {
        Intent broadIntent = new Intent();
        broadIntent.putExtra(LocalActivityReceiver.DEEP_LINK, deepLink);
        broadIntent.putExtra(LocalActivityReceiver.tarPackageName,tarPackageName);
        broadIntent.setAction(LocalActivityReceiver.openTargetActivityByDeepLink);
        Log.i("ycx","使用deepLink打开应用对应页面");
        service.sendBroadcast(broadIntent);
    }

    private void setRecycle(Boolean b){
        recycle = b;
    }

    private static class OnResumeRunnable implements Runnable{
        private MyActivityHandler handler;
        private Operation operation;
        private Intent intent;
        private Boolean update = true;
        private MonitorActivityReceiver receiver;
        public OnResumeRunnable(MonitorActivityReceiver receiver,MyActivityHandler handler,Operation operation,Intent intent){
            this.handler = handler;
            this.operation = operation;
            this.intent = intent;
            this.receiver = receiver;
        }

        @Override
        public void run() {
            while(update){
                update = false;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.i("LZH","Intent sleep");
            }
            handler.onResumeActivity(operation,intent);
            receiver.setRecycle(true);
        }
        public void setUpdate(Boolean b,Operation operation,Intent intent){
            update = b;
            this.operation = operation;
            this.intent = intent;
        }
    }

}
