public class TestTimeNew {
    public static void main(String[] args) {
        /*String time ="20210423";


        System.out.println();*/
        for (int i =1; i< 3;i++){
            if (i == 2){
                try{
                    Long l = null;
                    String s = l.toString();
                }catch (Exception e){
                    continue;
                }
            }
            System.out.println(i);
        }

    }
}