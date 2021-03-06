package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestDownService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/down")
@Slf4j
@Api("下载")
public class HanDownController {
    @Autowired
    private TestDownService testDownService;


    @ApiOperation("测试下载")
    @PostMapping("/downFile")
    public String downFile(){
        testDownService.downFile();
        return "---downFile---";
    }

    @ApiOperation(notes = "文件下载", value = "文件下载")
    @GetMapping("/downloadfile")
    public ResponseEntity<byte[]> downloadFile(HttpServletResponse response) {

        return testDownService.downFile();
    }

    /**
     * 支持在线打开方式
     * @param filePath
     * @param response
     * @param isOnLine
     * @throws Exception
     */
    @ApiOperation(notes = "文件在线", value = "文件在线")
    @GetMapping("/downLoad")
    public void downLoad(String filePath, HttpServletResponse response, boolean isOnLine) throws Exception {
        File f = new File(filePath);
        if (!f.exists()) {
            response.sendError(404, "File not found!");
            return;
        }
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
        byte[] buf = new byte[1024];
        int len = 0;

        response.reset(); // 非常重要
        if (isOnLine) { // 在线打开方式
            URL u = new URL("file:///" + filePath);
            response.setContentType(u.openConnection().getContentType());
            response.setHeader("Content-Disposition", "inline; filename=" + f.getName());
            // 文件名应该编码成UTF-8
        } else { // 纯下载方式
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
        }
        OutputStream out = response.getOutputStream();
        while ((len = br.read(buf)) > 0)
            out.write(buf, 0, len);
        br.close();
        out.close();
    }

}
