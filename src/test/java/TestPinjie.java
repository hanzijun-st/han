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
            String[] a ={"厨房","餐饮","酒店","餐馆","食堂","咖啡店","西餐厅","饭店","学校","幼儿园"};
            String[] b ={"消毒柜","开水器","饮水机","烘干机"};

            for (String datadd : b) {
                for (String datass : a) {
                    String str ="";
                    str ="allcontent:"+datadd+" AND allcontent:"+datass;
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
        File file = new File("C:/Users/Administrator/Desktop/wenjian/test10.txt");

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