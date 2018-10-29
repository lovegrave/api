package cn.yanss.m.cookhouse.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'+z yyyy");
    public final static SimpleDateFormat random = new SimpleDateFormat("HHmmss");

    private DateUtil() {
    }

    public static Date getCurrentTime() {
        return new Date();
    }

    public static String getStartTime() {
        return simpleDateFormat.format(new Date());
    }

    public static Date getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date DateConvert(String date) throws ParseException {
        /**
         * 将字符串改为date的格式
         */
        return sdf.parse(date);
    }

    public static String getRandom() {
        return random.format(new Date());
    }

}
