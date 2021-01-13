import com.qianlima.offline.util.ExcelUtil;
import com.qianlima.offline.util.XlsToXls;

import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */
public class TestClass {
    public static void main(String[] args) {
        Map<String, Object> map = XlsToXls.readXlsOne("E:\\excelFile\\2.xls", 0);

        System.out.println(map.toString());
    }
}
