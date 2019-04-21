package com.example.sei.deeplinkserver.monitorService;

import android.content.Intent;

import java.util.ArrayList;

public class MyActivityHandler {
    public static MyActivityHandler myHandler;
    private ArrayList<OpenActivityTask> tasks;
    private ArrayList<OpenActivityTask> finishedTask;
    public static MyActivityHandler getInstance(){
        if(myHandler==null){
            myHandler = new MyActivityHandler();
        }
        return myHandler;
    }
    public MyActivityHandler(){
        tasks = new ArrayList<>();
        finishedTask = new ArrayList<>();
    }

    public void onCreateActivity(Operation operation, Intent intent){
        for(OpenActivityTask task:tasks){
            task.onCreateActivity(operation,intent);
        }
        release();
    }
    public void onResumeActivity(Operation operation,Intent intent){
        for (OpenActivityTask task:tasks){
            task.onResumeActivity(operation, intent);
        }
        release();
    }
    public void onDestroyActivity(Operation operation,Intent intent){
        for(OpenActivityTask task:tasks){
            task.onDestroyActivity(operation, intent);
        }
        release();
    }

    public void onDrawView(Operation operation,Intent intent){
        for(OpenActivityTask task:tasks){
            task.onDrawView(operation, intent);
        }
        release();
    }
    //移除之前添加的相同的任务，防止发生错误
    public void addTask(OpenActivityTask task){
        String taskType = task.getTaskType();

        int len = tasks.size();
        for(int i=0;i<len;i++){
            if(tasks.get(i).getTaskType()==null||tasks.get(i).getTaskType().equals(taskType)){
                tasks.remove(i);
                i--;
                len--;
            }
        }
        tasks.add(task);
    }

    public synchronized void release(){
        for(OpenActivityTask finished:finishedTask){
            for(int i=0;i<tasks.size();i++){
                if(tasks.get(i)==finished){
                    tasks.remove(i);
                    break;
                }
            }
        }
    }
    public void setFinishedTask(OpenActivityTask task){
        finishedTask.add(task);
    }
}
