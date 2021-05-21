import com.qianlima.offline.util.StrUtil;
import org.apache.commons.lang3.ArrayUtils;
import com.qianlima.offline.bean.Student;
import com.qianlima.offline.service.han.CurrencyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.util.StrUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class TestString4 {

    @Autowired
    private CurrencyService currencyService;

    public static void main(String[] args) {
        /*List<Student> list = new ArrayList<>();
        for (int i = 0; i <4 ; i++) {
            Student stu = new Student();
            if (i == 0){
                stu.setAge(10);
            } else {
                stu.setAge(i * 10);
            }
            stu.setName("zhangsan"+i);
            list.add(stu);
        }

        List<Student> collect =list.stream().filter(st ->st.getAge() >10).collect(Collectors.toList());
        System.out.println(collect.toString());*/
       /* String str ="abcd";
        String substring = str.substring(0, 2);
        System.out.println(substring);*/
      /* String[] s1={"1","2","3"};//全
       String[] s2={"1","3"};*/
       /*if (Arrays.asList(s1).containsAll(Arrays.asList(s2))){
            System.out.println(true);
       }*/
       /* Collection subtract = CollectionUtils.subtract(Arrays.asList(s1), Arrays.asList(s2));
        List<String> list = new ArrayList<>(subtract);
        System.out.println(list);*/
       /*String s ="1,2,3";
        String replace = s.replace(",", ";");
        System.out.println(replace);*/

      /* String[] str ={"a","b","c"};
        if (str.toString().contains("a")) {
            System.out.println(true);
        }*/




        /*Map map = new HashMap();       //定义Map对象
        map.put("apple", "新鲜的苹果");      //向集合中添加对象
        map.put("computer", "配置优良的计算机");
        map.put("book", "堆积成山的图书");
        map.put("time", new Date());
        String key = "booksbc";
        boolean contains = map.containsKey(key);    //判断是否包含指定的键值
        if (contains) {         //如果条件为真
            System.out.println("在Map集合中包含键名" + key); //输出信息
        } else {
            System.out.println("在Map集合中不包含键名" + key);
        }*/
        String[] aa ={"北大纵横管理咨询","北京北大纵横管理咨询有限责任公司","北京北大纵横管理咨询有限公司","北大纵横","北京纵横联合","中大咨询","中大管理咨询","广东中大管理咨询集团","中大创新咨询","广东中大管理咨询集团股份有限公司","正略钧策","正略集团","正略钧策企业管理","正略钧策集团股份有限公司","华彩咨询","华彩管理","上海华彩管理咨询有限公司","华夏基石","北京华夏基石企业管理咨询有限公司","和君集团","和君集团有限公司","和君咨询","和君咨询有限公司","和君商学","北京和君商学在线科技股份有限公司","北京纵横联合投资有限公司","广州市中大管理咨询有限公司","北京正略钧策咨询股份有限公司","北京和君咨询有限公司","北京北大保得利投资顾问有限公司","北京凯德欧亚咨询中心有限公司","烟台三校科技园置业有限公司","中大市场调研（深圳）有限公司","广州黑岩股权投资管理有限公司","北京中大创新咨询有限公司","中大管理咨询（深圳）有限公司","中大人才发展（深圳）有限公司","广州市中大信息技术有限公司","广西中大管理咨询有限公司","深圳正略百川信息技术有限公司","武汉正略百川企业管理咨询有限公司","成都正略企业管理咨询有限公司","广州正略钧策企业管理咨询有限公司","天津正略钧策企业管理咨询有限公司","天津正略百川企业管理咨询有限公司","上海正略钧策企业管理咨询有限公司","上海正略企业管理咨询有限公司","上海华彩企业顾问有限公司","北京中人基石文化发展有限公司","嘉兴和君清基股权投资合伙企业","中科建发通信科技有限公司","天津和君企业管理咨询有限公司","和君集团福建企业管理咨询有限公司","北京华职基业教育科技有限公司","和君集团安徽三度企业管理咨询有限公司","和君集团云南企业管理咨询有限公司","和君聚成无锡企业管理咨询有限公司","北京和君新媒体信息技术有限公司","新疆和君咨询有限公司","广州和君商学教育科技有限公司","宁波梅山保税港区三度斋股权投资合伙企业","江西和君商学在线科技有限公司","北京北大纵横管理咨询有限责任公司长沙办事处","广东中大管理咨询集团股份有限公司南京分公司","广东中大管理咨询集团股份有限公司成都分公司","广东中大管理咨询集团股份有限公司长沙分公司","上海华彩管理咨询有限公司第一分公司","北京和君咨询有限公司上海分公司"};
        String s = StrUtil.listToStr(aa);
        if (s.contains("北大纵横管理咨询")){
            System.out.println(true);
        }else {
            System.out.println(false);
        }

        String[] strList ={"北大纵横管理咨询","北京北大纵横管理咨询有限责任公司","北京北大纵横管理咨询有限公司"};
        System.out.println(strList.toString());
        if (strList.toString().contains("北大纵横管理咨询")) {
            System.out.println("1");
        }

        String join = StringUtils.join(aa, ",");
        System.out.println(join);

    }


}