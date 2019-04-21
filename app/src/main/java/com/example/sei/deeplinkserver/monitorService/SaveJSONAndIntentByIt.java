package com.example.sei.deeplinkserver.monitorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sei.deeplinkserver.share.SavePreference;

public class SaveJSONAndIntentByIt extends BroadcastReceiver {
    public static final String SAVE_JSON = "SAVE_JSON";
    public static final String SAVE_INTENT_CREATEBY_JSON = "SAVE_INTENT_CREATEBY_JSON";
    public static final String KEY = "KEY";
    public static final String INTENT_DATA = "INTENT_DATA";
    public static final String JSON_DATA = "JSON_DATA";

    private Context context;
    private SavePreference savePreference;
    private String key;
    private Intent intentToSave;
    private String JsonStr;
    public SaveJSONAndIntentByIt(Context context){
        this.context = context;
        savePreference = new SavePreference(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case SaveJSONAndIntentByIt.SAVE_INTENT_CREATEBY_JSON:
                key = intent.getStringExtra(SaveJSONAndIntentByIt.KEY);
                intentToSave = intent.getParcelableExtra(SaveJSONAndIntentByIt.INTENT_DATA);
                Log.i("LZH","保存Intent key: "+key);
                if(intentToSave==null){
                    Log.i("LZH","保存Intent 为null");
                }
                savePreference.writeIntent(key,intentToSave);
                break;
            case SaveJSONAndIntentByIt.SAVE_JSON:
                key = intent.getStringExtra(SaveJSONAndIntentByIt.KEY);
                JsonStr = intent.getStringExtra(SaveJSONAndIntentByIt.JSON_DATA);
                Log.i("LZH","保存JSON key: "+key+" JsonStr: "+JsonStr);
                savePreference.writeJSONStr(key,JsonStr);
                break;
        }
    }

}
