package com.example.sei.deeplinkserver.util.normal;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String getHMS(){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String time = date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
        return time;
    }
}
