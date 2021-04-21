package com.qianlima.offline.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DownLoadUtil {
    /**
     *
     * @param urlStr    下载文件的url（不包括文件名）
     * @param response    响应流
     * @throws Exception
     */
    public static void  downLoadFromUrl(String urlStr,String fileName, HttpServletResponse response) throws Exception {
        response.reset();
        //response.setContentType("image/jpeg");    可是指定下载文件格式
        //设置头信息                 Content-Disposition为属性名  附件形式打开下载文件   指定名称  为 设定的fileName
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        //URLEncoder.encode()    如果文件名为中文，需进行解码。
        URL url = new URL(urlStr+ URLEncoder.encode(fileName));
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        InputStream inputStream = conn.getInputStream();
        ServletOutputStream output = response.getOutputStream();
        byte[] buffer = new byte[1024 * 8];
        int count=0;
        while ((count = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, count);
            output.flush();
        }
        output.close();
    }

}