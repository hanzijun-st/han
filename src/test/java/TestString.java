import com.qianlima.offline.util.StrUtil;

public class TestString {
    public static void main(String[] args) {
       /* String str ="我是";
        str += StrUtil.splictYh("中国人");

        str +=",我爱";
        str +=StrUtil.splictYh("我的祖国");
        System.out.println(str);


        for (int i=0; i<6;i++){
            if (i ==2){
                continue;
            }
            System.out.println(i);
        }*/
       String resultStr ="";
        String str = "国务院国有资产监督管理委员会/中国移动通信集团有限公司/中国铁通集团有限公司/中国铁通集团有限公司北京通信设备维护中心";
        String[] split = str.split("/");
        for (int i=split.length-1;i>-1;i--) {
            resultStr+=split[i]+"/";
        }
        System.out.println(resultStr);
    }
}