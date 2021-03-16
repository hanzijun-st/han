import com.qianlima.offline.bean.ErrorExceptionBean;

/**
 * 测试抛出异常
 */
public class TestException {
    public static void main(String[] args){
        ErrorExceptionBean bean = new ErrorExceptionBean("异常", 500);
        System.out.println(bean.getCode());
        System.out.println(bean.getMessage());
    }
}