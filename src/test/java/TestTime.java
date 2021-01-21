package test.java;

import com.qianlima.offline.util.CommonDateUtils;
import com.qianlima.offline.util.DateUtils;

import java.util.Date;

/**
 * Created by Administrator on 2021/1/12.
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

        String weekDayCnOfDate = CommonDateUtils.getWeekDayCnOfDate(new Date());
        System.out.println(weekDayCnOfDate);

        Date lastDayOfWeek = CommonDateUtils.getLastDayOfWeek(2021, 5);
        System.out.println(DateUtils.getFormatDateStr(lastDayOfWeek,"yyyy-MM-dd"));
    }

}
