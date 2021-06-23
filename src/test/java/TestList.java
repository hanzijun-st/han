import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class TestList {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        ListIterator<Integer> l = list.listIterator();
        while (l.hasNext()) {
            if (l.next() < 6) {
                l.remove();
            }
        }
        System.out.println("- - - L:" + list);


        for (int i = list.size() - 1; i >= 0; i--) {
            Integer integer = list.get(i);
            if (integer < 6) {
                list.remove(i);
            }
        }
        System.out.println(list);
    }
}