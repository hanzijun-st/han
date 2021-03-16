import com.qianlima.offline.rule02.BiaoDiWuRule;

public class TestEnum {
    public static void main(String[] args) {
        int type = 1;
        for (BiaoDiWuRule value : BiaoDiWuRule.values()) {
            if (value.getValue().intValue() == type){
                System.out.println(value.getName());
            }
        }
    }
}