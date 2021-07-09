import java.math.BigDecimal;

public class TestBigDecimal {

    public static void main(String[] args) {
        /*String str ="109916666.59";

        BigDecimal a = new BigDecimal(str);
        double v = a.setScale(2, BigDecimal.ROUND_DOWN).doubleValue();

        String s = new BigDecimal(Double.parseDouble(a.toPlainString())).toString();
        System.out.println(a);
        System.out.println(s);*/


        String abc = "1.567840334E12";
        BigDecimal bd = new BigDecimal(abc);
        System.out.println(new BigDecimal(Double.parseDouble(bd.toPlainString())).toString());
    }
}