package com.example.a17916.a71.openTaskModule;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UnionTaskBuilder extends TaskBuilder {
    private UnionOpenActivityTask task;
    private List<StepContent> steps;
    public UnionTaskBuilder(Context context) {
        super(context);
        steps = new ArrayList<>();
    }

    @Override
    public void addStepToTask(UnionOpenActivityTask task) {

    }

    public void addStep(StepContent step){
        steps.add(step);
    }

    public void addIntentStep(Intent intent,String activityName,String appName){
        StepContent stepContent = new StepContent(StepContent.INTENT_TYPE,intent,null,null,activityName,null);
        stepContent.setAppName(appName);
        steps.add(stepContent);

    }

    public void addTextStep(String text,String activityName,String appName){
        Log.i("LZH","要输入的Text: "+text);
        StepContent stepContent = new StepContent(StepContent.INPUT_TEXT_TYPE,null,text,null,activityName,null);
        stepContent.setAppName(appName);
        steps.add(stepContent);
    }

    public void addMotionEventStep(byte[] bytes,String activityName,String appName){
        StepContent stepContent = new StepContent(StepContent.MOTION_EVENT_TYPE,null,null,bytes,activityName,null);
        stepContent.setAppName(appName);
        steps.add(stepContent);
    }

    @Override
    public UnionOpenActivityTask generateTask() {
        task = new UnionOpenActivityTask(handler);
        int size = steps.size();
        for(int i=0;i<size;i++){
            task.addStep(steps.remove(0));
        }
        return task;
    }
}
