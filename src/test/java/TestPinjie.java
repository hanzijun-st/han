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
        String[] aa = {"硅片", "电池片", "电池组件", "硅料", "铸锭", "拉棒", "多晶硅", "晶体硅", "薄膜电池", "太阳能板", "太阳能单晶板", "蓄电池", "太阳板", "铜芯导线", "铜芯花线", "单晶硅", "非晶硅", "电池板", "光伏板"};
        String[] ee = {"采购", "购置", "购买", "采买"};

        for (String datadd : ee) {
            for (String datass : aa) {
                String str = datadd + datass;
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
        File file = new File("C:/Users/Administrator/Desktop/wenjian/e-a.txt");

        if (file.exists()) {
            System.err.println("如果已存在test.txt的文件，将更新文件内容");
            file.delete();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), "GB2312");
                BufferedWriter bw = new BufferedWriter(osw);
                for (String s : strList) {
                    bw.write(s + "\r\n");
                }
                System.out.println("已完成100%，导入结束！");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}