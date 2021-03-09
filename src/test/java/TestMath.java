import com.qianlima.offline.util.MathUtil;
import io.swagger.models.auth.In;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 数字相关的测试
 */
public class TestMath {
    public static void main(String[] args) {
       /*
        //转换大写数字
        String s = MathUtil.getAmountToDaXie(123.5);
        System.out.println(s);*/

       /*//数组中找到最小值
       List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        int min=Integer.MAX_VALUE;//int类型能表示的最大值
        for(int e:list){
            if(e<min){
                min=e;
            }
        }
        System.out.println(min);*/
       List<Integer> a = new ArrayList<>();
       a.add(8);
       a.add(4);
       List<Integer> b = new ArrayList<>();
       b.add(3);
       b.add(5);

       List allList = new ArrayList();
       allList.addAll(a);
       allList.addAll(b);
        List list = mergeArray(a, b);
        System.out.println(list.toString());
    }
    // 合并数组
    public static List mergeArray(List a, List b) {
        //int result[] = new int[a.size() + b.size()];
        List list = new ArrayList(a.size()+b.size());
        if (checkSort(a) && checkSort(b)) {
            // 说明ab数组都是有序的数组
            // 定义两个游标
            int i = 0, j = 0, k = 0;
            while (i < a.size() && j < b.size()) {
                if (Integer.valueOf(a.get(i).toString()) <= Integer.valueOf(b.get(j).toString())) {
                    list.add(a.get(i++));
                } else {
                    list.add(b.get(j++));
                }
            }
            while (i < a.size()) {
                // 说明a数组还有剩余
                list.add(a.get(i++));
            }
            while (j < b.size()) {
                list.add(b.get(j++));
            }
        }else {
            //Collections.sort(allList);
        }
        return list;
    }
    // 检查一个数组是否是有序1 2 3
    public static boolean checkSort(List a) {
        boolean flag = false;// 默认不是有序的
        for (int i = 0; i < a.size() - 1; i++) {
            if (Integer.valueOf(a.get(i).toString()) > Integer.valueOf(a.get(i + 1).toString())) {
                // 说明不是有序的
                flag = false;
                break;
            } else {
                flag = true;
            }
        }
        return flag;
    }



}