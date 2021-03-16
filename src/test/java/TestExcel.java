import com.qianlima.offline.util.ExcelUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试导出
 */
public class TestExcel {
    public static void main(String[] args){
        String[] title ={"a","b","c"};
        List pp = new ArrayList<>();

        Per p = new Per();
        p.setName("hhh");
        p.setAge("20");
        p.setAddr("北京");
        pp.add(p);

        byte[] export = ExcelUtil.export("2", title, pp);
        System.out.println(export.length);
    }

    @Data
    static class Per{
        private String name;
        private String age;
        private String addr;
    }
}