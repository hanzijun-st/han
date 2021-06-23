package com.qianlima.offline.util;

import com.qianlima.offline.entity.ZhongXinBean;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取Excel数据，进行写入数据库
 */
public class ReadExcelUtil {

    /**
     * 读取Excel-获取集合
     *
     * @return 数据集合
     */
    public static List<String> readExcel(String filePath){

        //读取Excel数据到List中
        List<String> list = new ArrayList<>();
        XSSFWorkbook workbook = null;
        try {
            File file = new File(filePath);
            // 读取Excel文件
            InputStream inputStream = new FileInputStream(filePath);
            //workbook = new HSSFWorkbook(inputStream);
            //获取excel文件流
            workbook = new XSSFWorkbook(inputStream);

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 循环工作表
        for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
            XSSFSheet hssfSheet = workbook.getSheetAt(numSheet);
            if (hssfSheet == null) {
                continue;
            }
            // 循环行
            for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                XSSFRow hssfRow = hssfSheet.getRow(rowNum);
                if (hssfRow == null) {
                    continue;
                }
                // 将单元格中的内容存入集合
                ZhongXinBean proprichange = new ZhongXinBean();

                XSSFCell cell = hssfRow.getCell(0);
                if (cell == null) {
                    continue;
                }

                String companyName = cell.getStringCellValue();//企业名称
                list.add(companyName);
            }
        }
        return list;
    }

}