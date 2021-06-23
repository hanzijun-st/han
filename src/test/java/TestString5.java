import com.qianlima.offline.util.StrUtil;

public class TestString5 {
    public static void main(String[] args) {
        /*String[] str ={"1","2",""};
        List<String[]> list = new ArrayList<>();
        list.add(StrUtil.deleteArrayNull(str));
        System.out.println(StrUtil.listToStr(list,","));*/
        String[] str ={"h","l","s"};
        if (str !=null && str.length >0){
            String s = new String("h");
            String s1 = StrUtil.listToStr(str);
            if (s1.contains(s)){
                System.out.println("包含");
            }else {
                System.out.println("不包含");
            }
        }else {
            System.out.println("原数组为空");
        }

        char c = 'h';
        String s = StrUtil.deleteString(StrUtil.listToStr(str), c);
        System.out.println(s);
    }
}