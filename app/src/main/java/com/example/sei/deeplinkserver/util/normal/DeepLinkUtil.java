package com.example.sei.deeplinkserver.util.normal;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
            parameters = contents.substring(i+1).split("&&&");
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
            String[] parameters = contents.substring(i+1).split("&&&");
            int size = parameters.length;
            for(int j = 0; j < size; j ++) {
                String parameter = parameters[j];
                int eq = parameter.indexOf('=');
                String key = parameter.substring(2, eq);
                String value = parameter.substring(eq+1);
                if(key.equals("Action")) {
                    intent.setAction(value);
                } else if(key.equals("Flag")) {
                    intent.setFlags(Integer.valueOf(value));
                }
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
                    String[] itemStrings = parameter.substring("A.".length()).split("_#_");
                    ArrayList<Object> arrayList = new ArrayList<>();
                    for (String itemString : itemStrings) {
                        String itemValue = itemString.substring(itemString.indexOf('=')+1);
                        Log.d("ycx", "item value:" + itemValue);
                        if      (itemString.startsWith("S.")) arrayList.add(itemValue);
                        else if (itemString.startsWith("B.")) arrayList.add(Boolean.valueOf(itemValue));
                        else if (itemString.startsWith("b.")) arrayList.add(Byte.valueOf(itemValue));
                        else if (itemString.startsWith("c.")) arrayList.add(itemValue.charAt(0));
                        else if (itemString.startsWith("d.")) arrayList.add(Double.valueOf(itemValue));
                        else if (itemString.startsWith("f.")) arrayList.add(Float.valueOf(itemValue));
                        else if (itemString.startsWith("i.")) arrayList.add(Integer.valueOf(itemValue));
                        else if (itemString.startsWith("l.")) arrayList.add(Long.valueOf(itemValue));
                        else if (itemString.startsWith("s.")) arrayList.add(Short.valueOf(itemValue));
                        else if (itemString.startsWith("O.")) {
                            Log.d("ycx", "try to restore object in the array");
                            String className = itemString.split("'")[1];
                            String classValue = itemString.substring(itemString.indexOf("=")+1);
                            //Log.d("ycx", "class name:" + className);
                            //Log.d("ycx", "class value:" + classValue);
                            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                            Class clazz = null;
                            try{
                                clazz = classLoader.loadClass(className);
                            } catch (Exception e) {
                                Log.e("ycx", "can't load class:" + className);
                                e.printStackTrace();
                            }
                            if (clazz == null)
                                continue;
                            Object o = null;
                            try {
                                o = clazz.newInstance();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if(o != null && o instanceof Serializable) {
                                Log.i("ycx","find a Serializable item!!!");
                                o = JSON.parseObject(classValue, clazz);
                                Log.e("ycx", o.toString());
                                arrayList.add(o);
                            } else if (o != null && o instanceof Parcelable) {
                                Log.i("ycx","find a Parcelable item!!!");
                                o = IntentUtil.byteToParcelable(IntentUtil.StringToByte(classValue), clazz);
                                if (o!= null) arrayList.add(o);
                            }
                        }
                    }
                    intent.putExtra(key, arrayList);
                } else if(parameter.startsWith("P.")) {
                    String className = parameter.split("'")[1];
                    String classValue = parameter.substring(parameter.indexOf("=")+1);
                    //Log.d("ycx", "class name:" + className);
                    //Log.d("ycx", "class value:" + classValue);
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Class clazz = null;
                    try{
                        clazz = classLoader.loadClass(className);
                    } catch (Exception e) {
                        Log.e("ycx", "can't load class:" + className);
                        e.printStackTrace();
                    }
                    if(clazz != null) {
                        Object o = IntentUtil.byteToParcelable(IntentUtil.StringToByte(classValue), clazz);
                        int first = parameter.indexOf("'");
                        int second = parameter.indexOf("'",first + 1);
                        key = parameter.substring(second+1, eq);
                        Log.i("ycx", "new key:" + key);
                        Log.i("ycx", "class name:" + o.getClass().getName());
                        if(o != null)intent.putExtra(key, (Parcelable) o);
                    }
                } else if(parameter.startsWith("O.")) {
                    String className = parameter.split("'")[1];
                    String classValue = parameter.substring(parameter.indexOf("=")+1);
                    //Log.d("ycx", "class name:" + className);
                    //Log.d("ycx", "class value:" + classValue);
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Class clazz = null;
                    try{
                        clazz = classLoader.loadClass(className);
                    } catch (Exception e) {
                        Log.e("ycx", "can't load class:" + className);
                        e.printStackTrace();
                    }
                    if(clazz != null) {
                        Object o = JSON.parseObject(classValue, clazz);
                        Log.e("ycx", o.toString());
                        int first = parameter.indexOf("'");
                        int second = parameter.indexOf("'",first + 1);
                        key = parameter.substring(second+1, eq);
                        Log.i("ycx", "new key:" + key);
                        Log.i("ycx", "class name:" + o.getClass().getName());
                        if(o != null)intent.putExtra(key, (Serializable) o);
                    }
                }
            }
        }
        // check intent
        //Log.i("ycx", "rebuild intent:" + intent.toURI());
        return intent;
    }

    private static String toUpperCaseFirstOne(String origin) {
        StringBuffer sb = new StringBuffer(origin);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "get");
        return sb.toString();
    }
}
