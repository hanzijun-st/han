import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.ReadFileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 双重循环，进行拼接
 */
public class TestReadFile {
    public static void main(String[] args) {

        try {
            List<String> bjDatasD = LogUtils.readRule("bjDatasD");//数据多的
            List<String> bjDatass =LogUtils.readRule("bjDatasS");//数据少的

            List<String> list = new ArrayList<>();
            for (String s : bjDatasD) {
                if (!bjDatass.contains(s)){
                    list.add(s);
                }
            }
            ReadFileUtil.readFile("E:/work/han/src/test/resource","123.txt",list);
            System.out.println("运行结束");
        } catch (IOException e) {

        }


    }

}