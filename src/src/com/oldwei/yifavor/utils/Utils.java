package com.oldwei.yifavor.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String DateToString(Date date) {
        DateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        return formatter.format(date);
    }

    public static Date StringToDate(String dateString) {
        DateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
