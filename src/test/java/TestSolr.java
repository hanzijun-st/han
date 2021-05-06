import com.qianlima.offline.bean.NoticeAllField;
import com.qianlima.offline.rule02.MyRuleUtils;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TestSolr {
    @Autowired
    private static MyRuleUtils myRuleUtils;
    public static void main(String[] args) {
        //String str = myRuleUtils.getIndustry("四川省巴中市南江县人民医院");
        //System.out.println(str);
        String str ="a，b,c";
        String keywords = str.replace("，", ",").toUpperCase();
        System.out.println(keywords);

        List<Integer> tes = getTes();
        System.out.println(tes);

    }

    /**
     * 游标
     * @return
     */
    private static List<Integer> getTes(){
        List<Integer> list = new ArrayList<>();
        Integer n =0;
        while (n < 100001) {
            n +=5000;
            System.out.println("运行到当前："+n);
            list.add(n);
        }
        return list;
    }
}