package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.OnlineContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NewJNiZaiJianService {

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    @Qualifier("crmJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate qlmJdbcTemplate;//文件

    HashMap<String, Area> areaMap =  new HashMap<>();

    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = qlmJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(map.get("areaid").toString(),area);
        }
    }

    private String secondToDate(long second,String patten) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second * 1000);//转换为毫秒
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(patten);
        String dateString = format.format(date);
        return dateString;
    }

    public void getSolrAll() throws Exception{

        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();

        String[] keywords = { "清扫保洁","环卫一体化","垃圾收转运","垃圾转运","垃圾分类服务","垃圾治理","公测管养","市政养护","绿化养护","水域保洁","市容管理","智慧环卫","智能清扫","城市大管家","物业城市","建筑垃圾","垃圾处理","老旧小区服务","三无小区服务"};

        for (String keyword : keywords) {
            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian( "yyyymmdd:[20180101 TO 20210631] AND catid:101 AND allcontent:\""+keyword+"\"",keyword, 4);
            log.info("keyword:{}====查询出了------size：{}条数据", keyword, mqEntities.size());
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    list1.add(data);
                    data.setKeyword(keyword);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }

        log.info("全部数据量："+list1.size());
        log.info("去重之后的数据量："+list.size());
        log.info("==========================");


        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() ->  guanWangData(content)));
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
        log.info("数据全部跑完啦,总数量为：" +1);

    }





    public void guanWangData(NoticeMQ map) {
        Long contentid = Long.valueOf(map.getContentid());
        log.info("contentid:{} 对应的数据状态不是" , contentid);
        Map<String, Object> map1 = qlmJdbcTemplate.queryForMap("SELECT * FROM phpcms_content where contentid=?",contentid);
        String updatetime = map1.get("updatetime").toString(); //跟新时间
        String dateString = secondToDate(Long.valueOf(updatetime),"yyyy-MM-dd hh:mm:ss");

        String xmNumber = map1.get("contentid").toString(); //项目编号

        List<Map<String, Object>> map2 = qlmJdbcTemplate.queryForList("SELECT * FROM zdy_xm_all where id=?",contentid);
        if(map2 == null || map2.size() == 0){
            return;
        }

        String title = map2.get(0).get("xmmc").toString(); //标题
        String jzjd = map2.get(0).get("jzjd").toString(); //进展阶段


        String xmxz = map2.get(0).get("xmxz").toString(); //项目性质

        List<Map<String, Object>> map3 = qlmJdbcTemplate.queryForList("SELECT * FROM zdy_xm_extenddetails where xmid=?",contentid);
        if(map3 == null || map3.size() == 0){
            return;
        }

        String yzlx = map3.get(0).get("yzlx").toString(); //业主类型
        if(yzlx.equals("1")){
            yzlx = "商业";
        }else if(yzlx.equals("2")){
            yzlx = "政府";
        }

        String lbA = map3.get(0).get("lbA").toString(); //项目类别
        StringBuilder lbAStr = new StringBuilder(); //项目类别
        String[] split1 = lbA.split(",");
        for (String s : split1) {
            Map<String, Object> jzjdmapping = qlmJdbcTemplate.queryForMap("SELECT * FROM zdy_xm_typemapping where xmid=?",Integer.valueOf(s));
            lbAStr.append(jzjdmapping.get("xmname")+",");
        }
        lbA = lbAStr.toString().substring(0,lbAStr.toString().length() - 1);

        String lbB = map3.get(0).get("lbB").toString(); //项目子类别
        StringBuilder lbBStr = new StringBuilder(); //项目类别
        String[] split2 = lbB.split(",");
        for (String s : split2) {
            Map<String, Object> jzjdmapping = qlmJdbcTemplate.queryForMap("SELECT * FROM zdy_xm_typemapping where xmid=?",Integer.valueOf(s));
            lbBStr.append(jzjdmapping.get("xmname")+",");
        }
        lbB = lbBStr.toString().substring(0,lbBStr.toString().length() - 1);

        String tze = map2.get(0).get("tze").toString(); //项目投资
        if(StringUtils.isBlank(tze) || tze.equals("0")){
            tze = map3.get(0).get("gsje").toString();
            if(StringUtils.isBlank(tze)){
                tze = "不确定";
            }
        }
        String kgrq = map3.get(0).get("kgrq").toString(); //开工时间
        String jgrq = map3.get(0).get("jgrq").toString(); //竣工时间
        String jzmj = map3.get(0).get("jzmj").toString(); //建筑面积
        String zdmj = map3.get(0).get("zdmj").toString(); //占地面积
        String jzcs = map3.get(0).get("jzcs").toString().equals("0")?"不确定":map3.get(0).get("jzcs").toString(); //建筑物层数

        String gjg = "";//钢结构
        if(map3.get(0).get("gjg").toString().equals("0")){
            gjg = "否";
        }else if(map3.get(0).get("gjg").toString().equals("1")){
            gjg = "是";
        }else{
            gjg = "不确定";
        }
        String zxqk = ""; //装修情况
        if(map3.get(0).get("zxqk").toString().equals("0")){
            zxqk = "不装修"; //钢结构
        }else if(map3.get(0).get("zxqk").toString().equals("1")){
            zxqk = "简装修"; //钢结构
        }else if(map3.get(0).get("zxqk").toString().equals("2")){
            zxqk = "精装修"; //钢结构
        }
        String zxbz = map3.get(0).get("zxbz").toString(); //装修标准
        if(StringUtils.isBlank(zxbz)){
            zxbz = "不确定";
        }
        String wqys = map3.get(0).get("wqys").toString(); //外墙预算
        if(StringUtils.isBlank(wqys)){
            wqys = "不确定";
        }
        String xmdz = map3.get(0).get("xmdz").toString(); //项目地址

        String content = map2.get(0).get("content").toString();//项目概括


        List<Map<String, Object>> map4 = qlmJdbcTemplate.queryForList("SELECT * FROM zdy_xm_lxrmore where xmid=?",contentid);

        List<Map> yezhuList = new ArrayList<>();
        List<Map> shejiList = new ArrayList<>();
        List<Map> shigongList = new ArrayList<>();
        for (Map<String, Object> object : map4) {
            if(Integer.valueOf(object.get("type").toString()) == 1){
                Map<String,Object> yezhu = new HashMap<>();
                yezhu.put("单位名称",object.get("dwmc")); //单位名称
                yezhu.put("地址",object.get("addr")); //地址
                yezhu.put("联系人",object.get("lxr")); //联系人
                yezhu.put("状态",object.get("isJob")); //状态
                yezhu.put("职位",object.get("Zhiwei")); //职位
                yezhu.put("性别",object.get("Sex")); //性别
                yezhu.put("手机",object.get("mobile")); //手机
                yezhu.put("电话",object.get("phone")); //电话
                yezhu.put("emial",object.get("email")); //emial
                yezhu.put("传真",object.get("fax")); //传真
                yezhu.put("备注",object.get("beizhu")); //备注
                yezhu.put("类型",object.get("type")); //类型
                yezhuList.add(yezhu);

                bdJdbcTemplate.update("INSERT INTO `xiangmu_data_sheet2`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
                        map.getTaskId(),contentid,title,object.get("dwmc"),object.get("Zhiwei"),object.get("lxr"),object.get("phone"),object.get("mobile"),"业主");

            }else if(Integer.valueOf(object.get("type").toString()) == 2){
                Map<String,Object> sheji = new HashMap<>();
                sheji.put("单位名称",object.get("dwmc")); //单位名称
                sheji.put("地址",object.get("addr")); //地址
                sheji.put("联系人",object.get("lxr")); //联系人
                sheji.put("状态",object.get("isJob")); //状态
                sheji.put("职位",object.get("Zhiwei")); //职位
                sheji.put("性别",object.get("Sex")); //性别
                sheji.put("手机",object.get("mobile")); //手机
                sheji.put("电话",object.get("phone")); //电话
                sheji.put("emial",object.get("email")); //emial
                sheji.put("传真",object.get("fax")); //传真
                sheji.put("备注",object.get("beizhu")); //备注
                sheji.put("类型",object.get("type")); //类型
                shejiList.add(sheji);
                bdJdbcTemplate.update("INSERT INTO `xiangmu_data_sheet2`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
                        map.getTaskId(),contentid,title,object.get("dwmc"),object.get("Zhiwei"),object.get("lxr"),object.get("phone"),object.get("mobile"),"设计");
            }else if(Integer.valueOf(object.get("type").toString()) == 3){
                Map<String,Object> shigong = new HashMap<>();
                shigong.put("单位名称",object.get("dwmc")); //单位名称
                shigong.put("地址",object.get("addr")); //地址
                shigong.put("联系人",object.get("lxr")); //联系人
                shigong.put("状态",object.get("isJob")); //状态
                shigong.put("职位",object.get("Zhiwei")); //职位
                shigong.put("性别",object.get("Sex")); //性别
                shigong.put("手机",object.get("mobile")); //手机
                shigong.put("电话",object.get("phone")); //电话
                shigong.put("emial",object.get("email")); //emial
                shigong.put("传真",object.get("fax")); //传真
                shigong.put("备注",object.get("beizhu")); //备注
                shigong.put("类型",object.get("type")); //类型
                shigongList.add(shigong);
                bdJdbcTemplate.update("INSERT INTO `xiangmu_data_sheet2`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
                        map.getTaskId(),contentid,title,object.get("dwmc"),object.get("Zhiwei"),object.get("lxr"),object.get("phone"),object.get("mobile"),"施工");
            }
        }

        String provinceStr = null; //旧省
        String cityStr = null; //旧市
        String countryStr = null; //旧县

        if (StringUtils.isNotBlank(map.getAreaid())) {
            provinceStr = getAreaMap(map.getAreaid()).get("areaProvince");
            cityStr = getAreaMap(map.getAreaid()).get("areaCity");
            countryStr = getAreaMap(map.getAreaid()).get("areaCountry");
        }

        log.info("处理到:{}",atomicInteger.incrementAndGet());

        String keyword = map.getKeyword();
        String code = "";
        String keyword_term = "";

        bdJdbcTemplate.update("INSERT INTO xiangmu_data_1(`contentid`,`title`, `updatetime`, `provinceStr`, `cityStr`, `countryStr`, `xmNumber`, `jzjd`, `xmxz`, `yzlx`, `lbA`, `lbB`, `tze`, `kgrq`, `jgrq`, `jzmj`, `zdmj`, `jzcs`, `gjg`, `zxqk`, `zxbz`, `wqys`, `xmdz`, `content`, `yezhuList`, `shejiList`, `shigongList`,`taskid`,`keyword`, `code`, `keyword_term`) VALUES (?,?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)",
                contentid, title, dateString, provinceStr, cityStr, countryStr, xmNumber, jzjd, xmxz, yzlx, lbA, lbB, tze, kgrq, jgrq, jzmj, zdmj, jzcs, gjg, zxqk, zxbz, wqys, xmdz, content, JSON.toJSONString(yezhuList), JSON.toJSONString(shejiList), JSON.toJSONString(shigongList), map.getTaskId(), keyword, code, keyword_term);
    }





    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();

    //招标单位联系人、联系电话。中标单位联系人、联系电话 多个的用的英文逗号分隔。
    private String format(String field) {
        if (StringUtils.isEmpty(field)) {
            return "";
        }
        return field.replaceAll("，", ",");
    }

    private AtomicInteger atomicInteger=new AtomicInteger(0);

    // 获取地区映射
    private synchronized Map<String, String> getAreaMap(String areaId) {
        Map<String, String> resultMap = new HashMap<>();
        if (kaAreaList == null || kaAreaList.size() == 0) {
            try {
                ClassPathResource classPathResource = new ClassPathResource("area/ka_area.txt");
                InputStream inputStream = classPathResource.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bufferedReader.readLine();
                while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
                    kaAreaList.add(line);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                log.error("读取ka_area 失败, 请查证原因");
            }
        }
        for (String kaArea : kaAreaList) {
            String[] areaList = kaArea.split(":", -1);
            if (areaList != null && areaList.length == 4) {
                if (areaList[0].equals(areaId)) {
                    resultMap.put("areaProvince", areaList[1]);
                    resultMap.put("areaCity", areaList[2]);
                    resultMap.put("areaCountry", areaList[3]);
                }
            }
        }
        return resultMap;
    }


}
