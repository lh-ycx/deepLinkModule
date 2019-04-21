package com.example.sei.deeplinkserver.util.normal;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class DeepLinkUtil {
    public static Boolean isLegal(String deepLink) {
        return deepLink.startsWith("dl://") && deepLink.length()>"dl://".length();
    }

    public static String getComponentName(String deepLink) {
        String contents = deepLink.substring("dl://".length());

        // build component name
        String componentName = null;
        int i = contents.indexOf('?');
        if (i == -1) componentName = contents;
        else componentName = contents.substring(0,i);
        return componentName;
    }

    public static String getPackageName(String deepLink) {
        String componentName = getComponentName(deepLink);
        return componentName.split("/")[0];
    }

    public static String getActivityName(String deepLink) {
        String componentName = getComponentName(deepLink);
        return componentName.split("/")[1];
    }

    public static String[] getParameters(String deepLink) {
        String contents = deepLink.substring("dl://".length());
        int i = contents.indexOf('?');
        String[] parameters = null;
        if(i != -1)
            parameters = contents.substring(i+1).split("&");
        return parameters;
    }

    public static Intent buildIntentFromDeepLink(String deepLink) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Log.i("ycx", "deep link:" + deepLink);
        if(!deepLink.startsWith("dl://")) {
            Log.i("ycx","illegal deep link!");
            return intent;
        }
        String contents = deepLink.substring("dl://".length());

        // build component name
        String componentName = null;
        int i = contents.indexOf('?');
        if (i == -1) componentName = contents;
        else componentName = contents.substring(0,i);
        Log.i("ycx", "componentName:" + componentName);
        intent.setComponent(ComponentName.unflattenFromString(componentName));

        // build parameters
        if(i != -1) {
            String[] parameters = contents.substring(i+1).split("&");
            int size = parameters.length;
            for(int j = 0; j < size; j ++) {
                String parameter = parameters[j];
                //Log.i("ycx", "parameter " + String.valueOf(j) + ":" + parameter);
                int eq = parameter.indexOf('=');
                String key = parameter.substring(2, eq);
                String value = parameter.substring(eq+1);
                //Log.i("ycx", "key:" + key);
                //Log.i("ycx", "value:" + value);
                if      (parameter.startsWith("S.")) intent.putExtra(key, value);
                else if (parameter.startsWith("B.")) intent.putExtra(key, Boolean.parseBoolean(value));
                else if (parameter.startsWith("b.")) intent.putExtra(key, Byte.parseByte(value));
                else if (parameter.startsWith("c.")) intent.putExtra(key, value.charAt(0));
                else if (parameter.startsWith("d.")) intent.putExtra(key, Double.parseDouble(value));
                else if (parameter.startsWith("f.")) intent.putExtra(key, Float.parseFloat(value));
                else if (parameter.startsWith("i.")) intent.putExtra(key, Integer.parseInt(value));
                else if (parameter.startsWith("l.")) intent.putExtra(key, Long.parseLong(value));
                else if (parameter.startsWith("s.")) intent.putExtra(key, Short.parseShort(value));
                else if (parameter.startsWith("A.")) {
                    //Log.i("ycx", "special array list!");
                    //Log.i("ycx", "value length:" + value.length());
                    JSONObject jsonList = JSON.parseObject(value);
                    //Log.i("ycx", "list size :" + String.valueOf(jsonList.size()));
                }
            }
        }
        // check intent
        //Log.i("ycx", "rebuild intent:" + intent.toURI());
        return intent;
    }
}
