package com.example.sei.deeplinkserver.util.normal;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.sei.deeplinkserver.monitorService.SaveJSONAndIntentByIt;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AddJsonParameterUtil {
    private final String API_NAME = "apiName";
    private final String VALUE = "value";
    private final String JSON_CHILD = "json";

    private Activity activity;


    public AddJsonParameterUtil(Activity activity){
        this.activity = activity;
    }

    //只针对豆瓣电影APP测试
    public void addParameter(String activityName,String specialKey,Intent intent){
        Log.i("ycx", "original intent:" + intent.toURI());

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/douban.xml";
        List<JSONNode> lists = getJSONFromXML(activityName,path);

        JSON json = null;
        int size = 0;

        int first = 0;
        Intent reCreateIntent = null;
        JSONArray jsonArray = null;
        for(JSONNode node:lists){
            jsonArray = node.jsonArray;
            size = jsonArray.size();
            for(int i=0;i<size;i++){
                json = (JSON) jsonArray.get(i);
                if(json instanceof JSONArray){
                    addObjectParameter((JSONArray) json,intent);
                }else if(json instanceof JSONObject){
                    addBasicParameter((JSONObject) json,intent);
                }
            }

            addIntentUriToJSONArray(jsonArray,intent);

            sendAndSaveJson(node.apiName,specialKey,jsonArray);


            reCreateIntent = recreateIntent(jsonArray);
            if(first==0){
                IntentUtil.showKeyValue(reCreateIntent.getExtras());
                first++;
            }
            sendAndSaveIntent(node.apiName,specialKey,reCreateIntent);
        }

    }
    //在JSON中添加一些必要信息，如IntentUri 应用的名称
    private void addIntentUriToJSONArray(JSONArray jsonArray, Intent intent){
        String intentUri = intent.toUri(0);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("intentUri",intentUri);

        String appName = AppUtil.getAppName(activity);
        jsonObject.put("appName",appName);
        jsonArray.add(0,jsonObject);
    }
    //测试
    private void sendAndSaveJson(String apiName,String specialKey,JSONArray jsonArray){
        Intent broadIntent = new Intent();
        broadIntent.setAction(SaveJSONAndIntentByIt.SAVE_JSON);
        String jsonData = jsonArray.toJSONString();
        String key = apiName+"/"+specialKey+"/"+"JSON";
        broadIntent.putExtra(SaveJSONAndIntentByIt.KEY,key);
        broadIntent.putExtra(SaveJSONAndIntentByIt.JSON_DATA,jsonData);
        activity.sendBroadcast(broadIntent);
    }
    //测试
    private Intent recreateIntent(JSONArray jsonArray){
        Intent intent = new Intent();
        JSON json = null;
        int size = jsonArray.size();


        JSONObject headJson = jsonArray.getJSONObject(0);
        String intentUri = headJson.getString("intentUri");
        try {
            //intent = Intent.parseUri(intentUri,0);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(ComponentName.unflattenFromString("com.douban.movie/com.douban.frodo.subject.activity.LegacySubjectActivity"));
            //Log.i("ycx", "origin:"+intent.toURI());
        } catch (Exception e) {
            Log.i("ycx", e.toString());
            e.printStackTrace();
        }
        if(intent==null){
            Log.i("LZH","初始化Intent失败");
            return null;
        }

        for(int i=1;i<size;i++){
            json = (JSON) jsonArray.get(i);
            if(json instanceof JSONObject){
                addBasicToIntent((JSONObject) json,intent);
            }else if(json instanceof JSONArray){
                addObjectToIntent((JSONArray) json,intent);
            }
        }

        //Log.i("ycx", "new:"+intent.toURI());

        return intent;
    }
    //测试
    private void sendAndSaveIntent(String apiName,String specialKey,Intent intent){
        Intent broadIntent = new Intent();
        broadIntent.setAction(SaveJSONAndIntentByIt.SAVE_INTENT_CREATEBY_JSON);

        String key = apiName+"/"+specialKey+"/"+"Intent";
        broadIntent.putExtra(SaveJSONAndIntentByIt.KEY,key);
        broadIntent.putExtra(SaveJSONAndIntentByIt.INTENT_DATA,intent);
        if(intent == null){
            Log.i("LZH","不能创建Intent");
            return;
        }
        activity.sendBroadcast(broadIntent);
    }
    //测试
    private void addBasicToIntent(JSONObject jsonObject, Intent intent){
        String key = jsonObject.getString("basic_name");
        String type = jsonObject.getString("basic_type");
        String value = jsonObject.getString("basic_value");
        if(value!=null){
            if(type.equals("java.lang.String")){
                intent.putExtra(key,value);
            }else if(type.equals("java.lang.Float")){
                intent.putExtra(key,Float.valueOf(value));
            }else if(type.equals("java.lang.Double")){
                intent.putExtra(key,Double.valueOf(value));
            }else if(type.equals("java.lang.Byte")){
                intent.putExtra(key,Byte.valueOf(value));
            }else if(type.equals("java.lang.Character")){
                intent.putExtra(key,value.charAt(0));
            }else if(type.equals("java.lang.Long")){
                intent.putExtra(key,Long.valueOf(value));
            }else if(type.equals("java.lang.Boolean")){
                intent.putExtra(key,Boolean.valueOf(value));
            }else if(type.endsWith("List")){
                restoreListAndFillIntent(jsonObject,intent);
            }else if(type.startsWith("[")){
                Log.i("LZH","未将数组还原");
            }
        }
    }

    /**
     * 获取由List转化的String类型的值，将其还原，并填充到Intent中
     * @param jsonObject
     * @param intent
     */
    private void restoreListAndFillIntent(JSONObject jsonObject, Intent intent){
        Log.i("ycx", "======================");

        String key = jsonObject.getString("basic_name");
        String value = jsonObject.getString("basic_value");
        String itemType = jsonObject.getString("item_type");//为item的确切类型，目前主要用来恢复Parcelable
        String itemFatherType = jsonObject.getString("item_father_type");//为item的类型
        JSONObject jsonList = JSONObject.parseObject(value);

        Log.i("ycx", "itemType:" + itemType);
        Log.i("ycx", "itemFatherType:" + itemFatherType);
        Log.i("ycx", "======================");

        int size = Integer.valueOf(jsonList.getString("size"));
        Log.i("LZH","list类型的链表的大小: "+size);
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
                    Log.i("LZH","未能根据类名创建一个Class "+itemType);
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
            Log.i("LZH","outPut list: "+parcelables.toString());
            Log.i("LZH","Intent中加入ParcelableList "+key);
        }else if(itemFatherType.equals(String.class.getName())){
            ArrayList<String> strings = new ArrayList<>();
            for(int i=0;i<list.size();i++){
                strings.add((String) list.get(i));
            }
            intent.putStringArrayListExtra(key,strings);
            Log.i("LZH","outPut list: "+strings.toString());
        }else if(itemFatherType.equals(Integer.class.getName())){
            ArrayList<Integer> ints = new ArrayList<>();
            for(int i=0;i<list.size();i++){
                ints.add((Integer) list.get(i));
            }
            intent.putIntegerArrayListExtra(key,ints);
            Log.i("LZH","outPut list: "+ints.toString());
        }

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


    //测试
    private void addObjectToIntent(JSONArray jsonArray, Intent intent){
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);

        String className = jsonObject.getString("object_type");
        String key = jsonObject.getString("object_name");
        String fatherType = jsonObject.getString("objectFather_type");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        ClassLoader classLoader = null;

        Class clazz = null;
        int size = jsonArray.size();
        JSONObject attrJson = null;
        Object parameter = null;
        try {
            clazz = classLoader.loadClass(className);
            if(clazz==null){
                Log.i("LZH","重新创建Intent时，无法创建对象参数");
            }
            parameter = clazz.newInstance();
            Field field = null;
            String attrName = null,value = null,type = null;
            for(int i=1;i<size;i++){
                attrJson = (JSONObject) jsonArray.get(i);
                attrName = attrJson.getString("attribute_name");
                value = attrJson.getString("attribute_value");
                type = attrJson.getString("attribute_type");
                field = clazz.getField(attrName);
                field.setAccessible(true);
                fillAttributeToObject(field,parameter,value,type);
            }
            if(fatherType.compareTo(Serializable.class.getName())==0){
                intent.putExtra(key,(Serializable)parameter);
            }else if(fatherType.compareTo(Parcelable.class.getName())==0){
                intent.putExtra(key,(Parcelable)parameter);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    //测试
    private Object fillAttributeToObject(Field field,Object o,String value,String type){
        if(value==null||value.length()<=0){
            return null;
        }
        if(type.equals("java.lang.String")){
            return value;
        }else if(type.equals("java.lang.Float")){
            return Float.valueOf(value);
        }else if(type.equals("java.lang.Double")){
            return Double.valueOf(value);
        }else if(type.equals("java.lang.Byte")){
            return Byte.valueOf(value);
        }else if(type.equals("java.lang.Character")){
            return value.charAt(0);
        }else if(type.equals("java.lang.Long")){
            return Long.valueOf(value);
        }else if(type.equals("java.lang.Boolean")){
            return Boolean.valueOf(value);
        }else if(type.equals("int")) {
            return Integer.valueOf(value);
        }
        Log.i("LZH","重构Intent时，无法识别基础类型："+type);
        return null;
    }

    /**
     * 从文件中读取json，并对当前页面的API 进行复制，然后返回复制的结果
     * @param path
     * @return
     */
    private List<JSONNode> getJSONFromXML(String activityName,String path){
        ArrayList<JSONNode> list = new ArrayList<>();
        File file = new File(path);
        if(!file.exists()){
            Log.i("LZH","找不到要读取的XML文件："+path);
            return list;
        }
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document dom = null;
        try {
            dom = builder.parse(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        if(dom==null){
            Log.i("LZH","无法解析xml文件");
            return list;
        }
        NodeList nodeList = dom.getElementsByTagName(API_NAME);
        int size = nodeList.getLength();
        Node node = null;
        JSONNode jsonNode = null;
        for(int i=0;i<size;i++){
            node = nodeList.item(i);
            jsonNode = createJSONNodeByXMLNode(node);
            if(jsonNode!=null&&jsonNode.apiName.startsWith(activityName)){
                list.add(jsonNode);
            }
        }

        return list;
    }

    private JSONObject addBasicParameter(JSONObject targetJsonObject, Intent intent){
        String key = targetJsonObject.getString("basic_name");
        String type = targetJsonObject.getString("basic_type");
        if(type.endsWith("List")){
            IntentUtil.addListAttributeToJSONObject(targetJsonObject,intent.getExtras(),key);
        }
        if(intent.getExtras() != null) {
            String value = IntentUtil.getValue(intent.getExtras(),key);
            //Log.i("ycx",key+" : "+value);
            //如果value的值为null，json中将不会有basic_value;
            targetJsonObject.put("basic_value",value);
        }
        return targetJsonObject;
    }

    /**
     * 假设对象中的属性为基础属性
     * @param targetJsonArray 需要填充属性的对象的JSON
     * @param intent
     * @return
     */
    private JSONArray addObjectParameter(JSONArray targetJsonArray, Intent intent){
        JSONObject objectJSON = (JSONObject)targetJsonArray.get(0);
        int size = targetJsonArray.size();
        String key = objectJSON.getString("object_name");
        //Log.i("LZH", "key:" + key);
        String value = IntentUtil.getValue(intent.getExtras(),key);
        if(value==null){
            Log.i("LZH","未找到："+key);
            return targetJsonArray;
        }
        String type = IntentUtil.getType(intent.getExtras(),key);
        objectJSON.put("objectFather_type",type);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        ClassLoader classLoader = null;

        Class clazz = null;
        JSONObject attrJson = null;
        if(type.compareTo(Serializable.class.getName())==0) {
            Serializable serializable = IntentUtil.getSerializable(key,intent.getExtras());
            try {
                clazz = classLoader.loadClass(type);
                Field field = null;
                String attrName = null;
                for(int i=1;i<size;i++){
                    attrJson = targetJsonArray.getJSONObject(i);
                    attrName = attrJson.getString("attribute_name");
                    field = clazz.getField(attrName);
                    field.setAccessible(true);
                    Object o = field.get(serializable);
                    attrJson.put("attribute_value",String.valueOf(o));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }else if(type.compareTo(Parcelable.class.getName())==0){
            Parcelable parcelable = IntentUtil.getParcelable(key,intent.getExtras());
            try {
                clazz = classLoader.loadClass(type);
                Field field = null;
                String attrName = null;
                for(int i=1;i<size;i++){
                    attrJson = targetJsonArray.getJSONObject(i);
                    attrName = attrJson.getString("attribute_name");
                    field = clazz.getField(attrName);
                    field.setAccessible(true);
                    Object o = field.get(parcelable);
                    attrJson.put("attribute_value",String.valueOf(o));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return targetJsonArray;
    }

    private JSONNode createJSONNodeByXMLNode(Node node){
        JSONNode jsonNode = null;
        Element element = (Element) node;
        String apiName = element.getAttribute(VALUE);
        NodeList lists = element.getElementsByTagName(JSON_CHILD);
        Element jsonChild = (Element) lists.item(0);
        String jsonArrayStr = jsonChild.getTextContent();
        if(jsonArrayStr.compareTo("[]")==0){
            jsonNode = new JSONNode(apiName,new JSONArray());
        }else{
//            Log.i("LZH","jsonArrayStr: "+jsonArrayStr);
            JSONArray jsonArray = JSONArray.parseArray(jsonArrayStr);
            if(jsonArray==null){
                Log.i("LZH",apiName+" : JSONArray解析失败");
                return null;
            }
            jsonNode = new JSONNode(apiName,jsonArray);
        }
        return jsonNode;
    }


    static class JSONNode{
        public String apiName;
        public JSONArray jsonArray;
        public JSONNode(){

        }
        public JSONNode(String apiName,JSONArray jsonArray){
            this.apiName = apiName;
            this.jsonArray = jsonArray;
        }
    }


    public void generateDeepLink(String activityName,String specialKey,Intent intent){
        Log.i("ycx", "====================");
        Bundle bundle = intent.getExtras();
        Set<String> keys = bundle.keySet();
        for(String key: keys){
            Log.i("ycx", "key name:" + key);
            Object o = bundle.get(key);
            if(o instanceof Parcelable) {
                Log.i("ycx", "it's a Parcelable object");
                Log.i("ycx", "class name:" + o.getClass().getName());
                try {
                    Class clazz =o.getClass();
                    Field[] fields = clazz.getDeclaredFields();
                    for(Field field: fields) {
                        String fieldName = field.getName();
                        Log.i("ycx", "field name:" + fieldName);
                        String fieldType = field.getType().getName();
                        Log.i("ycx", "field type:" + fieldType);
                        try {
                            String methodName = toUpperCaseFirstOne(fieldName);
                            Method method = clazz.getMethod(methodName);
                            method.setAccessible(true);
                            Object value = method.invoke(o);
                            Log.i("ycx", "field value:" + value.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(o instanceof Serializable) {
                Log.i("ycx","it's a serializable object");
                String className = o.getClass().getName();
                Log.i("ycx", "class name:" + className);
                String value = o.toString();
                String prefix = className.equals("java.lang.String")    ? "S." :
                                className.equals("java.lang.Boolean")   ? "B." :
                                className.equals("java.lang.Byte")      ? "b." :
                                className.equals("java.lang.Character") ? "c." :
                                className.equals("java.lang.Double")    ? "d." :
                                className.equals("java.lang.Float")     ? "f." :
                                className.equals("java.lang.Integer")   ? "i." :
                                className.equals("java.lang.Long")      ? "l." :
                                className.equals("java.lang.Short")     ? "s." :
                                className.equals("java.util.ArrayList") ? "A." :
                                "O.";
                Log.i("ycx", prefix+value);
            } else {
                Log.wtf("ycx", "Warning! it's an unknown type");
                Log.wtf("ycx", "class name:" + o.getClass().getName());
            }


        }
        Log.i("ycx", "====================");


        Log.i("ycx", "original intent:" + intent.toURI());

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/org.wikipedia.alpha.xml";
        Log.i("ycx", "path:" + path);
        List<JSONNode> lists = getJSONFromXML(activityName,path);

        JSON json = null;
        int size = 0;

        int cnt = 0;
        JSONArray jsonArray = null;
        JSONObject result = new JSONObject();
        JSONObject metaData = new JSONObject();
        Uri data = intent.getData();
        String action = intent.getAction();
        Log.i("ycx", "action:" + action);
        String componentName = intent.getComponent().flattenToString();
        Log.i("ycx", "componentName:" + componentName);
        if (data != null) {
            metaData.put("data", data.toString());
        }
        if (action != null) {
            metaData.put("action", action);
        }
        if (componentName != null) {
            metaData.put("componentName", componentName);
        }
        //Log.i("ycx", metaData.toJSONString());
        Log.i("ycx", "start adding parameter!");
        for(JSONNode node:lists){
            jsonArray = node.jsonArray;
            size = jsonArray.size();
            for(int i=0;i<size;i++){
                json = (JSON) jsonArray.get(i);
                if(json instanceof JSONArray){
                    //Log.i("ycx", "before addObjectParameter");
                    addObjectParameter((JSONArray) json,intent);
                    //Log.i("ycx", "after addObjectParameter");
                }else if(json instanceof JSONObject){
                    //Log.i("ycx", "before addBasicParameter");
                    addBasicParameter((JSONObject) json,intent);
                    //Log.i("ycx", "after addBasicParameter");
                }
            }
            //addIntentUriToJSONArray(jsonArray,intent);
            jsonArray.add(0, metaData.clone());
            result.put(String.valueOf(cnt++), jsonArray);
        }
        Log.i("ycx", "finish adding parameter!");
        //Log.i("ycx", "result:" + result.toJSONString());
        Log.i("ycx", "result size:" + result.size());
        Log.i("ycx", "length:" + result.toJSONString().length());
        if(result.toJSONString().length() > 500000) {
            Log.i("ycx", "result is too long, need to be trimmed");
            for(int i = 1; i < cnt; i++) {
                result.remove(String.valueOf(i));
            }
            Log.i("ycx", "after trimming");
            Log.i("ycx", "result size:" + result.size());
            Log.i("ycx", "length:" + result.toJSONString().length());
        }


        Intent broadIntent = new Intent();
        broadIntent.setAction(SaveJSONAndIntentByIt.SAVE_JSON);
        broadIntent.putExtra(SaveJSONAndIntentByIt.KEY,specialKey);
        broadIntent.putExtra(SaveJSONAndIntentByIt.JSON_DATA,result.toJSONString());
        activity.sendBroadcast(broadIntent);
        Log.i("ycx", "finish generateDeepLink");
    }

    private static String toUpperCaseFirstOne(String origin) {
        StringBuffer sb = new StringBuffer(origin);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "get");
        return sb.toString();
    }
}
