import jxl.write.DateTime;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//数字相关的
public class TestNumber {
   /*public static void main(String[] args){
       TestNumber plus = new TestNumber();
       int sum = plus.sum(5);
       System.out.println(sum);
   }

   public int sum(int i){
       if (i == 1) {
           return 1;
       }
       int s = sum(i - 1);
       return i + s;
   }*/

   /* public static void main(String[] args) {
        for (int i = 1; i <= 20; i++) {
            System.out.println("第" + i + "个月的总数为:" + f(i));
        }
    }
    public static int f(int x) {
        if (x == 1 || x == 2) {
            return 1;
        } else {
            return f(x - 1) + f(x - 2);
        }
    }*/



    public static void main(String[] args) {
        //boolean thisTime = isThisTime("2021-05-01", "yyyy-MM");
        //System.out.println(thisTime);
        getFormat();
    }
    public static boolean isThisTime(String time, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(time);
            String param = DateFormatUtils.format(date,pattern);//参数时间
            String now = DateFormatUtils.format(new Date(),pattern);//当前时间
            if (param.equals(now)) {
                return true;
            }
        } catch (ParseException e) {
            e.getMessage();
        }


        return false;
    }

    public static String getFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        String format = DateFormatUtils.format(calendar.getTime(), "yyyy-MM");
        System.out.println(format);
        return format;
    }


}