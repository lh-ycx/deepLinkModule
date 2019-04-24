package com.example.sei.deeplinkserver.util.normal;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class IntentUtil {
    private static String[] basicType = new String[]{"java.lang.String","java.lang.Integer","java.lang.Float",
            "java.lang.Double","java.lang.Byte","java.lang.Character","java.lang.Long",
            "java.lang.Boolean"};

    public static String getValue(Bundle bundle,String targetKey){
        Set<String> keySet = bundle.keySet();
        Object o = null;
        String clazzName = null;
        for(String key:keySet){
            o = bundle.get(key);
            clazzName = o.getClass().getName();
            if(key.compareTo(targetKey)==0){

                if(isBasicType(clazzName)){
                    Log.i("LZH",key+" : "+String.valueOf(o)+" 基本类型： "+clazzName);
                    return String.valueOf(o);
                }else if(isArray(clazzName)){

                    Log.i("LZH","类型为数组: 未实现"+clazzName);
                }else if(isList(o)){
                    return listDataToString((List) o);
//                    Log.i("LZH","类型为链表: 未实现"+clazzName);
                }else if(isSerializable(o)){
                    return serializableToString((Serializable) o);
                }else if(isParcelable(o)){
                    return parcelableToString((Parcelable) o);
                }else {
                    Log.i("LZH","无法识别对应的类型: "+clazzName);
                }
            }
            if(clazzName.compareTo(Bundle.class.getName())==0){
                return getValue((Bundle) o,targetKey);
            }
        }
        return null;
    }
    public static String getType(Bundle bundle,String targetKey){
        Set<String> keySet = bundle.keySet();
        Object o = null;
        String clazzName = null;
        for(String key:keySet){
            o = bundle.get(key);
            clazzName = o.getClass().getName();
            if(key.compareTo(targetKey)==0){
                return clazzName;
            }
            if(clazzName.compareTo(Bundle.class.getName())==0){
                return getType((Bundle) o,targetKey);
            }
        }
        return null;
    }
    public static void showKeyValue(Bundle bundle){
        if(bundle==null){
            return;
        }
        Set<String> keySet = bundle.keySet();
        Object o = null;
        String clazzName = null;
        for(String key:keySet){
            o = bundle.get(key);
            clazzName = o.getClass().getName();
            Log.i("LZH","key: "+key+" ValueType "+clazzName+" value "+o);

            if(clazzName.compareTo(Bundle.class.getName())==0){
               showKeyValue((Bundle) o);
            }
        }
    }
    public static Serializable getSerializable(String targetKey,Bundle bundle){
        Set<String> keySet = bundle.keySet();
        Object o = null;
        String clazzName = null;
        for(String key:keySet){
            o = bundle.get(key);
            clazzName = o.getClass().getName();
            if(key.compareTo(targetKey)==0){
                if(isSerializable(o)){
                    return (Serializable) o;
                }
            }
            if(clazzName.compareTo(Bundle.class.getName())==0){
                return getSerializable(targetKey,(Bundle) o);
            }
        }
        return null;
    }
    public static Parcelable getParcelable(String targetKey,Bundle bundle){
        Set<String> keySet = bundle.keySet();
        Object o = null;
        String clazzName = null;
        for(String key:keySet){
            o = bundle.get(key);
            clazzName = o.getClass().getName();
            if(key.compareTo(targetKey)==0){
                if(isParcelable(o)){
                    return (Parcelable) o;
                }
            }
            if(clazzName.compareTo(Bundle.class.getName())==0){
                return getParcelable(targetKey,(Bundle) o);
            }
        }
        return null;
    }

    /**
     * 将List中的数据保存为JSON，第一个是“size”:num;以后的是下表“i”:String
     * @param list
     * @return
     */
    private static String listDataToString(List list){
        if(list.size()<=0){
            return null;
        }
        int size = list.size();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("size",size+"");
        Object o = null;
        String className = null;
        for(int i=0;i<size;i++){
            o = list.get(i);
            className = o.getClass().getName();
            if(isBasicType(className)){
                jsonObject.put("item_"+i,String.valueOf(o));
            }else if(isParcelable(o)){
                jsonObject.put("item_"+i,parcelableToString((Parcelable) o));
            }else if(isSerializable(o)){
                jsonObject.put("item_"+i,serializableToString((Serializable) o));
            }
        }
        Log.i("LZH","List Size :"+jsonObject.getInteger("size"));
        Log.i("LZH","ListType Value :"+jsonObject.toJSONString());
        Log.i("LZH", "jsonObject size:" + jsonObject.size());
        return jsonObject.toJSONString();
    }
    private static boolean isBasicType(String clazzName){
        for(String type:basicType){
            if(type.compareTo(clazzName)==0){
                return true;
            }
        }
        return false;
    }
    private static boolean isArray(String clazzName){
        if(clazzName.startsWith("[")){
            return  true;
        }
        return false;
    }
    private static boolean isList(Object o){
        if(o instanceof List){
            return true;
        }
        return false;
    }
    private static boolean isSerializable(Object o){
        if(o instanceof Serializable){
            return true;
        }
        return false;
    }
    private static boolean isParcelable(Object o){
        if(o instanceof Parcelable){
            return true;
        }
        return false;
    }
    public static String serializableToString(Serializable serializable){
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(serializable);
        return jsonObject.toJSONString();
    }
    public static String parcelableToString(Parcelable parcelable){
        byte[] bytes = parcelableToByte(parcelable);
        String value = byteToString(bytes);
        return value;
    }
    public static byte[] parcelableToByte(Parcelable parcelable){
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeParcelable(parcelable,0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }
    public static String byteToString(byte[] bytes){
        String value = Base64.encodeToString(bytes,0);
        return value;
    }
    public static byte[] StringToByte(String value){
        byte[] bytes = Base64.decode(value,0);
        return bytes;
    }
    public static Parcelable byteToParcelable(byte[] bytes,Class clazz){
        Parcel parcel = Parcel.obtain();
        if(clazz == null)
            return null;
        parcel.unmarshall(bytes,0,bytes.length);
        parcel.setDataPosition(0);
        Parcelable parcelable = parcel.readParcelable(clazz.getClassLoader());
        return parcelable;
    }


    /**
     * 临时的
     * 这是一个包含List的json,所以给他添加一个指示它Item类型的属性
     * @param jsonObject 包含List属性的JSON
     * @param bundle 包含list的bundle
     * @param targetKey  list值对应的Key
     */
    public static void addListAttributeToJSONObject(JSONObject jsonObject, Bundle bundle, String targetKey){
        Set<String> keySet = bundle.keySet();
        Object o = null;
        String clazzName = null;
        List list = null;
        Object itemObject;
        for(String key:keySet){
            if(!key.equals(targetKey)){
                continue;
            }
            o = bundle.get(key);
            clazzName = o.getClass().getName();
            if(o instanceof List){
                list = (List) o;
                if(list.size()<=0){
                    return;
                }
                itemObject = list.get(0);
                if(isSerializable(itemObject)){
                    jsonObject.put("item_type",itemObject.getClass().getName());
                    jsonObject.put("item_father_type",Serializable.class.getName());
                }else if(isParcelable(itemObject)){
                    jsonObject.put("item_father_type",Parcelable.class.getName());
                    jsonObject.put("item_type",itemObject.getClass().getName());
                }else if(itemObject instanceof Integer){
                    jsonObject.put("item_father_type",Integer.class.getName());
                    jsonObject.put("item_type",Integer.class.getName());
                }else if(itemObject instanceof String){
                    jsonObject.put("item_father_type",String.class.getName());
                    jsonObject.put("item_type",String.class.getName());
                }else if(itemObject instanceof Character){
                    jsonObject.put("item_father_type",Character.class.getName());
                    jsonObject.put("item_type",Character.class.getName());
                }else if(itemObject instanceof Long){
                    jsonObject.put("item_father_type",Long.class.getName());
                    jsonObject.put("item_type",Long.class.getName());
                }else if(itemObject instanceof Float){
                    jsonObject.put("item_father_type",Float.class.getName());
                    jsonObject.put("item_type",Float.class.getName());
                }else if(itemObject instanceof Double){
                    jsonObject.put("item_father_type",Double.class.getName());
                    jsonObject.put("item_type",Double.class.getName());
                }else if(itemObject instanceof Byte){
                    jsonObject.put("item_father_type",Byte.class.getName());
                    jsonObject.put("item_type",Byte.class.getName());
                }else if(itemObject instanceof Boolean){
                    jsonObject.put("item_father_type",Boolean.class.getName());
                    jsonObject.put("item_type",Boolean.class.getName());
                }
            }else if(clazzName.compareTo(Bundle.class.getName())==0){
                addListAttributeToJSONObject(jsonObject,(Bundle) o,targetKey);
            }
        }
    }
    public static Object getListItem(String targetKey,Bundle bundle){
        Set<String> keySet = bundle.keySet();
        List list = null;
        Object itemObject = null;
        Object o = null;
        for(String key:keySet){
            o = bundle.get(key);
            if(key.equals(targetKey)){
                list = (List) o;
                if(list==null||list.size()<=0){
                    return null;
                }else {
                    itemObject = list.get(0);
                    return itemObject;
                }
            }else if(o instanceof Bundle){
                return getListItem(targetKey, (Bundle) o);
            }
        }
        return null;
    }

}
