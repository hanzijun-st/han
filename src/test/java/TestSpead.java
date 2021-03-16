import java.util.*;

/**
 * 测试速度效率
 */
public class TestSpead {
    public static void main(String[] args){
        Map map = new HashMap<>();
        //List<Integer> list = new ArrayList<>();

        System.out.println("开始时间："+System.currentTimeMillis());
        Set<Integer> set = new HashSet<>();
        for (int i=0;i<1000000;i++){
            /*if (!map.containsKey(i)) {
                list.add(i);
                map.put(i, "0");
            }*/
            set.add(i);
        }
        //System.out.println(list.size()+"结束时间："+System.currentTimeMillis());
        System.out.println(set.size()+"结束时间："+System.currentTimeMillis());

    }
}

