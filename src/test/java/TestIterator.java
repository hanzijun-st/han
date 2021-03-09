import java.util.*;

/**
 * 迭代器
 */
public class TestIterator {
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("张三1");
        list.add("张三2");
        list.add("张三3");
        list.add("张三4");

        List<String> linkList = new LinkedList<String>();
        linkList.add("link1");
        linkList.add("link2");
        linkList.add("link3");
        linkList.add("link4");

        Set<String> set = new HashSet<String>();
        set.add("set1");
        set.add("set2");
        set.add("set3");
        set.add("set4");

        List<Map> listMap = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("1","h");
        map.put("2","hh");
        listMap.add(map);

        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
        }
        Iterator<Map.Entry<String, Object>> iterator1 = entries.iterator();
        while (iterator1.hasNext()){
            Map.Entry<String, Object> next = iterator1.next();
            String key = next.getKey();
            String value = next.getValue().toString();
            System.out.println(key+"---"+value);
        }

        //使用迭代器遍历ArrayList集合
        Iterator<Map> iterator = listMap.iterator();
        while (iterator.hasNext()){
            Map next = iterator.next();
            System.out.println(next);
        }

        Iterator<String> listIt = list.iterator();
        while(listIt.hasNext()){
            String next = listIt.next();
            System.out.println(listIt.next());
        }
        //使用迭代器遍历Set集合
        Iterator<String> setIt = set.iterator();
        while(setIt.hasNext()){
            String next = setIt.next();
            System.out.println(setIt.next());
        }
        //使用迭代器遍历LinkedList集合
        Iterator<String> linkIt = linkList.iterator();
        while(linkIt.hasNext()){
            String next = linkIt.next();
            System.out.println(linkIt.next());
        }
    }
}