import com.alibaba.fastjson.JSON;
import com.qianlima.offline.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
       /*String resultStr ="";
        String str = "国务院国有资产监督管理委员会/中国移动通信集团有限公司/中国铁通集团有限公司/中国铁通集团有限公司北京通信设备维护中心";
        String[] split = str.split("/");
        for (int i=split.length-1;i>-1;i--) {
            resultStr+=split[i]+"/";
        }
        System.out.println(resultStr);*/
      /* List<Integer> list = new ArrayList<>();
       list.add(1);
       list.add(2);
       list.add(3);

        System.out.println(list.size());*/
      /*String str ="";

      for (int i =0;i<3;i++){
          str = i+"/"+str;
      }
        System.out.println(str);*/


      /*String str ="";

      if (str.contains("h")){
          System.out.println("hahaha"+str);
      }*/
      String str ="{\"code\": 0,\"msg\": \"\",\"data\": {\"province\": \"北京市\",\"city\": \"海淀区\",\"regLocation\": \"北京市海淀区上地东路1号院1号楼8层A801-5\"}}";
      Map map =  (Map) JSON.parse(str);
      String code = map.get("code").toString();
      String data = map.get("data").toString();
      Map m =(Map) JSON.parse(data);
        String province = m.get("province").toString();
        String city = m.get("city").toString();
        String regLocation = m.get("regLocation").toString();

        System.out.println("code:"+code);
        System.out.println("province:"+regLocation);
        System.out.println("city:"+city);
        System.out.println("regLocation:"+regLocation);
    }
}