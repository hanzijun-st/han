import java.util.Arrays;

public class TestDelStr {
    public static void main(String[] args) {
        //该数组有多个相同的元素
        String[] arr01 = {"a", "a", "b", "s", "a", "g", "f", "b", "w", "s", "a"," "," "};
        System.out.println("\n原始数组：" + Arrays.toString(arr01));

        //删除有相同元素的元素（null和“ ”，不处理）
        deleteSameDate(arr01);
        //输出
        System.out.print("删除重复元素后的数组：");
        for (String s : arr01) {
            if (s != null) {
                System.out.print(s + " ");
            }
        }
    }
    //输出String[]中分别相同的字符串元素，以及统计个数。并把相同的元素赋值null。
    private static void deleteSameDate(String[] arr01) {
        //计数器
        int count = 1;

        //下标i的数据为参考元素
        for (int i = 0; i < arr01.length; i++) {
            //下标j的元素，从i右边第一个元素开始
            for (int j = i + 1; j < arr01.length; j++) {
                //找到了相同的元素
                if (arr01[i] != null && !arr01[i].equals(" ") &&arr01[i].equals(arr01[j])) {
                    //相同的元素赋值null
                    arr01[j] = null;
                    //计数器+1
                    count++;
                }
            }

            //如果大于1，则说明有相同的元素，而且找完了第一对所有相同的元素
            if (count > 1) {
                //输出第一个相同元素的值和数量
                System.out.println("重复出现的元素："+ arr01[i]+", 共有"+count+"个");
                //参考元素已找到相同的元素，则把参考元素赋值null
                arr01[i] = null;
                //复位计数器
                count=1;
                //递归找下一对相同的元素
                deleteSameDate(arr01);
            }
        }
    }
}