package com.example.sei.deeplinkserver;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.sei.deeplinkserver.manageActivity.ActivityController;
import com.example.sei.deeplinkserver.monitorService.OpenActivityTask;
import com.example.sei.deeplinkserver.openTaskModule.OpenActivityByDeepLinkTask;
import com.example.sei.deeplinkserver.openTaskModule.UnionOpenActivityTask;
import com.example.sei.deeplinkserver.openTaskModule.UnionTaskBuilder;
import com.example.sei.deeplinkserver.receive.LocalActivityReceiver;
import com.example.sei.deeplinkserver.share.SavePreference;
import com.example.sei.deeplinkserver.util.normal.DeepLinkUtil;
import com.example.sei.deeplinkserver.util.normal.IntentUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    public CoreService mCoreService;
    public SavePreference savePreference;

    public HttpServer(CoreService coreService) {
        super(8080);
        mCoreService = coreService;
        savePreference = SavePreference.getInstance(mCoreService.getApplicationContext());
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        Log.i("ycx", "method:" + method.toString());

        String uri = session.getUri();
        if(NanoHTTPD.Method.GET.equals(method)) {
            if(uri.startsWith("/getDeepLink")) {
                String randomKey = UUID.randomUUID().toString();
                Intent intent = new Intent();
                intent.setAction(LocalActivityReceiver.GenerateDeepLink);
                intent.putExtra(LocalActivityReceiver.DEEP_LINK_KEY, randomKey);
                mCoreService.sendBroadcast(intent);
                try{
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 尝试三次
                for(int i = 0; i < 3; i++) {
                    if(savePreference.testKey(randomKey)) {
                        String deepLink = savePreference.readJSONStr(randomKey);
                        Log.i("ycx", "received deep link:" + deepLink);
                        return newFixedLengthResponse(deepLink);
                        /*
                        JSONObject json = (JSONObject) JSON.parse(JsonStr);

                        Log.i("ycx", "intent json:" + JsonStr);

                        int size = json.size();
                        if(size == 0) {
                            return newFixedLengthResponse("not found!");
                        } else {
                            if (uri.equals("/getDeepLink")){
                                //JSONArray jsonArray = (JSONArray) json.get("0");
                                //String deepLink = getDeepLinkFromJson(jsonArray);
                                return newFixedLengthResponse(JsonStr);
                            } else if (uri.equals("/getDeepLinks")){
                                int current;
                                StringBuilder builder = new StringBuilder();
                                for (current = 0; current < size; current++) {
                                    JSONArray jsonArray = (JSONArray) json.get(String.valueOf(current));
                                    String deepLink = getDeepLinkFromJson(jsonArray);
                                    builder.append(deepLink + "\n");
                                }
                                return newFixedLengthResponse(builder.toString());
                            } else {
                                return newFixedLengthResponse("Bad Request!");
                            }
                        }*/
                    }
                    try{
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return newFixedLengthResponse("not found!");
            } else if(uri.startsWith("/p")) {
                String deepLink = session.getQueryParameterString();
                Log.i("ycx", "deep link:" + deepLink);
                if(!deepLink.startsWith("dl://")) {
                    return newFixedLengthResponse("illegal deep link!");
                }
                ActivityController controller = ActivityController.getInstance(mCoreService.getApplicationContext());
                OpenActivityByDeepLinkTask task = new OpenActivityByDeepLinkTask(controller.myHandler);
                task.setDeepLink(deepLink);
                task.setTaskType(OpenActivityTask.openByDeepLinkType);
                controller.addTask(DeepLinkUtil.getPackageName(deepLink),null,task);
                return newFixedLengthResponse("success");
            } else if(uri.startsWith("/j")) {
                String deepLink = session.getQueryParameterString();
                Log.i("ycx", "deep link:" + deepLink);
                if(!deepLink.startsWith("dl://")) {
                    return newFixedLengthResponse("illegal deep link!");
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
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
                        Log.i("ycx", "parameter " + String.valueOf(j) + ":" + parameter);
                        int eq = parameter.indexOf('=');
                        String key = parameter.substring(2, eq);
                        String value = parameter.substring(eq+1);
                        Log.i("ycx", "key:" + key);
                        Log.i("ycx", "value:" + value);
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
                            Log.i("ycx", "special array list!");
                            Log.i("ycx", "value length:" + value.length());
                            JSONObject jsonList = JSON.parseObject(value);
                            Log.i("ycx", "list size :" + String.valueOf(jsonList.size()));
                        }
                    }
                }


                // check intent
                Log.i("ycx", "rebuild intent:" + intent.toURI());

                // add task
                String appName = "豆瓣电影";
                Log.i("LZH","appName: "+appName);

                UnionTaskBuilder builder = new UnionTaskBuilder(mCoreService);
                builder.addIntentStep(intent,"com.douban.movie.activity.MainActivity",appName);
                UnionOpenActivityTask task = builder.generateTask();
                ActivityController controller = ActivityController.getInstance(mCoreService.getApplicationContext());

                String pkName = intent.getComponent().getPackageName();

                controller.addTask(pkName,null,task);

                return newFixedLengthResponse("success");

            }
        } else if(NanoHTTPD.Method.POST.equals(method)) {
            try  {
                HashMap<String, String> files = new HashMap<String, String>();
                session.parseBody(files);
                String deepLink = files.get("postData");
                //Log.i("ycx", "body:" + body);
                Log.i("ycx", "deep link length:" + deepLink.length());
                if(!deepLink.startsWith("dl://")) {
                    return newFixedLengthResponse("illegal deep link!");
                }
                ActivityController controller = ActivityController.getInstance(mCoreService.getApplicationContext());
                OpenActivityByDeepLinkTask task = new OpenActivityByDeepLinkTask(controller.myHandler);
                task.setDeepLink(deepLink);
                task.setTaskType(OpenActivityTask.openByDeepLinkType);
                controller.addTask(DeepLinkUtil.getPackageName(deepLink),null,task);
                return newFixedLengthResponse("success");
            } catch (IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse("catch IOException");
            } catch (ResponseException e) {
                e.printStackTrace();
                return newFixedLengthResponse("catch ResponseException");
            } catch (JSONException e) {
                e.printStackTrace();
                return newFixedLengthResponse("catch JSONException");
            }
            /*
            try {
                HashMap<String, String> files = new HashMap<String, String>();
                session.parseBody(files);
                String deepLink = files.get("postData");
                //Log.i("ycx", "body:" + body);
                Log.i("ycx", "body length:" + deepLink.length());

                if(!deepLink.startsWith("dl://")) {
                    return newFixedLengthResponse("illegal deep link!");
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
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
                        Log.i("ycx", "parameter " + String.valueOf(j) + ":" + parameter);
                        int eq = parameter.indexOf('=');
                        String key = parameter.substring(2, eq);
                        String value = parameter.substring(eq+1);
                        Log.i("ycx", "key:" + key);
                        Log.i("ycx", "value:" + value);
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
                            Log.i("ycx", "special array list!");
                            Log.i("ycx", "value length:" + value.length());
                            JSONObject jsonList = JSON.parseObject(value);
                            Log.i("ycx", "list size :" + String.valueOf(jsonList.size()));

                            String itemType = "com.douban.frodo.fangorns.model.PhotoBrowserItem";
                            String itemFatherType = "android.os.Parcelable";

                            restoreListAndFillIntent(key, value, intent, itemType);
                        }
                    }
                }

                // check intent
                Log.i("ycx", "rebuild intent:" + intent.toURI());

                // add task
                String appName = "豆瓣电影";
                Log.i("LZH","appName: "+appName);

                UnionTaskBuilder builder = new UnionTaskBuilder(mCoreService);
                builder.addIntentStep(intent,"com.douban.movie.activity.MainActivity",appName);
                UnionOpenActivityTask task = builder.generateTask();
                ActivityController controller = ActivityController.getInstance(mCoreService.getApplicationContext());

                String pkName = intent.getComponent().getPackageName();

                controller.addTask(pkName,null,task);

                return newFixedLengthResponse("success");

            } catch (IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse("catch IOException");
            } catch (ResponseException e) {
                e.printStackTrace();
                return newFixedLengthResponse("catch ResponseException");
            } catch (JSONException e) {
                e.printStackTrace();
                return newFixedLengthResponse("catch JSONException");
            }
            */
        }


        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Sorry, Can't Found the page!");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }

    public String getDeepLinkFromJson(JSONArray jsonArray) {

        StringBuilder builder = new StringBuilder();
        int length = jsonArray.size();

        builder.append("dl://");
        JSONObject metaData = jsonArray.getJSONObject(0);
        builder.append(metaData.getString("componentName"));
        builder.append("?");

        // when linker flag is true, need to add "&" before the parameter
        Boolean linkerFlag = false;
        Boolean questionMarkFlag = false;

        for(int i = 1; i < length; i++) {
            JSON tempParameter = (JSON) jsonArray.get(i);
            if(tempParameter instanceof JSONObject) {
                JSONObject parameter = (JSONObject) tempParameter;

                //check value existence
                if(!parameter.containsKey("basic_value")) continue;
                String parameterValue = parameter.getString("basic_value");
                if(parameterValue.equals("")) continue;

                //build parameter linker
                if(linkerFlag) builder.append("&");
                else linkerFlag = true;

                //build type;
                String basicType = parameter.getString("basic_type");
                String type = basicType.equals("java.lang.String")    ? "S." :
                              basicType.equals("boolean")             ? "B." :
                              basicType.equals("java.lang.Boolean")   ? "B." :
                              basicType.equals("java.lang.Byte")      ? "b." :
                              basicType.equals("java.lang.Character") ? "c." :
                              basicType.equals("java.lang.Double")    ? "d." :
                              basicType.equals("java.lang.Float")     ? "f." :
                              basicType.equals("java.lang.Integer")   ? "i." :
                              basicType.equals("java.lang.Long")      ? "l." :
                              basicType.equals("java.lang.Short")     ? "s." :
                              basicType.equals("java.util.ArrayList") ? "A." :
                                      "error";
                assert(!type.equals("error"));
                builder.append(type);

                //build parameter name
                String parameterName = parameter.getString("basic_name");
                builder.append(parameterName);
                builder.append("=");

                //build parameter value
                builder.append(parameterValue);

                questionMarkFlag = true;

            } else if(tempParameter instanceof JSONArray) {
                // need to be implemented
            }
        }

        //check if question mark is necessary
        if(!questionMarkFlag) builder.deleteCharAt(builder.length()-1);

        Log.i("ycx", "deeplink:" + builder.toString());

        return builder.toString();
    }


    public int restoreListAndFillIntent(String key, String value, Intent intent, String itemType){
        //String key = jsonObject.getString("basic_name");
        //String value = jsonObject.getString("basic_value");
        //String itemType = jsonObject.getString("item_type");//为item的确切类型，目前主要用来恢复Parcelable
        //String itemFatherType = jsonObject.getString("item_father_type");//为item的类型
        String itemFatherType = Parcelable.class.getName();
        JSONObject jsonList = JSONObject.parseObject(value);
        int size = Integer.valueOf(jsonList.getString("size"));
        Log.i("ycx","list类型的链表的大小: "+size);
        String itemValue = null;
        ArrayList<Object> list = new ArrayList<>();
        //创建位置item类型的List
        for(int i=0;i<size;i++){
            itemValue = jsonList.getString("item_"+i);
            if(itemFatherType.equals(Integer.class.getName())){
                list.add(transformToInteger(itemValue));
            }else if(itemFatherType.equals(Long.class.getName())){
                list.add(transformToLong(itemValue));
            }else if(itemFatherType.equals(Float.class.getName())){
                list.add(transformToFloat(itemValue));
            }else if(itemFatherType.equals(Double.class.getName())){
                list.add(transformToDouble(itemValue));
            }else if(itemFatherType.equals(Byte.class.getName())){
                list.add(transformToByte(itemValue));
            }else if(itemFatherType.equals(Character.class.getName())){
                list.add(transformToCharacter(itemValue));
            }else if(itemFatherType.equals(Boolean.class.getName())){
                list.add(transformToBoolean(itemValue));
            }else if(itemFatherType.equals(String.class.getName())){
                list.add(itemValue);
            }else if(itemFatherType.equals(Serializable.class.getName())){
                list.add(transformToSerializable(itemValue));
            }else if(itemFatherType.equals(Parcelable.class.getName())){

                Class clazz = null;
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try {
                    clazz = classLoader.loadClass(itemType);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(clazz == null){
                    Log.i("ycx","未能根据类名创建一个Class "+itemType);
                }
//                Object itemObject = IntentUtil.getListItem(key,bundle);
//                Log.i("LZH","item的类型："+itemObject.getClass().getName());
                list.add(transformToParcelable(itemValue,clazz));
            }
        }
        //确定list中item的确切类型
        if(itemFatherType.equals(Parcelable.class.getName())){
            ArrayList<Parcelable> parcelables = new ArrayList<>();

            for(int i=0;i<list.size();i++){
                parcelables.add((Parcelable) list.get(i));
            }
            intent.putParcelableArrayListExtra(key,parcelables);
            Log.i("ycx","outPut list: "+parcelables.toString());
            Log.i("ycx","Intent中加入ParcelableList "+key);
        }else if(itemFatherType.equals(String.class.getName())){
            ArrayList<String> strings = new ArrayList<>();
            for(int i=0;i<list.size();i++){
                strings.add((String) list.get(i));
            }
            intent.putStringArrayListExtra(key,strings);
            Log.i("ycx","outPut list: "+strings.toString());
        }else if(itemFatherType.equals(Integer.class.getName())){
            ArrayList<Integer> ints = new ArrayList<>();
            for(int i=0;i<list.size();i++){
                ints.add((Integer) list.get(i));
            }
            intent.putIntegerArrayListExtra(key,ints);
            Log.i("ycx","outPut list: "+ints.toString());
        }

        return 0;
    }

    private static Integer transformToInteger(String value){
        return Integer.valueOf(value);
    }
    private static Long transformToLong(String value){
        return Long.valueOf(value);
    }
    private static Double transformToDouble(String value){
        return Double.valueOf(value);
    }
    private static Float transformToFloat(String value){
        return Float.valueOf(value);
    }
    private static Character transformToCharacter(String value){
        if(value.length()<=0){
            Log.i("LZH","从JSON中解析char,填充Intent失败");
            return ' ';
        }
        return Character.valueOf(value.charAt(0));
    }
    private static Byte transformToByte(String value){
        return Byte.valueOf(value);
    }
    private static Boolean transformToBoolean(String value){
        return Boolean.valueOf(value);
    }
    private static Serializable transformToSerializable(String value){
        Serializable serializable = (Serializable) JSONObject.parseArray(value,Serializable.class);
        return serializable;
    }
    private static Parcelable transformToParcelable(String value,Class clazz){
        byte[] bytes = IntentUtil.StringToByte(value);
        Parcelable parcelable = IntentUtil.byteToParcelable(bytes,clazz);
        return parcelable;
    }

}


