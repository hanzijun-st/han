import com.qianlima.offline.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class TestString5 {
    public static void main(String[] args) {
        String[] str ={"1","2",""};
        List<String[]> list = new ArrayList<>();
        list.add(StrUtil.deleteArrayNull(str));
        System.out.println(StrUtil.listToStr(list,","));
    }
}