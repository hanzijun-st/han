import io.swagger.models.auth.In;

public class TestTry {
   /* public static void main(String[] args) {
       getNum();
    }

    private static Integer getNum(){
        Integer i = 10;
        try {
            System.out.println("初始值："+i);
            return i;
        } catch (Exception e){
            i = i+10;
            System.out.println("catch:"+i);
            e.getMessage();
            return i;
        } finally {
            i = 20;
            System.out.println("finally:"+i);
            return i;
        }
    }*/

    public static void main(String[] args) {
        int i = 10;
        Byte aByte = (byte) i;
        if (aByte !=null){
            System.out.println(aByte);
        }else {
            System.out.println("No");
        }
    }
}