package com.example.sei.deeplinkserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.sei.deeplinkserver.ViewManager.FloatViewManager;
import com.example.sei.deeplinkserver.manageActivity.ActivityController;
import com.example.sei.deeplinkserver.manageActivity.ControllerService;
import com.example.sei.deeplinkserver.openTaskModule.UnionOpenActivityTask;
import com.example.sei.deeplinkserver.openTaskModule.UnionTaskBuilder;
import com.example.sei.deeplinkserver.share.SavePreference;


public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private CoreService mCoreService;
    private Boolean isBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        StartSendDataService();


        Intent intent = new Intent(this, CoreService.class);
        startService(intent);

        Log.i("ycx", "Service started, start to bind service");
        isBind = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isBind && serviceConnection != null) {
            unbindService(serviceConnection);
            stopService(new Intent(this, CoreService.class));
        }

        //mServerManager.stopServer();
        //mServerManager.unRegister();
    }

    public void saveIntent(View view){
        FloatViewManager floatViewManager = FloatViewManager.getInstance(this);
        floatViewManager.showSaveIntentViewBt();
    }

    //以下只针对豆瓣电影APP测试
    public void clickOpen(View view){
        String key = editText.getText().toString();

        SavePreference savePreference = SavePreference.getInstance(this.getApplicationContext());

        Intent intent = savePreference.getIntent(key);
        if(intent==null){
            Log.i("LZH","得不到JSON创建的Intent");
            return;
        }
        //找出appName
        String keys[] = key.split("/");
        String newKey = keys[0]+"/"+keys[1]+"/"+"JSON";
        String jsonStr = savePreference.readJSONStr(newKey);
        JSONArray jsonArray = JSONArray.parseArray(jsonStr);
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);

        String appName = jsonObject.getString("appName");
        Log.i("LZH","appName: "+appName);

        UnionTaskBuilder builder = new UnionTaskBuilder(this);
        builder.addIntentStep(intent,"com.douban.movie.activity.MainActivity",appName);
        UnionOpenActivityTask task = builder.generateTask();
        ActivityController controller = ActivityController.getInstance(getApplicationContext());

        String pkName = intent.getComponent().getPackageName();

        controller.addTask(pkName,null,task);

    }
    private void StartSendDataService(){
        Intent intent = new Intent(this,ControllerService.class);
        startService(intent);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("ycx", "onServiceConnected:");
            CoreService.MyBinder mBinder = (CoreService.MyBinder) service;
            mCoreService = mBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(this.getClass().getName(), "onServiceDisconnected");
        }
    };

    public CoreService getmCoreService() {
        return mCoreService;
    }
}
