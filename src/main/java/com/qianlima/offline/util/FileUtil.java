package com.qianlima.offline.util;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.poi.hssf.record.LabelSSTRecord;

import java.io.*;

public class FileUtil {

    public static InputStream getResourcesFileInputStream(String fileName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("" + fileName);
    }

    public static String getPath() {
        return FileUtil.class.getResource("/").getPath();
    }

    public static File createNewFile(String pathName) {
        File file = new File(getPath() + pathName);
        if (file.exists()) {
            file.delete();
        } else {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }
        return file;
    }

    public static File readFile(String pathName) {
        return new File(getPath() + pathName);
    }

    public static File readUserHomeFile(String pathName) {
        return new File(System.getProperty("user.home") + File.separator + pathName);
    }

    /**
     * 读取excel
     */
    public static void readExcel(String fileUrl){
        File file = new File(fileUrl);
        try {
            // 创建输入流，读取Excel
            InputStreamReader isr=new InputStreamReader(new FileInputStream(fileUrl),"GBK");
            InputStream is = new FileInputStream(file.getAbsolutePath());
            //FileWriter fw = new FileWriter("d:\\t.xml",true);
            BufferedReader buffer = new BufferedReader(isr);

            //fw.write("<opcTagsInfo>\n\t");

            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            Sheet[] sheets=wb.getSheets();
            for(int i=0;i<sheets.length;i++){
                Sheet sheet =sheets[i];
                //fw.write("<tags id=\"" +sheet.getName()+ "\">\n\t");
                //fw.write("\t<OPCItems>\n\t\t");
                for(int j=1;j<sheet.getRows();j++){// 循环进行读写
                    Cell cell1 = sheet.getCell(0,j);
                    Cell cell2 = sheet.getCell(1,j);
                    Cell cell3 = sheet.getCell(2,j);
                    Cell cell4 = sheet.getCell(3,j);

                    String contents1 = cell1.getContents();
                    String contents2 = cell2.getContents();
                    String contents3 = cell3.getContents();
                    String contents4 = cell4.getContents();

                    //fw.write("\t<OPCItem name=\"" + contents2 + "\"  tag=\"" +contents3 + "\" key=\"" +contents1 + "\" dataType=\"" +contents4+ "\" />\n\t\t");
                }
                //fw.write("</OPCItems>\n\t");
                //fw.write("</tags>\n\t");
            }
            //fw.write("\n</opcTagsInfo>");
            //fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}