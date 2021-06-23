import com.qianlima.offline.Application;
import com.qianlima.offline.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TestRedis {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void setRedis(){
        redisUtil.saveCode("12","345",1L);
    }
}