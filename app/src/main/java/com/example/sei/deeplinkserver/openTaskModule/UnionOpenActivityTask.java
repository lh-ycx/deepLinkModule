package com.example.sei.deeplinkserver.openTaskModule;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sei.deeplinkserver.monitorService.MonitorActivityService;
import com.example.sei.deeplinkserver.monitorService.MyActivityHandler;
import com.example.sei.deeplinkserver.monitorService.OpenActivityTask;
import com.example.sei.deeplinkserver.monitorService.Operation;

import java.util.ArrayList;

public class UnionOpenActivityTask extends OpenActivityTask {
    private int sumStepNum;
    private ArrayList<StepContent> steps;
    private StepContent curStep;
    private String curActivityName;
    private String curAppName;
    private String requireActivityName;

    private int time = 5;

    public UnionOpenActivityTask(MyActivityHandler handler){
        super(handler,null);
        steps = new ArrayList<>();
    }

    /**
     *
     * @param handler
     * @param context 不是必须的，因为不再用其获得保存点击事件
     */
    public UnionOpenActivityTask(MyActivityHandler handler, Context context) {
        super(handler, context);
    }

    @Override
    public void onCreateActivity(Operation operation, Intent intent) {
    }

    //以下骤的背景：事先打开应用，收到命令立刻执行步骤
    //
    //
    //
    //
    //
    @Override
    public void onResumeActivity(Operation operation, Intent intent) {
        if(!isStepsAvailable()){
            Log.i("LZH","出错，无法取得任务的下一步");
            myHandler.setFinishedTask(this);
        }

        curStep = steps.get(0);
        curActivityName = intent.getStringExtra(MonitorActivityService.RESUME_ACTIVITY_NAME);
        curAppName = intent.getStringExtra(MonitorActivityService.RESUME_APP_NAME);
        requireActivityName = curStep.getActivityName();

        if(curStep.getStepType()!=StepContent.INTENT_TYPE&&!curActivityName.equals(requireActivityName)){
            return;
        }else if(curStep.getStepType()==StepContent.INTENT_TYPE&&!curAppName.equals(curStep.getAppName())){
            return;
        }

        Log.i("LZH","要打开的Activity: "+curStep.getSendIntent().getComponent().getClassName());
        executeStep(curStep,operation);
        steps.remove(0);

        if(!isStepsAvailable()){
            myHandler.setFinishedTask(this);
        }
    }

    @Override
    public void onDestroyActivity(Operation operation, Intent intent) {
    }

    @Override
    public void onDrawView(Operation operation, Intent intent) {
        if(!isStepsAvailable()){
            Log.i("LZH","出错，无法取得任务的下一步");
            myHandler.setFinishedTask(this);
        }

        Log.i("LZH","time: "+time);
        if(--time>0){
            //多次发送绘制完成广播，保证显示出搜索结果
            return;
        }
        curStep = steps.get(0);

        requireActivityName = curStep.getActivityName();

        executeStep(curStep,operation);
        steps.remove(0);

        if(!isStepsAvailable()){
            myHandler.setFinishedTask(this);
        }
    }
    public void addStep(StepContent oneStep){
        steps.add(oneStep);
        sumStepNum++;
    }
    public boolean isStepsAvailable(){
        if(steps.isEmpty()||steps.size()<=0){
            return false;
        }
        return true;
    }
    private void executeStep(StepContent step,Operation operation){
        switch (step.getStepType()){
            case StepContent.INTENT_TYPE:
                //在未打开对应应用的情况下，使用此方法打开对应的Activity
//                operation.operationStartActivity(step.getSendIntent(),requireActivityName);
                //在已经打开App的情况下，使用此方法打开对应的Activity
                operation.operationStartActivity(step.getSendIntent(),null,
                        step.getAppName());
                break;
            case StepContent.INPUT_TEXT_TYPE:
                operation.operationReplayInputEvent(step.getInputText(),requireActivityName);
                break;
            case StepContent.MOTION_EVENT_TYPE:
                operation.operationReplayMotionEvent(step.getEventBytes(),requireActivityName);
                break;
        }
    }
}
