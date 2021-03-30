import com.qianlima.offline.Application;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestTencentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事务
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TestTransactional {
    @Autowired
    CurrencyService currencyService;
    
    @Autowired
    TestTencentService testTencentService;
    
    @Test
    public void test1 () {
        List<Map> maps = new ArrayList<>();
        Map map = new HashMap();
        map.put("task_id","H1");
        map.put("keyword","L1");

        try {
            String hhh = map.get("hhh").toString();
            maps.add(map);
            currencyService.saveData1(maps);
            test2();
        }catch (Exception e){
            e.getMessage();
        }
    }

    @Transactional
    @Async
    public void test2(){
        List<Map> maps2 = new ArrayList<>();
        Map map2 = new HashMap();
        map2.put("task_id","H222");
        map2.put("keyword","L222");
        maps2.add(map2);
        //((TestTencentService) AopContext.currentProxy()).saveData2(maps2);
        testTencentService.saveData2(maps2);
    }
}