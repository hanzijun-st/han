package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeAllField;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.Test39Service;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class Test39ServiceImpl implements Test39Service {

    @Autowired
    private OnlineContentSolr onlineContentSolr;
    @Autowired
    private OnlineNewContentSolr onlineNewContentSolr;

    @Autowired
    private CurrencyService currencyService;//为了获取 progid

    @Autowired
    private CusDataNewService cusDataNewService;//调用中台接口


    @Override
    public void test() {
        log.info("没有调取任何接口-测试项目启动");
    }

    @Override
    public void getZhongRuan(Integer type, String date,String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        String[] a={"神州网信"};
        try {
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(str);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            /*for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(str);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //混合中标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progidStr+")  AND newZhongBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(str);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }*/
            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("=========================================================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词c
            for (String key : a){
                arrayList.add(key);
            }

            List<String> readList = new ArrayList<>();
            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (StrUtil.isNotEmpty(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }

                System.out.println(str + ": " + total);
                readList.add(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

            readList.add("全部数据量：" + listAll.size());
            readList.add("去重之后的数据量：" + list.size());

            ReadFileUtil.readFile(ReadPathUtil.getPath("file"),"关键词.txt",readList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getZhongRuan2(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        //List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        Set<Long> setList = new HashSet<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        String[] a={"神州网信"};
        try {
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(str);
                                    setList.add(data.getContentid());
                                    /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }*/
                                }
                            }
                        }
                    }
                }));
            }
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //混合中标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progidStr+")  AND newZhongBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(str);
                                    setList.add(data.getContentid());
                                    /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }*/

                                }
                            }
                        }
                    }
                }));
            }
            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + setList.size());
            log.info("=========================================================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词c
            for (String key : a){
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (StrUtil.isNotEmpty(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + setList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (listAll != null && listAll.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listAll) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getAliBiaoti(Integer type, String date,String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            List<String> a = LogUtils.readRule("keyWords");
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }*/
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            System.out.println("全部数据量：" + listAll.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (listAll != null && listAll.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listAll) {
                    futureList.add(executorService.submit(() -> cusDataNewService.saveIntoMysqlToAli(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getTest(Integer type, String date, String progidStr) {
        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        String str ="S603环万佛湖公路养护";
        List<NoticeAllField> noticeAllFields = onlineNewContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:" + progid + ") AND allcontent:\"" + str + "\"", "", 1);
        for (NoticeAllField noticeAllField : noticeAllFields) {
            log.info("得到的所有solr中的字段：{}",noticeAllField);
        }
    }

    @Override
    public void getBiMaWei(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            List<String> a = LogUtils.readRule("keyWordsA");
            List<String> b = LogUtils.readRule("keyWordsB");
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoFirstIndustry:\"" + "金融企业" + "\" AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (String str : b) {
                String[] split = str.split("&");
                String str1 = split[0];
                String str2 = split[1];
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoFirstIndustry:\"" + "金融企业" + "\" AND (allcontent:\"" + str1 + "\"  AND allcontent:\"" + str2 + "\")", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

           /* ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : a) {
                arrayList.add(key);
            }
            //关键词a
            for (String key : b) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());*/

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                try {
                    List<String> l = LogUtils.readRule("keyWordsC");
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveByList(content,l)));
                    }
                    for (Future future : futureList) {
                        try {
                            future.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    executorService.shutdown();
                } catch (IOException e) {

                }

            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getBiMaWeiByTitle(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            List<String> a = LogUtils.readRule("keyWordsA");
            List<String> b = LogUtils.readRule("keyWordsB");
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoFirstIndustry:\"" + "金融企业" + "\" AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    //匹配标题词是否包含关键词  二次过滤
                                    if (data.getTitle().contains(str)){
                                        data.setKeyword(str);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (String str : b) {
                String[] split = str.split("&");
                String str1 = split[0];
                String str2 = split[1];
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoFirstIndustry:\"" + "金融企业" + "\" AND (title:\"" + str1 + "\"  AND title:\"" + str2 + "\")", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    //匹配标题词是否包含关键词  二次过滤
                                    if (data.getTitle().contains(str1) && data.getTitle().contains(str2)){
                                        data.setKeyword(str);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

           /* ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : a) {
                arrayList.add(key);
            }
            //关键词a
            for (String key : b) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());*/

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
                List<Future> futureList = new ArrayList<>();
                try {
                    List<String> l = LogUtils.readRule("keyWordsC");
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveByTitle(content,l)));
                    }
                    for (Future future : futureList) {
                        try {
                            future.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    executorService.shutdown();
                } catch (IOException e) {

                }

            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getShanXiXingBaoLai(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            String[] a ={"消毒柜","开水器","饮水机","烘干机"};
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : a) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getShanXiXingBaoLai2(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            String[] a ={"厨房","餐饮","酒店","餐馆","食堂","咖啡店","西餐厅","饭店","学校","幼儿园"};
            String[] b ={"消毒柜","开水器","饮水机","烘干机"};
            /*for (String str : b) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }*/
            for (String str : b) {
                for (String str2 : a) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:"+progid+") AND allcontent:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : b) {
                for(String k : a)
                arrayList.add(key+"&"+k);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    /**
     * 毕马威中国-规则三
     * @param type
     * @param date
     * @param progidStr
     */
    @Override
    public void getBiMaWeiByTitle_3(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            List<String> a = LogUtils.readRule("keyWordsA");
            List<String> b = LogUtils.readRule("keyWordsB");
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+")  AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    //匹配标题词是否包含关键词  二次过滤
                                    if (data.getTitle().contains(str)){
                                        data.setKeyword(str);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (String str : b) {
                String[] split = str.split("&");
                String str1 = split[0];
                String str2 = split[1];
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+")  AND (title:\"" + str1 + "\"  AND title:\"" + str2 + "\")", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    //匹配标题词是否包含关键词  二次过滤
                                    if (data.getTitle().contains(str1) && data.getTitle().contains(str2)){
                                        data.setKeyword(str);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
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

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                try {
                    List<String> l = LogUtils.readRule("keyWordsC");
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveByTitle(content,l)));
                    }
                    for (Future future : futureList) {
                        try {
                            future.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    executorService.shutdown();
                } catch (IOException e) {

                }
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getBiMaWeiByTitle_3_1(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        try {
            //关键词a
            List<String> a  = LogUtils.readRule("keyWordsD");

            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位-全文
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND zhaoFirstIndustry:\"" + "金融企业" + "\" AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (Future future1 : futureList1) {
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

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                String[] pbc = {"咨询有限公司","制造技术有限公司","娱乐有限公司","有限责任公司","有限公司","影视有限公司","医疗技术研究所","研究院有限公司","研究院","研究所","宣传中心","信息有限公司","信息技术有限公司","信息技术集团有限公司","信息传播有限公司","信息产业有限公司","小微服务企业","网络有限公司","网络通信有限公司","网络公司","投资有限公司","投资企业","投资管理有限公司","通讯科技有限公司","通信研究院","通信科技有限公司","设备有限公司","软件技术开发有限公司","企业集团","企业","平台服务有限公司","培训学校","控股有限公司","科学技术研究院","科技有限公司","科技发展有限公司","开发有限公司","健康管理有限公司","健康服务有限公司","技术有限公司","技术集团有限公司","技术发展有限公司","集团有限公司","集团股份有限公司","集团公司","基地","国家工程中心","管理有限公司","管理监督中心","股份有限公司","公司","工程有限公司","工程科技有限公司","工程技术有限公司","服务有限公司","发展中心","电子商务有限公司","创业投资有限公司","传媒有限公司","传媒集团有限公司","传媒股份有限公司","传播工程有限公司","城市与交通研究所","城市管理监督中心","产业有限责任公司","产业有限公司","产业投资管理有限公司","产业集团"};
                ExecutorService executorService = Executors.newFixedThreadPool(80);

                try {
                    List<String> a = LogUtils.readRule("keyWordsD");
                    List<String> l = Arrays.asList(pbc);
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                       futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveByPb(content,l,a)));
                    }
                    for (Future future : futureList) {
                        try {
                            future.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    executorService.shutdown();
                } catch (IOException e) {

                }
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getShanXiXingBaoLai2_1(Integer type, String date, String progidStr) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String progid = currencyService.getProgidStr(progidStr);//获取对应的progid对应的值
        //关键词a
        try {
            String[] a ={"厨房","餐饮","酒店","餐馆","食堂","咖啡店","西餐厅","饭店","学校","幼儿园"};
            String[] b ={"消毒柜","开水器","饮水机","烘干机"};
            /*for (String str : b) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:"+progid+") AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }*/
            for (String str : b) {
                for (String str2 : a) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:3 OR progid:5) AND zhongBiaoUnit:* AND allcontent:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(str+"&"+str2);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            for (Future future1 : futureList1) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : b) {
                for(String k : a){
                    arrayList.add(key+"&"+k);
                }
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }


    /**
     * 调用中台数据，进行处理
     */
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            //String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            //先去链接
            //content =processAboutContent(content);
            //再判断是否是字母
            //content = checkString(content);

            cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作

        }
    }
    private void getZhongTaiDatasAndSaveByPb(NoticeMQ noticeMQ,List<String> list,List<String> a) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段

            //正文当中关键词紧跟着屏蔽词，该关键词隐藏
            //list 屏蔽词     a为关键词
            String str ="";//正文
            for (String gjc : a) {
                for (String pbc : list) {
                    if (content.contains(gjc+pbc)){
                        str = content.replaceAll(gjc,"");
                    }
                }
            }
            String keyword = resultMap.get("keyword").toString();
            boolean kb =false;
            for (String ss : a) {
                if (str.contains(ss)){
                    kb = true;
                    keyword = keyword+","+ss;
                }
            }
            if (kb){
                resultMap.put("keyword",keyword);
                cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作
                log.info("数据入库成功---{}",keyword);
            }
        }
    }

    //匹配关键词的 中台数据
    private void getZhongTaiDatasAndSaveByList(NoticeMQ noticeMQ,List<String> list) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            //先去链接
            content =processAboutContent(content);
            //再判断是否是字母
            content = checkString(content);

            //匹配关键词
            for (String s : list) {
                String[] split = s.split("&");
                String s1 = split[0];
                if (split.length>1){
                    String s2 = split[1];
                    if (content.contains(s1) && content.contains(s2)){
                        resultMap.put("keyword_term",s);
                        resultMap.put("content",content);
                        cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作
                        log.info("数据存入数据库成功：{}",resultMap.get("content_id"));
                        break;
                    }
                }else {
                    if (content.contains(s1)){
                        resultMap.put("keyword_term",s1);
                        resultMap.put("content",content);
                        cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作
                        log.info("数据存入数据库成功：{}",resultMap.get("content_id"));
                        break;
                    }
                }
            }
        }
    }
    //匹配关键词的 中台数据
    private void getZhongTaiDatasAndSaveByTitle(NoticeMQ noticeMQ,List<String> list) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String title = resultMap.get("title").toString();
            title = checkString(title);

            //匹配关键词
            for (String s : list) {
                String[] split = s.split("&");
                String s1 = split[0];
                if (split.length>1){
                    String s2 = split[1];
                    if (title.contains(s1) && title.contains(s2)){
                        resultMap.put("keyword_term",s);
                        cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作
                        log.info("数据存入数据库成功：{}",resultMap.get("content_id"));
                        break;
                    }
                }else {
                    if (title.contains(s1)){
                        resultMap.put("keyword_term",s1);
                        cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作
                        log.info("数据存入数据库成功：{}",resultMap.get("content_id"));
                        break;
                    }
                }
            }
        }
    }
    /**
     * 判断是否是字母
     * @param str 传入字符串
     * @return 是字母返回true，否则返回false
     */
    public static String checkString(String str) {
        str = str.toUpperCase();
        String[] Keywords = new String[]{"OA","A股","CRM","ERP","ESG","FTP","GBS","IPO","LGD","NLP","PPP","RPA","RPA","SAP","SOX","UAT","APIM","IFRS","SCRM","AGILE","CAS21","CAS22","IFRS9","IFRS9","SWIFT","DEVOPS","IFRS16","ISO27001","ISO27701"};
        for (String Keyword : Keywords) {
            Keyword = Keyword.toUpperCase();
            boolean flag = true;
            int key = str.indexOf(Keyword);
            if (key != -1) {
                if (key != 0) {
                    String substring1 = str.substring(key - 1, key);
                    Pattern p = Pattern.compile("[A-Z]");
                    Matcher m = p.matcher(substring1);
                    if (m.find() == true) flag = false;
                }
                if (key + Keyword.length() < str.length()) {
                    String substring1 = str.substring(key + Keyword.length(), key + Keyword.length() + 1);
                    Pattern p = Pattern.compile("[A-Z]");
                    Matcher m = p.matcher(substring1);
                    if (m.find() == true) flag = false;
                }
            }
            if (flag == false) {
                str = str.replace(Keyword.toUpperCase(), "");
            }
        }
        return str.toUpperCase();
    }

    /**
     * 去链接
     * @param content
     * @return
     */
    public static String processAboutContent(String content) {
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a[href]");
        Integer elementSize = elements.size();
        for (Integer i = 0; i < elementSize; i++) {
            Element element = elements.get(i);
            if (element == null || document.select("a[href]") == null || document.select("a[href]").size() == 0) {
                break;
            }
            String elementStr = element.attr("href");
            if (StringUtils.isNotBlank(elementStr) && elementStr.contains("www.qianlima.com")) {
                if (element.is("a")) {
                    element.remove();
                }
            }
        }
        return document.body().html();
    }




}