import com.qianlima.offline.rule02.MyRuleUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class TestSolr {
    @Autowired
    private static MyRuleUtils myRuleUtils;
    public static void main(String[] args) {
        String str = myRuleUtils.getIndustry("四川省巴中市南江县人民医院");
        System.out.println(str);
    }
}