package com.qianlima.offline.util;

import org.springframework.util.ResourceUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileNotFoundException;

public class ReadPathUtil {
    /**
     * 获取项目下文件全路径
     * @param folderName 某个文件夹名称
     * @return
     */
    public static String getPath(String folderName){
        if (StrUtil.isEmpty(folderName)){
            return null;
        }

        try {
            String path = ResourceUtils.getURL("src/main/resources").getPath()+ folderName;
            return path;
        } catch (FileNotFoundException e) {

        }
        return null;
    }

    /**
     *  获取当前系统电脑桌面路径
     * @return
     */
    public static String getHomePath(){
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File com = fsv.getHomeDirectory();
        return com.getPath();
    }
}