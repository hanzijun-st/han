import com.sun.org.apache.regexp.internal.RE;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.Map;

public class TestLc {
    public static void main(String[] args) {
        String a ="";//1
        String b="";//2
        String c="";//3

        String user="1"+a;//通过xm bz
        Long userId =0L;
        Map<String, Object> map = getMap();
        map.get("");

    }

    private static Map<String,Object> getMap(){

        Map<String,Object> param = new HashMap<>();
        param.put("userId",1);
        param.put("userName","h");
        param.put("proId",11);

        return param;
    }
}