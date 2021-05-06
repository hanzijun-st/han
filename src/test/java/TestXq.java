public class TestXq {
    public static void main(String[] args){
       String str = "asuting";
        char[] chars = str.toCharArray();
        char c = str.charAt(1);
        for (char aChar : chars) {
            System.out.println(aChar);
        }
        System.out.println("结果二："+c);

        int i = str.codePointAt(0);//UNCODE 编码
        System.out.println("i:"+i);

        String DX = str.toUpperCase();
        String XX = str.toLowerCase();
        System.out.println(DX+":"+XX);

        int as = DX.compareTo("AS");
        int as1 = DX.compareToIgnoreCase("aSUT");//忽略大小写
        System.out.println("as1:"+as1);
        if (as >0){
            System.out.println("存在");
        }else {
            System.out.println("不存在");
        }

        String s ="哈哈哈";
        int ha = s.compareToIgnoreCase("哈");
        System.out.println("ha:"+ha);


        StringBuilder reverse = new StringBuilder(str).reverse();//线程安全
        System.out.println(reverse.toString());

        StringBuffer buffer = new StringBuffer(str).reverse();
        System.out.println("buffer:----"+buffer.toString());
    }

}