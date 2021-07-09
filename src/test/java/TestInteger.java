public class TestInteger {
    public static void main(String[] args) {
        int a = 100;
        int b = 100;
        System.out.println(a == b);

        Integer c = 100;
        Integer d = 100;
        System.out.println(c == d);

        Integer e = new Integer(100);
        Integer f = new Integer(100);
        System.out.println(e == f);
        System.out.println(e.intValue() == f.intValue());


        Integer g = 1000;
        Integer h = 1000;
        System.out.println(g == h);

        int k =1000;
        int m =1000;
        System.out.println(k == m);




        String url ="https://www.ccgp.gov.cn/jyywjsgcfwjzzbhxrgs/786646.jhtml";
        if (url.contains("www.ccgp.gov.cn")){
            System.out.println("出现错误");
        }else {
            System.out.println("没错");
        }
    }
}