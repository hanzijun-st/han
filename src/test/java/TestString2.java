import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestString2 {
    public static void main(String[] args) {
        String s ="55%";
       /* if (s.contains("%")){
            System.out.println(s);
            String sub = s.substring(0, s.length() - 1);
            Integer integer = Integer.valueOf(sub);
            System.out.println(integer);
        }

        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher("adsada_asdads");
        System.out.println(m.find());*/

        if ("%".equals(s.substring(s.length()-1,s.length()))){
            Integer percentNum = Integer.valueOf(s.substring(0,s.length()-1));
            if (percentNum < 50){
                System.out.println("不行");
            }else {
                System.out.println("可以");
            }
        }

        String patten = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,}$";
        String str ="Aa123456";
        boolean matches = str.matches(patten);
        System.out.println(matches);


    }

    //返回对应的数据

}