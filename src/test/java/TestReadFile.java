import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.ReadFileUtil;
import com.qianlima.offline.util.ReadPathUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 双重循环，进行拼接
 */
public class TestReadFile {
    public static void main(String[] args) {


        /*List<String> bjDatasD = LogUtils.readRule("bjDatasD");//数据多的
        List<String> bjDatass =LogUtils.readRule("bjDatasS");//数据少的

        List<String> list = new ArrayList<>();
        for (String s : bjDatasD) {
            if (!bjDatass.contains(s)){
                list.add(s);
            }
        }
        ReadFileUtil.readFile("E:/work/han/src/test/resource","123.txt",list);
        System.out.println("运行结束");*/

        /*List<String> list = new ArrayList<>();
        for (int i=0;i<600;i++){
            list.add("测试一次数据的导入性是不是可以："+i);
        }
        String path = ReadPathUtil.getPath("file");
        ReadFileUtil.readFile(path,"test.txt",list);
        System.out.println("运行成功");*/

        /*String homePath = ReadPathUtil.getHomePath();
        List<String> list = new ArrayList<>();
        for (int i=0;i<100;i++){
            list.add("测试一次桌面路径："+i);
        }
        ReadFileUtil.readFile(homePath,"test.txt",list);*/


    }

}