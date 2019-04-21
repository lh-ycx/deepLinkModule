package com.example.sei.deeplinkserver.share;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.util.Base64;
import android.util.Log;

public class SavePreference {
    private static SavePreference savePreference;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    public static SavePreference getInstance(Context context){
        if(savePreference == null){
            savePreference = new SavePreference(context);
        }
        return savePreference;
    }
    public SavePreference(Context context){
        this.context = context;
        preferences = context.getSharedPreferences("Intent",Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    public Intent getIntent(String key){
        byte[] bytes = readRawData(key);
        Intent intent = transformToIntent(bytes);
        return intent;
    }
    public void writeJSONStr(String key,String jsonStr){
        editor.putString(key,jsonStr).commit();
    }
    public String readJSONStr(String key){
        String raw= preferences.getString(key,"");
        return raw;
    }

    public void writeIntent(String key,Intent intent){
//        Log.i("LZH","save key "+key);
        byte[] bytes = transformToBytes(intent);
        writeRawData(key,bytes);
    }
    public void writeMotionEvent(String key,byte [] bytes){
//        Log.i("LZH","save event byte key "+key);
        writeRawData(key,bytes);
    }
    public byte[] readMotionEventsByte(String key){
        return readRawData(key);
    }
    private byte[] readRawData(String key){
        String raw= preferences.getString(key,"");
        if(raw.equals("")) {
            Log.i("LZH","read intent is null");
            return null;
        }
        byte[] base64Byte = Base64.decode(raw,0);
        return base64Byte;
    }
    private void writeRawData(String key,byte[] bytes){
        String data = Base64.encodeToString(bytes,0);
        editor.putString(key,data).commit();
    }
    private Intent transformToIntent(byte[] bytes){
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes,0,bytes.length);
        parcel.setDataPosition(0);
        Intent intent = parcel.readParcelable(Intent.class.getClassLoader());
        return intent;
    }
    private byte[] transformToBytes(Intent intent){
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeParcelable(intent,0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public Boolean testKey(String key) {
        return preferences.contains(key);
    }



}
