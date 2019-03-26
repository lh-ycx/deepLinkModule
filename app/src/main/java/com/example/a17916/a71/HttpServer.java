package com.example.a17916.a71;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.a17916.a71.manageActivity.ActivityController;
import com.example.a17916.a71.openTaskModule.UnionOpenActivityTask;
import com.example.a17916.a71.openTaskModule.UnionTaskBuilder;
import com.example.a17916.a71.receive.LocalActivityReceiver;
import com.example.a17916.a71.share.SavePreference;
import java.util.UUID;
import java.util.logging.Handler;

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
        String uri = session.getUri();
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
                    String JsonStr = savePreference.readJSONStr(randomKey);
                    JSONObject json = (JSONObject) JSON.parse(JsonStr);

                    Log.i("ycx", "intent json:" + JsonStr);

                    int size = json.size();
                    if(size == 0) {
                        return newFixedLengthResponse("not found!");
                    } else {
                        if (uri.equals("/getDeepLink")){
                            JSONArray jsonArray = (JSONArray) json.get("0");
                            String deepLink = getDeepLinkFromJson(jsonArray);
                            return newFixedLengthResponse(deepLink);
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
                    }
                }
                try{
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return newFixedLengthResponse("not found!");
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
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Sorry, Can't Found the page!");
        builder.append("</body></html>\n");
        Intent intent = new Intent();
        intent.setAction(LocalActivityReceiver.GenerateIntentData);
        mCoreService.sendBroadcast(intent);
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
                              basicType.equals("java.lang.Boolean")   ? "B." :
                              basicType.equals("java.lang.Byte")      ? "b." :
                              basicType.equals("java.lang.Character") ? "c." :
                              basicType.equals("java.lang.Double")    ? "d." :
                              basicType.equals("java.lang.Float")     ? "f." :
                              basicType.equals("java.lang.Integer")   ? "i." :
                              basicType.equals("java.lang.Long")      ? "l." :
                              basicType.equals("java.lang.Short")     ? "s." :
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

}
