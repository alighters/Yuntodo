package com.oldwei.yifavor.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * 手机号验证
     * 
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }
}
