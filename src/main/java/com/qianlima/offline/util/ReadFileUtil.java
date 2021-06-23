package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 将内容写文件中，指定存放的目录
 */
@Slf4j
public class ReadFileUtil {

    public static void readFile(String url, String fileName, List<String> strList) {

        File file = new File(url+"/"+fileName);

        if(file.exists()) {
            System.err.println("如果已存在该名称的文件，将更新原文件内容");
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
    /**
     * 同一个文件中多次写入数据  (默认false)
     * @param url
     * @param fileName
     * @param map
     * @param date
     */
    public static void readFileByMap(String url, String fileName, Map<String, Long> map,String date,boolean b) {
        File file = new File(url+"/"+fileName);

        if(file.exists()) {
            log.info("---如果已存在该名称的文件，将更新原文件内容---");
            file.delete();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file,b),"UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write("----"+date+"\r\n");
                for (Map.Entry<String, Long> strLongEntry : map.entrySet()) {
                    String s = strLongEntry.getKey()+":"+strLongEntry.getValue();
                    bw.write(s+"\r\n");
                }
                log.info("已完成100%，导入结束!!!!!!");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void readFileByMapObj(String url, String fileName, Map<String, Object> map,String date,boolean b) {
        File file = new File(url+"/"+fileName);

        if(file.exists()) {
            log.info("---如果已存在该名称的文件，将更新原文件内容---");
            file.delete();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file,b),"UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write("----"+date+"\r\n");
                for (Map.Entry<String, Object> strLongEntry : map.entrySet()) {
                    String s = strLongEntry.getKey()+":"+strLongEntry.getValue();
                    bw.write(s+"\r\n");
                }
                log.info("已完成100%，导入结束!!!!!!");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}