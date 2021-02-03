import com.qianlima.offline.util.StrUtil;

public class TestString {
    public static void main(String[] args) {
        String str ="我是";
        str += StrUtil.splictYh("中国人");

        str +=",我爱";
        str +=StrUtil.splictYh("我的祖国");
        System.out.println(str);
    }
}