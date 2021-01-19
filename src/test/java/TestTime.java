import com.qianlima.offline.util.DateUtils;
import com.qianlima.offline.util.JsonUtil;
import com.qianlima.offline.util.MapUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */
public class TestTime {
    public static void main(String[] args) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(d);


        Map<String,Object> map = new HashMap<>();
        map.put("time", DateUtils.parseDateFromDateStr(format));

       Date date = (Date) map.get("time");

        String formatDateStr = DateUtils.getFormatDateStr(date);
        System.out.println("时间格式化："+formatDateStr);
    }

}
