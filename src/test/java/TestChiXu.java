import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestChiXu {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        readFile(list);
    }

    public static void readFile(List<String> strList) {
        File file = new File("C:/Users/Administrator/Desktop/wenjian/testChiXu.txt");

       /* if(file.exists()) {
            System.err.println("如果已存在test.txt的文件，将更新文件内容");
            file.delete();
        }*/
        //if(!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file,true),"GB2312");
                BufferedWriter bw = new BufferedWriter(osw);
                for (String s : strList) {
                    bw.write(s+"\r\n");
                }
                System.out.println("已完成100%，导入结束！");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
       // }
    }
}