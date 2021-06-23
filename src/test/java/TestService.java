import com.qianlima.offline.Application;
import com.qianlima.offline.service.han.CurrencyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class TestService {
    @Autowired
    private CurrencyService currencyService;

    @Test
    public void test(){
        //String testService = currencyService.getTestService();
        //System.out.println(testService);
    }
}