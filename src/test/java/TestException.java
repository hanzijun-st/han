import com.qianlima.offline.bean.ErrorExceptionBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试抛出异常
 */
public class TestException {
    public static void main(String[] args){
       /* ErrorExceptionBean bean = new ErrorExceptionBean("异常", 500);
        System.out.println(bean.getCode());
        System.out.println(bean.getMessage());*/
       
       Map map = new HashMap<>();
       map.put("h",1);
       
       int i =0;
       try {
           String hh = map.get("hh").toString();
       }catch (Exception e){
           e.getMessage();
           i = 1;
       }
        System.out.println(i);
       
    }
}