package com.example.a17916.a71.manageActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.a17916.a71.monitorService.MonitorActivityService;

import java.util.ArrayList;

public class ControllerReciver extends BroadcastReceiver {
    private MonitorActivityService.TransportBinder transportBinder = null;
    private MonitorActivityService monitorService;
    private String packageName = "";
    private String text;
    private Intent tarIntent;
    public ControllerReciver(MonitorActivityService.TransportBinder transportBinder){
        this.transportBinder = transportBinder;
        monitorService = transportBinder.getService();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case ActivityController.SEND_ACTIVITY_INTENT:
                packageName = intent.getStringExtra(ActivityController.PK_NAME);
                tarIntent = intent.getParcelableExtra(ActivityController.TARGET_INTENT);
                monitorService.openActivity(packageName,tarIntent);
                break;
            case ActivityController.OPEN_ACTIVITY:
                packageName = intent.getStringExtra(ActivityController.PK_NAME);
                monitorService.openApp(packageName);
                break;

        }
    }
}
