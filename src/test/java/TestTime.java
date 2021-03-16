import com.qianlima.offline.util.CommonDateUtils;
import com.qianlima.offline.util.DateUtils;

import java.util.ArrayList;
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

        List<String> daysBetwwen = DateUtils.getDaysBetwwen(7, CommonDateUtils.COMMON_DATE_STR3);
        List<String> list = new ArrayList<>();
        System.out.println(daysBetwwen);
        for (String s : daysBetwwen) {
            list.add(s.replace("-", ""));
        }
        System.out.println("最新list："+list);

        String s = daysBetwwen.get(0);
        String s1 = daysBetwwen.get(6);
        System.out.println(s+"==="+s1);

    }

}
