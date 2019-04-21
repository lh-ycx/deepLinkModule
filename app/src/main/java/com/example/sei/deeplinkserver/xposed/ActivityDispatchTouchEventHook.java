package com.example.sei.deeplinkserver.xposed;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcel;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.sei.deeplinkserver.monitorService.MonitorActivityService;

import java.util.ArrayList;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;

public class ActivityDispatchTouchEventHook extends XC_MethodHook {
    private HashMap<String ,Integer> hashMap = new HashMap<String,Integer>();
    private int time = 0;
    private String lastActivityName = "";
    private ArrayList<MotionEvent> list = new ArrayList<MotionEvent>();
    private String key = "";
    private MotionEvent motionEvent;
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//        super.beforeHookedMethod(param);
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        boolean hasRun = (boolean) param.getResult();
        Activity activity = (Activity) param.thisObject;
        ComponentName componentName = activity.getComponentName();
        Intent intent = new Intent();
        motionEvent = MotionEvent.obtain((MotionEvent) param.args[0]);
        intent.setAction(MonitorActivityService.SAVE_MOTION_EVENT);
        intent.putExtra(MonitorActivityService.MOTION_EVENT,transformToBytes(motionEvent));
        intent.putExtra(MonitorActivityService.EVENT_ACTIVITY,componentName.getClassName());
        activity.sendBroadcast(intent);

//        needSendText(activity);
    }

    /**
     * 发送当前页面EditText中的文字
     * @param activity
     */
    private void needSendText(Activity activity) {
        View decor = activity.getWindow().getDecorView();
        EditText editText = null;
        editText = findEditText(decor);
        if(editText!=null){
            String text = editText.getText().toString();
            Intent intent = new Intent();
            intent.setAction(MonitorActivityService.SAVE_EDIT_TEXT);
            intent.putExtra(MonitorActivityService.EDIT_TEXT,text);
            activity.sendBroadcast(intent);

        }
    }


    private byte[] transformToBytes(MotionEvent event){
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeParcelable(event,0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    private EditText findEditText(View view){
        ArrayList<View> list = new ArrayList<>();
        list.add(view);
        View cur;
        ViewGroup viewGroup;
        while (!list.isEmpty()){
            cur = list.remove(0);
            if(cur instanceof ViewGroup){
                viewGroup = (ViewGroup) cur;
                for(int i=0;i<viewGroup.getChildCount();i++){
                    list.add(viewGroup.getChildAt(i));
                }
            }else if(cur instanceof EditText){
                return (EditText) cur;
            }
        }
        return null;
    }
}
