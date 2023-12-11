package com.baiyinliang.finance.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateStatusUtil {

    //判断选择的日期是否是今天
    public static boolean isToday(Date time) {
        return isThisTime(time, "yyyy-MM-dd");
    }

    //判断选择的日期是否是本周
    public static boolean isThisWeek(Date time) {
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        calendar.setTime(time);
        int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }

    //判断选择的日期是否是本月
    public static boolean isThisMonth(Date time) {
        return isThisTime(time, "yyyy-MM");
    }

    //判断选择的日期是否是本年
    public static boolean isThisYear(Date time) {
        return isThisTime(time, "yyyy");
    }

    //判断选择的日期是否是本季度
    public static boolean isThisQuarter(Date time) {
        Date QuarterStart = getCurrentQuarterStartTime();
        Date QuarterEnd = getCurrentQuarterEndTime();
        return time.after(QuarterStart) && time.before(QuarterEnd);
    }

    private static boolean isThisTime(Date time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String param = sdf.format(time);//参数时间
        String now = sdf.format(new Date());//当前时间
        if (param.equals(now)) {
            return true;
        }
        return false;
    }

    /**
     * 获得季度开始时间
     * @return
     */
    public static Date getCurrentQuarterStartTime() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;
        SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = null;
        try {
            if (currentMonth >= 1 && currentMonth <= 3)
                c.set(Calendar.MONTH, 0);
            else if (currentMonth >= 4 && currentMonth <= 6)
                c.set(Calendar.MONTH, 3);
            else if (currentMonth >= 7 && currentMonth <= 9)
                c.set(Calendar.MONTH, 4);
            else if (currentMonth >= 10 && currentMonth <= 12)
                c.set(Calendar.MONTH, 9);
            c.set(Calendar.DATE, 1);
            now = longSdf.parse(shortSdf.format(c.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return now;
    }

    /**
     * 当前季度的结束时间
     * @return
     */
    public static Date getCurrentQuarterEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getCurrentQuarterStartTime());
        cal.add(Calendar.MONTH, 3);
        return cal.getTime();
    }

    public static void main(String[] args) {

        try {
            System.out.println("当天：" + DateStatusUtil
                    .isToday(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-07-15 08:23:21")));
            System.out.println("本周：" + DateStatusUtil
                    .isThisWeek(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-07-25 08:23:21")));
            System.out.println("本月：" + DateStatusUtil
                    .isThisMonth(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-07-15 08:23:21")));
            System.out.println("本年：" + DateStatusUtil
                    .isThisYear(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-07-15 08:23:21")));
            System.out.println("本季度：" + DateStatusUtil
                    .isThisQuarter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-07-15 08:23:21")));

        } catch (Exception pe) {
            System.out.println(pe.getMessage());
        }

    }

}