import java.util.Arrays;

public class TestJiaoYanXiangTong {
    public static void main(String[] args) {
        String s1="{\"followTime\":\"2020-05-07\",\"followVersion\":\"跟进3\",\"followStage\":\"主体施工\",\"followDesc\":\"截止2020年5月7日,该项目处于主体施工单位开工阶段,整体预计2020年12月完工。\"},{\"followTime\":\"2019-12-09\",\"followVersion\":\"跟进2\",\"followStage\":\"主体施工\",\"followDesc\":\"截止2019年12月9日,该项目主体施工单位已确定,单位为:广西圣泰建设工程有限公司,联系人将进一步核实。\"},{\"followTime\":\"2019-10-23\",\"followVersion\":\"跟进1\",\"followStage\":\"设计阶段\",\"followDesc\":\"截止2019年10月23日,该项目处于设计阶段,完成时间暂未定。\"}";
        String s2="{\"followTime\":\"2020-05-07\",\"followVersion\":\"跟进3\",\"followStage\":\"主体施工\",\"followDesc\":\"截止2020年5月7日,该项目处于主体施工单位开工阶段,整体预计2020年12月完工。\"},{\"followTime\":\"2019-12-09\",\"followVersion\":\"跟进2\",\"followStage\":\"主体施工\",\"followDesc\":\"截止2019年12月9日,该项目主体施工单位已确定,单位为:广西圣泰建设工程有限公司,联系人将进一步核实。\"},{\"followTime\":\"2019-10-23\",\"followVersion\":\"跟进1\",\"followStage\":\"设计阶段\",\"followDesc\":\"截止2019年10月23日,该项目处于设计阶段,完成时间暂未定。\"}";
        //boolean contains = ss.toLowerCase().contains(s2.toLowerCase());
        //System.out.println(contains);
        char[] arrayCh=s1.toCharArray();
        Arrays.sort(arrayCh);
        String sortedStr1=new String(arrayCh);

        char[] arrayCh2=s2.toCharArray();
        Arrays.sort(arrayCh2);
        String sortedStr2=new String(arrayCh2);

        if (sortedStr1.equals(sortedStr2)){
            System.out.println(true);
        }else{
            System.out.println(false);
        }
    }
}