import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.util.MathUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

public class TestChouJiang {
    //先从库表中抽取对应的人数

    @Autowired
    private CurrencyService currencyService;



    public static void main(String[] args) {
        //int num = (int)(Math.random()*1000+1);

        Random random = new Random();
        int i = random.nextInt(10000);
        System.out.println(i);
    }
}


