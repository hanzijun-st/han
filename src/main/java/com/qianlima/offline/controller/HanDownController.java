package com.qianlima.offline.controller;

import com.qianlima.offline.bean.Student;
import com.qianlima.offline.entity.ZhongXinBean;
import com.qianlima.offline.service.han.TestDownService;
import com.qianlima.offline.util.DownLoadUtil;
import com.qianlima.offline.util.ExportExcelUtil;
import com.qianlima.offline.util.ExportExcelWrapperUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/down")
@Slf4j
@Api("下载")
@CrossOrigin(origins = "*",maxAge = 3600)
public class HanDownController {
    @Autowired
    private TestDownService testDownService;


    @ApiOperation("测试下载")
    @PostMapping("/downFile")
    public String downFile() {
        testDownService.downFile();
        return "---downFile---";
    }

    @ApiOperation(notes = "文件下载-通过文件的路径", value = "文件下载")
    @GetMapping("/downloadfile")
    public ResponseEntity<byte[]> downloadFile(HttpServletResponse response) {

        return testDownService.downFile();
    }

    /**
     * TODO 下载文件到本地
     *
     * @param fileUrl   远程地址
     * @param fileLocal 本地路径
     * @throws Exception
     * @author nadim
     * @date Sep 11, 2015 11:45:31 AM
     */
    @ApiOperation(notes = "文件下载-通过链接", value = "文件下载")
    @GetMapping("/downloadFileByUrl")
    public void downloadFileByUrl(String fileUrl, String fileLocal, HttpServletResponse response) throws Exception {
        fileLocal = "E:/downExcelFile/任务说明_file0.txt";
        fileUrl = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fyouimg1.c-ctrip.com%2Ftarget%2Ftg%2F035%2F063%2F726%2F3ea4031f045945e1843ae5156749d64c.jpg&refer=http%3A%2F%2Fyouimg1.c-ctrip.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1621474400&t=cf398940aaf825a99e99a64b6879ba65";

        String picUrl = fileUrl;
        String fileName = "aq.jpeg";
        try {
            DownLoadUtil.downLoadFromUrl(picUrl, fileName, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("文件上传")
    @RequestMapping(value = "/upload/file", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    public String upload(MultipartFile uploadFile, HttpServletRequest request) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        List list = new ArrayList();//存储生成的访问路径
        if (uploadFile !=null) {
           // for (int i = 0; i < uploadFiles.; i++) {
                //MultipartFile uploadFile = uploadFiles[i];
                //设置上传文件的位置在该项目目录下的uploadFile文件夹下，并根据上传的文件日期，进行分类保存
                //String realPath = "usr\\local\\ka\\hzj\\upLoad";
                String realPath = File.separator+"usr"+File.separator+"local"+File.separator+"ka"+File.separator+"hzj"+File.separator+"upLoad";
                String format = sdf.format(new Date());
                File folder = new File(realPath);
                if (!folder.isDirectory()) {
                    folder.mkdirs();
                }

                String oldName = uploadFile.getOriginalFilename();
                String prefix = oldName.substring(oldName.lastIndexOf("."));//后缀
                try {
                    //保存文件
                    //File file = new File(folder, oldName);
                    boolean flag = true;
                    while (flag) {
                        String name = oldName.replaceAll("[.][^.]+$", "");//获取没有后缀的文件名称
                        if (name.contains("_file")) {
                            String[] names = name.split("_file");
                            Integer num = Integer.valueOf(names[1].toString()) + 1;
                            oldName = names[0] + "_file" + num;
                        } else {
                            oldName = name + "_file0";
                        }
                        File newFile = new File(folder, oldName + prefix);
                        if (!newFile.exists()) {
                            flag = false;
                        }
                    }

                    uploadFile.transferTo(new File(folder, oldName + prefix));

                    //生成上传文件的访问路径
                    //String filePath = request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort() + "/uploadFile" + format + newName;
                    String filePath = realPath + "\\" + oldName + prefix;
                    list.add(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
           // }
            return list.toString();
        } else if (uploadFile == null) {
            return "请选择文件";
        }
        return "上传失败";
    }



    @ApiOperation("文件上传-linux1")
    @RequestMapping(value = "/upload/fileLinux1", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    public String uploadLinux1(@RequestParam("file") MultipartFile uploadFile, HttpServletRequest request) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        List list = new ArrayList();//存储生成的访问路径
        if (uploadFile !=null) {
           // for (int i = 0; i < uploadFiles.; i++) {
                //MultipartFile uploadFile = uploadFiles[i];
                //设置上传文件的位置在该项目目录下的uploadFile文件夹下，并根据上传的文件日期，进行分类保存
                //String realPath = "E:\\downExcelFile";
                String realPath = File.separator+"usr"+File.separator+"local"+File.separator+"ka"+File.separator+"hzj"+File.separator+"upLoad";
                String format = sdf.format(new Date());
                File folder = new File(realPath);
                if (!folder.isDirectory()) {
                    folder.mkdirs();
                }

                String oldName = uploadFile.getOriginalFilename();
                String prefix = oldName.substring(oldName.lastIndexOf("."));//后缀
                try {
                    //保存文件
                    //File file = new File(folder, oldName);

                    uploadFile.transferTo(new File(folder, oldName));

                    //生成上传文件的访问路径
                    //String filePath = request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort() + "/uploadFile" + format + newName;
                    String filePath = realPath + File.separator + oldName;
                    list.add(filePath);
                    try {
                        testDownService.upFileToMySql_1(filePath);
                        log.info("- - -数据导入到库成功- - -");
                    } catch (Exception e) {
                        e.getMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
           // }
            return list.toString();
        } else if (uploadFile == null) {
            return "请选择文件";
        }
        return "上传失败";
    }


    @ApiOperation("文件上传-企业名录-数据清空1")
    @RequestMapping(value = "/upload/del1", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String del1() {
        testDownService.del1();
        return "企业名称-数据库1-清空成功";
    }

    @ApiOperation("文件上传-企业名录-数据清空1")
    @RequestMapping(value = "/upload/del2", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
    public String del2() {
        testDownService.del2();
        return "企业名称-数据库2-清空成功";
    }



    @ApiOperation("文件上传-linux2")
    @RequestMapping(value = "/upload/fileLinux2", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    public String uploadLinux2(@RequestParam("file") MultipartFile uploadFile, HttpServletRequest request) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        List list = new ArrayList();//存储生成的访问路径
        if (uploadFile !=null) {
           // for (int i = 0; i < uploadFiles.; i++) {
                //MultipartFile uploadFile = uploadFiles[i];
                //设置上传文件的位置在该项目目录下的uploadFile文件夹下，并根据上传的文件日期，进行分类保存
                //String realPath = "E:\\downExcelFile";
                String realPath = File.separator+"usr"+File.separator+"local"+File.separator+"ka"+File.separator+"hzj"+File.separator+"upLoad";
                String format = sdf.format(new Date());
                File folder = new File(realPath);
                if (!folder.isDirectory()) {
                    folder.mkdirs();
                }

                String oldName = uploadFile.getOriginalFilename();
                String prefix = oldName.substring(oldName.lastIndexOf("."));//后缀
                try {
                    //保存文件
                    //File file = new File(folder, oldName);

                    uploadFile.transferTo(new File(folder, oldName));

                    //生成上传文件的访问路径
                    //String filePath = request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort() + "/uploadFile" + format + newName;
                    String filePath = realPath + File.separator + oldName;
                    list.add(filePath);
                    try {
                        testDownService.upFileToMySql_2(filePath);
                        log.info("- - -数据导入到库成功- - -");
                    } catch (Exception e) {
                        e.getMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
           // }
            return list.toString();
        } else if (uploadFile == null) {
            return "请选择文件";
        }
        return "上传失败";
    }


    /**
     * 支持在线打开方式
     *
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


    @ApiOperation("工具导出")
    @GetMapping("/downExcel")
    public void getExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 准备数据
        List<Student> list = new ArrayList<>();
        // list.add(new Student(1,"张三asdf",22));
        // list.add(new Student(2,"李四asd",33));
        // list.add(new Student(3,"王五",23));
        String[] columnNames = {"ID", "姓名", "年龄"};
        String fileName = "学生信息表";
        String title = "学生信息表";
        ExportExcelWrapperUtil<Student> util = new ExportExcelWrapperUtil<>();
        util.exportExcel(fileName, title, columnNames, list, response, ExportExcelUtil.EXCEl_FILE_2007);
        testDownService.downExcel();
    }

    @GetMapping(value = "/upload/export")
    @ApiOperation("导出企业名录-天眼查")
    public String checkDownLoad(HttpServletResponse response) {
        String fileName = "企业名录";
        String title = "信息表";
        String[] headers ={"原始名称","实收注册资金","企业状态","注册资金","登记机关","企业名称","经营范围","行业","注册地址","注册号","联系方式","统一社会信用代码","核准时间","经营开始时间","企业类型","组织机构代码","经营结束时间","法人","省","市","县"};

        ExportExcelWrapperUtil<ZhongXinBean> util = new ExportExcelWrapperUtil<>();
        util.exportExcel(fileName, title, headers, testDownService.getList1(), response, ExportExcelUtil.EXCEl_FILE_2007);
        return null;
    }

    @GetMapping(value = "/upload/export2")
    @ApiOperation("导出企业名录-天眼查")
    public String downLoad2(HttpServletResponse response) {
        String fileName = "企业名录";
        String title = "信息表";
        String[] headers ={"原始名称","实收注册资金","企业状态","注册资金","登记机关","企业名称","经营范围","行业","注册地址","注册号","联系方式","统一社会信用代码","核准时间","经营开始时间","企业类型","组织机构代码","经营结束时间","法人","省","市","县"};

        ExportExcelWrapperUtil<ZhongXinBean> util = new ExportExcelWrapperUtil<>();
        util.exportExcel(fileName, title, headers, testDownService.getList2(), response, ExportExcelUtil.EXCEl_FILE_2007);
        return null;
    }

}
