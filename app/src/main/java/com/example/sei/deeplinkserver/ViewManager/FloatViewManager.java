package com.example.sei.deeplinkserver.ViewManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.example.sei.deeplinkserver.receive.LocalActivityReceiver;
import com.example.sei.deeplinkserver.view.SaveIntentView;

public class FloatViewManager {
    private SaveIntentView saveIntentView;

    private static FloatViewManager floatViewManager;
    private WindowManager.LayoutParams layoutParams;
    private Context context;
    private Activity activity;
    private WindowManager windowManager;
    public FloatViewManager(Context context){
        this.context = context;
        this.activity = (Activity) context;
    }
    public static FloatViewManager getInstance(Context context){
        if(floatViewManager == null){
            floatViewManager = new FloatViewManager(context);
        }
        return floatViewManager;
    }

    public void showSaveIntentViewBt(){
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        saveIntentView = new SaveIntentView(context);
        if(layoutParams == null){
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = saveIntentView.width;
            layoutParams.height = saveIntentView.height;
            layoutParams.gravity = Gravity.BOTTOM|Gravity.LEFT;
            if (Build.VERSION.SDK_INT > 24) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.format = PixelFormat.RGBA_8888;

            layoutParams.x = 0;
            layoutParams.y = 0;
        }

        saveIntentView.setLayoutParams(layoutParams);

        windowManager.addView(saveIntentView,layoutParams);

        saveIntentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LZH","click createBt");
                Intent intent = new Intent();
                intent.setAction(LocalActivityReceiver.GenerateIntentData);
                context.sendBroadcast(intent);

            }
        });
    }
}
