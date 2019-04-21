package com.example.sei.deeplinkserver.openTaskModule;

import android.content.Intent;

public class StepContent {
    public final static int INTENT_TYPE = 0x01; //说明这一步是发送Intent的操作
    public final static int INPUT_TEXT_TYPE = 0x02; //说明这一步是发送搜索字的操作
    public final static int MOTION_EVENT_TYPE = 0x04; //说明这一步是发送点击事件的操作
    private int stepType;
    private Intent sendIntent;
    private String inputText;
    private byte[] eventBytes;
    private String activityName;//说明这一步要在哪一个Activity执行
    private String appName;//说明这一步要在哪一个app执行
    private StepCondition stepCondition;

    public StepContent(int type, Intent intent, String text, byte[] bytes){
        stepType = type;
        sendIntent = intent;
        inputText  = text;
        eventBytes = bytes;
    }
    public StepContent(int type, Intent intent, String text, byte[] bytes,String activityName,String appName){
        this(type,intent,text,bytes);
        this.activityName = activityName;
        this.appName = appName;
    }
    public void setStepCondition(StepCondition stepCondition){
        this.stepCondition = stepCondition;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getStepType() {
        return stepType;
    }

    public Intent getSendIntent() {
        return sendIntent;
    }

    public String getInputText() {
        return inputText;
    }

    public byte[] getEventBytes() {
        return eventBytes;
    }

    public StepCondition getStepCondition() {
        return stepCondition;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getAppName() {
        return appName;
    }
}
