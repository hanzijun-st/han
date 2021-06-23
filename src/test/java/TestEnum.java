import com.google.common.util.concurrent.AtomicDouble;
import com.qianlima.offline.rule02.BiaoDiWuRule;

import java.util.concurrent.atomic.AtomicInteger;

public class TestEnum {
    public static void main(String[] args) {
       /* int type = 1;
        for (BiaoDiWuRule value : BiaoDiWuRule.values()) {
            if (value.getValue().intValue() == type){
                System.out.println(value.getName());
            }
        }*/
       AtomicInteger atomicInteger = new AtomicInteger(1);

        for (int i = 0; i <5 ; i++) {
            atomicInteger.getAndAdd(1);
        }
        System.out.println(atomicInteger);
    }
}