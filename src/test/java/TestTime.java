import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 对时间进行处理
 */
public class TestTime {
    public static void main(String[] args) {
        /*Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(d);


        Map<String,Object> map = new HashMap<>();
        map.put("time", DateUtils.parseDateFromDateStr(format));

       Date date = (Date) map.get("time");

        String formatDateStr = DateUtils.getFormatDateStr(date);
        System.out.println("时间格式化："+formatDateStr);*/

       /* String weekDayCnOfDate = CommonDateUtils.getWeekDayCnOfDate(new Date());
        System.out.println(weekDayCnOfDate);

        Date lastDayOfWeek = CommonDateUtils.getLastDayOfWeek(2021, 5);
        System.out.println(DateUtils.getFormatDateStr(lastDayOfWeek,"yyyy-MM-dd"));*/

       /* List<String> daysBetwwen = DateUtils.selectNewBetween(18);
        List<String> list = new ArrayList<>();
        System.out.println(daysBetwwen);
        for (String s : daysBetwwen) {
            list.add(s.replace("-", ""));
        }
        System.out.println("最新list："+list);*/
        /*long t1=1616120269889L;
        long t2=1616118529000L;
        System.out.println((t1 - t2) /(60*60*1000));

        Long yesterdayBeginTime = DateUtils.getYesterdayBeginTime();
        Long yesterdayEndTime = DateUtils.getYesterdayEndTime();
        System.out.println(yesterdayBeginTime+"-"+yesterdayEndTime);


        System.out.println(DateUtils.getDayStartTime());
        System.out.println(DateUtils.getDayEndTime());*/

       /* Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        todayStart.add(Calendar.DAY_OF_MONTH, -1);*/


       /* Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        todayEnd.add(Calendar.DAY_OF_MONTH, -1);*/


        /*Date time = todayStart.getTime();
        //Date time2 = todayEnd.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(time);
        //String format2 = sdf.format(time2);

        System.out.println(format);*/
        //System.out.println(format2);
        //long time = todayStart.getTime().getTime();
        //System.out.println(time);

        /*Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH),0,0,0);
        System.out.println(c.getTime().getTime());*/

       /* List<String> dayss = new ArrayList<>();
        Calendar start = Calendar.getInstance();
        start.setTime(getDateAdd(14));
        Long startTIme = start.getTimeInMillis();
        Calendar end = Calendar.getInstance();
        end.setTime(new Date());
        Long endTime = end.getTimeInMillis();
        Long oneDay = 1000 * 60 * 60 * 24l;
        Long time = startTIme;
        while (time < endTime) {
            Date d = new Date(time);
            dayss.add(DateFormatUtils.format(d,"yyyy-MM-dd"));
            time += oneDay;
        }
        System.out.println(dayss);*/

       /* List<String> dayss = new ArrayList<>();
        Calendar start = Calendar.getInstance();
        start.setTime(getDateAdd(14));
        Long startTIme = start.getTimeInMillis();
        Calendar end = Calendar.getInstance();
        end.setTime(new Date());
        Long endTime = end.getTimeInMillis();
        Long oneDay = 1000 * 60 * 60 * 24l;
        Long time = startTIme;
        while (time < endTime) {
            dayss.add(String.valueOf(time));
            time += oneDay;
        }

        System.out.println(dayss);*/





    }
    private static  Date getDateAdd(int days){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return c.getTime();
    }


}
