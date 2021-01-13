package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.Student;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2021/1/12.
 */

@Service
@Slf4j
public class TestServiceImpl implements TestService{

    @Autowired
    private ContentSolr contentSolr;
    @Autowired
    private ZhongTaiBiaoDiWuService bdwService;
    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    @Override
    public void getBdw() {
        try {
            bdwService.getSolrAllField2("hBdw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getDatasToUpdateKeyword() {

    }

    @Override
    public void updateKeyword() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();

        //String[] keywords ={"交换机","锐捷"};
        try {
            List<String> keywords = LogUtils.readRule("hKeywords");
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid,name,brand,model FROM h_biaodiwu");
            if (mapList !=null && mapList.size() >0){
                List<Map<String,Object>> list = new ArrayList<>();

                for (Map<String, Object> map : mapList) {
                    String id = map.get("id").toString();
                    String contentid = map.get("contentid").toString();
                    String name = map.get("name").toString();
                    String brand = map.get("brand").toString();
                    String model = map.get("model").toString();

                    String key = "";
                    for (String keyword : keywords) {
                        if (name.contains(keyword) || brand.contains(keyword) || model.contains(keyword)){
                            key+=keyword+"、";
                        }
                    }
                    if (ZTStringUtil.isNotBlank(key)){
                        Map<String,Object> m = new HashMap<>();
                        m.put(id,key.substring(0,key.length() - 1));
                        list.add(m);
                    }
                }
                if (list !=null && list.size() > 0){
                    for (Map<String, Object> map : list) {
                        for(Map.Entry<String,Object> e :map.entrySet()){
                            if (e.getValue() !=null){
                                futureList.add(executorService1.submit(() -> {
                                    bdJdbcTemplate.update("UPDATE h_biaodiwu SET keyword = ? WHERE id = ?", e.getValue() , e.getKey());
                                }));
                            }
                        }
                    }
                }
                for (Future future1 : futureList) {
                    try {
                        future1.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        executorService1.shutdown();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService1.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String downLoad() {
        try {
            //List<String> keywords = LogUtils.readRule("hKeywords");
            ExcelUtil2<Student> util = new ExcelUtil2<Student>();
            // 准备数据
            List<Student> list = new ArrayList<>();
            list.add(new Student(1,"哈哈哈",22));
            list.add(new Student(2,"嘎嘎嘎",33));
            list.add(new Student(3,"咔咔咔",32));

            String[] columnNames = { "ID", "姓名", "年龄" };
            util.exportExcel("用户导出", columnNames, list, new FileOutputStream("E:/test.xls"), ExcelUtil2.EXCEL_FILE_2003);
        } catch (IOException e) {
            e.printStackTrace();
            return "-------导出失败";
        }
        return "-------导出成功";
    }
}
