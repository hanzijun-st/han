import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TestTimeBetween {
    public static void main(String[] args) {
        List<String> dayss = new ArrayList<>();
        Calendar start = Calendar.getInstance();

        start.setTime(getSJC());
        Long startTIme = start.getTimeInMillis();
        Calendar end = Calendar.getInstance();
        end.setTime(getSJCEnd());
        Long endTime = end.getTimeInMillis();
        Long oneDay = 1000 * 60 * 60 * 24L;
        Long time = startTIme;
        while (time < endTime) {
            Date d = new Date(time);
            dayss.add(DateFormatUtils.format(d,"yyyy-MM-dd"));
            time += oneDay;
        }
        System.out.println(dayss.toString());

        Date dateAdd = getDateAdd(-14);
        System.out.println(dateAdd);
        String format1 = DateFormatUtils.format(dateAdd,"yyyy-MM-dd HH:mm:ss");
        System.out.println(format1);

        Long time1 = getTime();
        System.out.println(time1);

        String day = DateFormatUtils.format(new Date(),"yyyy-MM-dd");
        System.out.println(day);
    }

    private static  Date getDateAdd(int days){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return c.getTime();
    }

    private static Long getTime(){
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.DAY_OF_MONTH, -14);//排除前一天
        return now.getTime().getTime();
    }

    private static  Date getSJC(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time = 1619856599000L;//5月1号
        String str = format.format(time);
        System.out.println("Format To String(Date):" + str);

        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {

        }
        return date;
    }
    private static  Date getSJCEnd(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time =1620634199000L;////5月10号
        String str = format.format(time);
        System.out.println("Format To String(Date):" + str);

        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {

        }
        return date;
    }
}