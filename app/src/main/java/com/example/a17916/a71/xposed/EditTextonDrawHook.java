package com.example.a17916.a71.xposed;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.example.a17916.a71.monitorService.MonitorActivityService;

import de.robv.android.xposed.XC_MethodHook;

public class EditTextonDrawHook extends XC_MethodHook {
    private int time = 0;
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//        super.beforeHookedMethod(param);
    }

    /**
     * 广播告知当前页面是否已经完成绘制
     * @param param
     * @throws Throwable
     */
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//        super.afterHookedMethod(param);
        View view = (View) param.thisObject;
        Context context = view.getContext();

        if(view instanceof EditText){
            time++;
//            Log.i("LZH","editText onDraw");
        }else{
            time=0;
//            Log.i("LZH","other onDraw");
        }
        if(time<3){
            return;
        }
        Intent intent = new Intent();
        intent.setAction(MonitorActivityService.ON_DRAW);
        intent.putExtra(MonitorActivityService.VIEW_TYPE,MonitorActivityService.EDITTEXT);

        context.sendBroadcast(intent);
        time = 0;
    }
}
