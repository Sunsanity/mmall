package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by hasee on 2017/5/28.
 */
public class DateTimeUtil {

    public static final String STANDARD_FORMAR = "yyyy-MM-dd HH:mm:ss";

    public static Date strToDate(String date,String format){
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(format);
        DateTime dateTime = dateTimeFormat.parseDateTime(date);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date,String format){
        if (date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(format);
    }

    public static Date strToDate(String date){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(STANDARD_FORMAR);
        DateTime dateTime = formatter.parseDateTime(date);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date){
        if (date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAR);
    }
}
