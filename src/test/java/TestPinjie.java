import com.qianlima.offline.util.LogUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 双重循环，进行拼接
 */
public class TestPinjie {
    public static void main(String[] args) {
        //try {
            List<String> list = new ArrayList<>();
            //List<String> bjDatasD = LogUtils.readRule("bjDatasD");//数据多的
            //List<String> bjDatass =LogUtils.readRule("bjDatasS");//数据少的
            String[] a ={"快递","物流","供应链","运输","配送","仓储","冷链","速投","速递","邮递","邮政","快件","快寄","包裹","专送","快运","货运","货站","派送","转运","运送","输送","送货","装运","储运","仓配","搬运","仓库","冷运","集散中心","海运","陆运","空运"};
            String[] b ={"有限公司","科技有限公司","集团有限公司","服份有限公司","控股有限公司","中心有限公司","港有限公司","投资有限公司","投资集团有限公司","发展有限公司","有限责任公司","园有限公司","装备有限公司","服份有限公司","产业集团有限公司","城有限公司","开发有限公司","分公司"};

            for (String datadd : a) {
                for (String datass : b) {
                    String str =datadd+datass;
                    //str ="allcontent:"+datadd+" AND allcontent:"+datass;
                    list.add(str);
                }
            }
            readFile(list);
/*
        } catch (IOException e) {
            e.getMessage();
        }*/
    }

    public static void readFile(List<String> strList) {
        File file = new File("C:/Users/Administrator/Desktop/wenjian/pinci.txt");

        if(file.exists()) {
            System.err.println("如果已存在test.txt的文件，将更新文件内容");
            file.delete();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file),"GB2312");
                BufferedWriter bw = new BufferedWriter(osw);
                for (String s : strList) {
                    bw.write(s+"\r\n");
                }
                System.out.println("已完成100%，导入结束！");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}