package com.example.sei.deeplinkserver.openTaskModule;

import android.content.Intent;
import android.util.Log;

import com.example.sei.deeplinkserver.monitorService.MonitorActivityService;
import com.example.sei.deeplinkserver.monitorService.MyActivityHandler;
import com.example.sei.deeplinkserver.monitorService.OpenActivityTask;
import com.example.sei.deeplinkserver.monitorService.Operation;
import com.example.sei.deeplinkserver.util.normal.DeepLinkUtil;

public class OpenActivityByDeepLinkTask extends OpenActivityTask{
    private String deepLink;
    private String curActivityName;
    private String curPackageName;

    private int time = 5;

    public OpenActivityByDeepLinkTask(MyActivityHandler handler){
        super(handler,null);
    }

    @Override
    public void onCreateActivity(Operation operation, Intent intent) {
    }


    @Override
    public void onResumeActivity(Operation operation, Intent intent) {
        if(!DeepLinkUtil.isLegal(deepLink)){
            Log.i("ycx","illegal deep link:" + deepLink);
            myHandler.setFinishedTask(this);
        }

        curPackageName = intent.getStringExtra(MonitorActivityService.RESUME_PACKAGE_NAME);
        String tarPackageName = DeepLinkUtil.getPackageName(deepLink);


        if(!tarPackageName.equals(curPackageName)){
            return;
        }

        operation.operationStartActivityByDeepLink(deepLink,null, tarPackageName);

        myHandler.setFinishedTask(this);
    }

    @Override
    public void onDestroyActivity(Operation operation, Intent intent) {
    }

    @Override
    public void onDrawView(Operation operation, Intent intent) {

    }


    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getDeepLink() {
        return deepLink;
    }
}
