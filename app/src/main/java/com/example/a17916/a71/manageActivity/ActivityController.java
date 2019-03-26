package com.example.a17916.a71.manageActivity;

import android.content.Context;
import android.content.Intent;

import com.example.a17916.a71.monitorService.MyActivityHandler;
import com.example.a17916.a71.monitorService.OpenActivityTask;
import com.example.a17916.a71.share.SavePreference;


public class ActivityController {
    private Context context;
    private static ActivityController activityController;
    public static final String SEND_ACTIVITY_INTENT = "sendActivityIntent";
    public static final String SEND_INTENT_MOTION = "sendIntentMotion";
    public static final String OPEN_ACTIVITY = "openActivity";
    public static final String TARGET_INTENT = "targetIntent";
    public static final String PK_NAME = "packageName";
    public static final String OPEN_ANALYSE_ACTIVITY = "openAnalyseResultActivity";
    public static final String TEXT = "TEXT";

    private String currentActivityName;
    private MyActivityHandler myHandler;
    public ActivityController(Context context){
        this.context = context;
        myHandler = MyActivityHandler.getInstance();
    }
    public static ActivityController getInstance(Context context){
        if(activityController == null){
            activityController = new ActivityController(context);
        }
        return activityController;
    }

    /**
     * 仅仅打开目标APP
     * @param packageName
     * @param activityName
     */
    public void openActivity(String packageName,String activityName){
        Intent broadIntent = new Intent();
        broadIntent.putExtra(ActivityController.PK_NAME,packageName);
        context.sendBroadcast(broadIntent);
    }

    /**
     * 打开App并传送要打开页面的Intent
     * @param packageName
     * @param intent
     */
    public void openActivity(String packageName,Intent intent){
        Intent broadIntent = new Intent();
        broadIntent.setAction(ActivityController.SEND_ACTIVITY_INTENT);
        broadIntent.putExtra(ActivityController.TARGET_INTENT,intent);
        broadIntent.putExtra(ActivityController.PK_NAME,packageName);
        context.sendBroadcast(broadIntent);
    }

    /**
     * 给任务队列添加一个任务，任务队列在MyActivityHandler中，将在MonitorActivityReceiver中得到处理
     *
     * 未真正实现任务队列，目前假设队列中只有一个任务
     * @param packageName
     * @param activityName
     * @param task
     */
    public void addTask(String packageName,String activityName, OpenActivityTask task){
        task.setContext(context);
        task.setMyHandler(myHandler);
        task.setSavePreference(SavePreference.getInstance(context));
        myHandler.addTask(task);

        Intent broadIntent = new Intent();
        broadIntent.setAction(ActivityController.OPEN_ACTIVITY);
        broadIntent.putExtra(ActivityController.PK_NAME,packageName);
        context.sendBroadcast(broadIntent);
    }

    /**
     * 先把任务添加到队列，然后打开对用的App
     * @param packageName
     * @param task
     */
    public void addTask(String packageName, OpenActivityTask task){
        task.setContext(context);
        task.setMyHandler(myHandler);
        task.setSavePreference(SavePreference.getInstance(context));
        myHandler.addTask(task);

        Intent broadIntent = new Intent();
        broadIntent.setAction(ActivityController.OPEN_ACTIVITY);
        broadIntent.putExtra(ActivityController.PK_NAME,packageName);
        context.sendBroadcast(broadIntent);
    }


}
