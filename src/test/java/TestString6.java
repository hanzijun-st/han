import com.qianlima.offline.util.CollectionUtils;
import com.qianlima.offline.util.StrUtil;

public class TestString6 {
    public static void main(String[] args) {
        //字符中包含特殊字符
        String str =" a han 韩1   3 5";
        CharSequence trim = StrUtil.trim(str);
        String s = trim.toString();
        System.out.println(s);

        String s1 = StrUtil.delAllPlace(str);
        System.out.println("-----"+s1);

        String a = StrUtil.deleteString(s1, 'a');
        System.out.println("====="+a);
    }
}