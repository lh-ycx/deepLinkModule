package com.example.sei.deeplinkserver.openTaskModule;

import android.content.Context;

import com.example.sei.deeplinkserver.monitorService.MyActivityHandler;
import com.example.sei.deeplinkserver.share.SavePreference;

public abstract class TaskBuilder {
    protected Context context;
    protected SavePreference savePreference;
    protected MyActivityHandler handler;
    public TaskBuilder (Context context){
        this.context = context;
        savePreference = SavePreference.getInstance(context.getApplicationContext());
        handler = MyActivityHandler.getInstance();
    }
    public UnionOpenActivityTask generateTask(){
        UnionOpenActivityTask task = new UnionOpenActivityTask(handler);
        addStepToTask(task);
        return task;
    }
    public abstract  void addStepToTask(UnionOpenActivityTask task);
}
