import com.sun.org.apache.xpath.internal.operations.Plus;

//数字相关的
public class TestNumber {
   /*public static void main(String[] args){
       TestNumber plus = new TestNumber();
       int sum = plus.sum(5);
       System.out.println(sum);
   }

   public int sum(int i){
       if (i == 1) {
           return 1;
       }
       int s = sum(i - 1);
       return i + s;
   }*/

    public static void main(String[] args) {
        for (int i = 1; i <= 20; i++) {
            System.out.println("第" + i + "个月的总数为:" + f(i));
        }
    }
    public static int f(int x) {
        if (x == 1 || x == 2) {
            return 1;
        } else {
            return f(x - 1) + f(x - 2);
        }
    }

}