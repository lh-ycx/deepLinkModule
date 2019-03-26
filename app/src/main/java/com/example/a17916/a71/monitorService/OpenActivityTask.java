package com.example.a17916.a71.monitorService;

import android.content.Context;
import android.content.Intent;

import com.example.a17916.a71.share.SavePreference;

public abstract class OpenActivityTask {
    protected MyActivityHandler myHandler;
    private Context context;
    protected SavePreference savePreference;
    protected String searchText;
    protected String taskType;//防止多次重复添加任务

    /**
     *
     * @param handler 用来把此任务交到一个任务队列
     * @param context 在自定义的任务中，用来获取点击事件的二进制码
     */
    public OpenActivityTask(MyActivityHandler handler,Context context){
        myHandler = handler;
        this.context = context;
        savePreference = SavePreference.getInstance(context);
    }
    public abstract void onCreateActivity(Operation operation, Intent intent);
    public abstract void onResumeActivity(Operation operation,Intent intent);
    public abstract void onDestroyActivity(Operation operation,Intent intent);
    public abstract void onDrawView(Operation operation,Intent intent);

    public void setMyHandler(MyActivityHandler myHandler) {
        this.myHandler = myHandler;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setSavePreference(SavePreference savePreference) {
        this.savePreference = savePreference;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskType() {
        return taskType;
    }

    protected boolean isAvailable(){
        if(myHandler==null||savePreference==null||context==null){
            return false;
        }
        return true;
    }
}
