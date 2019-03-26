package com.example.a17916.a71.manageActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.a17916.a71.monitorService.MonitorActivityService;

public class ControllerService extends Service {
    private MonitorActivityService.TransportBinder  transportBinder = null;
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this,MonitorActivityService.class);
        bindService(intent,connection,Context.BIND_AUTO_CREATE);

    }
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            transportBinder = (MonitorActivityService.TransportBinder) service;
            ControllerReciver reciver = new ControllerReciver(transportBinder);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ActivityController.SEND_INTENT_MOTION);
            intentFilter.addAction(ActivityController.SEND_ACTIVITY_INTENT);
            intentFilter.addAction(ActivityController.OPEN_ACTIVITY);
            intentFilter.addAction(ActivityController.OPEN_ANALYSE_ACTIVITY);
            registerReceiver(reciver,intentFilter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
