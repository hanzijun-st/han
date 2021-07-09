package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.entity.ProposeCompany;
import com.qianlima.offline.entity.ProposeFollow;
import com.qianlima.offline.entity.ProposeInfo;
import com.qianlima.offline.entity.ProposeOverView;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NiZaiJianService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("lsJdbcTemplate")
    private JdbcTemplate lsJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }


    public void guanWangData(NoticeMQ map) {
        Long contentid = Long.valueOf(map.getContentid());
        log.info("contentid:{} 对应的数据状态不是" , contentid);
        Map<String, Object> map1 = lsJdbcTemplate.queryForMap("SELECT * FROM phpcms_content where contentid=?",contentid);
        String updatetime = map1.get("updatetime").toString(); //跟新时间
        String dateString = secondToDate(Long.valueOf(updatetime),"yyyy-MM-dd hh:mm:ss");

        String xmNumber = map1.get("contentid").toString(); //项目编号

        List<Map<String, Object>> map2 = gwJdbcTemplate.queryForList("SELECT * FROM zdy_xm_all where id=?",contentid);
        if(map2 == null || map2.size() == 0){
            return;
        }

        String title = map2.get(0).get("xmmc").toString(); //标题
        String jzjd = map2.get(0).get("jzjd").toString(); //进展阶段


        String xmxz = map2.get(0).get("xmxz").toString(); //项目性质

        List<Map<String, Object>> map3 = gwJdbcTemplate.queryForList("SELECT * FROM zdy_xm_extenddetails where xmid=?",contentid);
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
            Map<String, Object> jzjdmapping = gwJdbcTemplate.queryForMap("SELECT * FROM zdy_xm_typemapping where xmid=?",Integer.valueOf(s));
            lbAStr.append(jzjdmapping.get("xmname")+",");
        }
        lbA = lbAStr.toString().substring(0,lbAStr.toString().length() - 1);

        String lbB = map3.get(0).get("lbB").toString(); //项目子类别
        StringBuilder lbBStr = new StringBuilder(); //项目类别
        String[] split2 = lbB.split(",");
        for (String s : split2) {
            Map<String, Object> jzjdmapping = gwJdbcTemplate.queryForMap("SELECT * FROM zdy_xm_typemapping where xmid=?",Integer.valueOf(s));
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


        List<Map<String, Object>> map4 = gwJdbcTemplate.queryForList("SELECT * FROM zdy_xm_lxrmore where xmid=?",contentid);

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

                bdJdbcTemplate.update("INSERT INTO `han_nizaijian_sheet`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
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
                bdJdbcTemplate.update("INSERT INTO `han_nizaijian_sheet`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
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
                bdJdbcTemplate.update("INSERT INTO `han_nizaijian_sheet`(`taskid`,`xmNumber`, `title`, `dwmc`, `dept`, `lxr`, `phone`, `mobile`, `type`) VALUES (?,?,?,?,?,?,?,?,?)",
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

        bdJdbcTemplate.update("INSERT INTO han_nizaijian(`contentid`,`title`, `updatetime`, `provinceStr`, `cityStr`, `countryStr`, `xmNumber`, `jzjd`, `xmxz`, `yzlx`, `lbA`, `lbB`, `tze`, `kgrq`, `jgrq`, `jzmj`, `zdmj`, `jzcs`, `gjg`, `zxqk`, `zxbz`, `wqys`, `xmdz`, `content`, `yezhuList`, `shejiList`, `shigongList`,`taskid`,`keyword`, `code`, `keyword_term`) VALUES (?,?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)",
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

    private String secondToDate(long second,String patten) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second * 1000);//转换为毫秒
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(patten);
        String dateString = format.format(date);
        return dateString;
    }

    public void getNiZaiJianData(NoticeMQ noticeMQ) {
        String infoId = noticeMQ.getContentid().toString();
        String url ="http://datafetcher.intra.qianlima.com/dc/projectInfo/contentIds";
        Map<String, Object> objectMap = QianlimaZTUtil2.getProposeDataByInfoId(url, infoId);

        ProposeInfo proposeInfo = new ProposeInfo();
        if (objectMap == null) {
            log.error("该条 info_id：{}，获取中台拟在建数据失败", infoId);
            throw new RuntimeException("获取中台拟在建数据失败");
        }
        String returnCode = (String) objectMap.get("returnCode");
        if ("500".equals(returnCode)) {
            log.error("该条 info_id：{}，获取中台拟在建数据失败", infoId);
            throw new RuntimeException("获取中台拟在建数据失败");
        } else if ("0".equals(returnCode)) {
            JSONObject data = (JSONObject) objectMap.get("data");
            if (data == null) {
                log.error("该条 info_id：{}，获取中台拟在建数据失败", objectMap);
                throw new RuntimeException("获取中台拟在建数据失败");
            }
            JSONArray proposeArray = data.getJSONArray("data");
            if (proposeArray == null || proposeArray.size() == 0) {
                log.error("该条 info_id：{}，获取中台拟在建数据失败", objectMap);
                throw new RuntimeException("获取中台拟在建数据失败");
            }
            JSONObject jsonObject = proposeArray.getJSONObject(0);
            if (jsonObject == null) {
                log.error("该条 info_id：{}，获取中台拟在建数据失败", objectMap);
                throw new RuntimeException("获取中台拟在建数据失败");
            }
            // 对应千里马数据库 数据状态为非 99 状态
            Boolean delFlag = jsonObject.getBoolean("del_flag");
            if (delFlag) {
                log.info("infoId:{} 对应的拟在建数据已被删除", infoId);
                return;
            }
            JSONObject projectDetail = jsonObject.getJSONObject("project_detail");
            if (projectDetail == null) {
                log.error("该条 info_id：{}，获取中台拟在建数据失败", objectMap);
                throw new RuntimeException("获取中台拟在建数据失败");
            }

            // xm_basic信息
            String jzjd = "";
            String xmxz = "";
            String tze = "";
            String content = "";
            JSONObject xmBasic = projectDetail.getJSONObject("xm_basic");
            if (xmBasic !=null){
                jzjd = xmBasic.getString("jzjd");
                xmxz = xmBasic.getString("xmxz");
                tze = xmBasic.getString("tze");
                content = xmBasic.getString("content");
            }

            //extend_detail
            String yzlx = "";
            String lbA = "";
            String lbB = "";

            String kgrq = "";
            String jgrq = "";
            String jzmj = "";
            String zdmj = "";
            String jzcs = "";
            String gjg = "";
            String zxqk = "";
            String zxbz = "";
            String wqys = "";
            String xmdz = "";
            JSONObject extendDetail = projectDetail.getJSONObject("extend_detail");
            if (extendDetail !=null){
                yzlx = "1".equals(extendDetail.getString("yzlx")) ? "商业" : "政府";
                lbA = extendDetail.getString("lbA");
                lbB = extendDetail.getString("lbB");
                kgrq = extendDetail.getString("kgrq");
                jgrq = extendDetail.getString("jgrq");
                jzmj = extendDetail.getString("jzmj");
                zdmj = "0".equals(extendDetail.getString("zdmj")) ? "未确定" : extendDetail.getString("zdmj");
                jzcs = "0".equals(extendDetail.getString("jzcs")) ? "未确定" : extendDetail.getString("jzcs");
                gjg = "0".equals(extendDetail.getString("gjg")) ? "不使用" : "使用";
                zxqk = "0".equals(extendDetail.getString("zxqk")) ? "不装修" : "简装修";
                zxbz = StringUtils.isBlank(extendDetail.getString("zxbz")) ? "未确定" : extendDetail.getString("zxbz");
                wqys = StringUtils.isBlank(extendDetail.getString("wqys")) ? "未确定" : extendDetail.getString("wqys");
                xmdz = extendDetail.getString("xmdz");
            }
            String proposeTitle = getProposeTitle(xmBasic, infoId);
            if (StringUtils.isBlank(proposeTitle)) {
                log.error("infoId:{} 对应的拟在建数据的标题为空 title：{}", infoId, proposeTitle);
                throw new RuntimeException("获取中台拟在建数据失败");
            }
            proposeInfo.setInfoTitle(proposeTitle);
            proposeInfo.setInfoQianlimaUrl(url);
            proposeInfo.setCreateTime(System.currentTimeMillis());
            // 获取省、市、县 地区名称
            Map<String, String> areaMap = getAreaMap(xmBasic.getInteger("diqu").toString());
            if (!areaMap.isEmpty()) {
                proposeInfo.setAreaProvince(areaMap.get("areaProvince"));
                proposeInfo.setAreaCity(areaMap.get("areaCity"));
                proposeInfo.setAreaCountry(areaMap.get("areaCountry"));
            }
            // publish_time 对应qianlima.phpscms_content表的updatetime
            String publishTime = DateFormatUtils.format(Long.valueOf(jsonObject.getInteger("publish_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            proposeInfo.setInfoUpdateTime(publishTime);
            // create_time 对应qianlima.phpscms_content表的inputtime
            String createTime = DateFormatUtils.format(Long.valueOf(jsonObject.getInteger("create_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            proposeInfo.setInfoPublishTime(createTime);

            JSONArray contactDetail = projectDetail.getJSONArray("contact_detail");
            //JSONArray followTraceDetail = projectDetail.getJSONArray("follow_trace_detail");
            // infoId
            proposeInfo.setInfoId(infoId);
            // 获取 项目跟进列表(倒序排列)
           // JSONArray follows = getAllProposeFollows(followTraceDetail);
           // proposeInfo.setProposeFollows(follows);
            // 获取 项目联系人列表(正序排列)
            //JSONArray proposeCompanys = getAllProposeCompany(contactDetail);
            //proposeInfo.setProposeCompanys(proposeCompanys);
            List<String> yezhuList = new ArrayList<>();  //type 1
            List<String> shejiList = new ArrayList<>();//type 2
            List<String> shigongList = new ArrayList<>();//type 3
            List<ProposeCompany> allProposeCompanyToList = getAllProposeCompanyToList(contactDetail);
            if (!CollectionUtils.isEmpty(allProposeCompanyToList)){
                for (ProposeCompany proposeCompany : allProposeCompanyToList) {
                    if ("1".equals(proposeCompany.getCompanyType())){
                        yezhuList.add(proposeCompany.getLinkman());
                    }else if ("2".equals(proposeCompany.getCompanyType())){
                        shejiList.add(proposeCompany.getLinkman());
                    } else if ("3".equals(proposeCompany.getCompanyType())){
                        shigongList.add(proposeCompany.getLinkman());
                    }

                }
            }
            // 获取 项目概况
            Long jzjdid = xmBasic.getLong("jzjdid");
            //ProposeOverView proposeOverView = getProposeOverView(projectDetail, follows);
            //proposeOverView.setFollowStageId(jzjdid);
            //JSONObject jsonBean = QlmJsonUtil.toJsonBean(proposeOverView);
            //proposeInfo.setProposeOverView(jsonBean);


            String title =proposeInfo.getInfoTitle();
            String dateString =proposeInfo.getInfoUpdateTime();
            String provinceStr = proposeInfo.getAreaProvince();
            String cityStr = proposeInfo.getAreaCity();
            String countryStr = proposeInfo.getAreaCountry();



            String yezhu ="";
            String sheji ="";
            String shigong ="";
            if (yezhuList.size() >0){
                yezhu =StrUtil.listToStr(yezhuList,",");
            }
            if (shejiList.size() >0){
                sheji =StrUtil.listToStr(shejiList,",");
            }
            if (shigongList.size() >0){
                shigong =StrUtil.listToStr(shigongList,",");
            }


            //String task_id = "";
            String keyword = "";
            String code = "";
            String keyword_term ="";
            String xmNumber ="";
            bdJdbcTemplate.update("INSERT INTO han_nizaijian(`contentid`,`title`, `updatetime`, `provinceStr`, `cityStr`, `countryStr`, `xmNumber`, `jzjd`, `xmxz`, `yzlx`, `lbA`, `lbB`, `tze`, `kgrq`, `jgrq`, `jzmj`, `zdmj`, `jzcs`, `gjg`, `zxqk`, `zxbz`, `wqys`, `xmdz`, `content`, `yezhuList`, `shejiList`, `shigongList`,`taskid`,`keyword`, `code`, `keyword_term`) VALUES (?,?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)",
                    infoId, title, dateString, provinceStr, cityStr, countryStr, xmNumber, jzjd, xmxz, yzlx, lbA, lbB, tze, kgrq, jgrq, jzmj, zdmj, jzcs, gjg, zxqk, zxbz, wqys, xmdz, content, yezhu, sheji, shigong, 1, keyword, code, keyword_term);

        }
    }

    // 获取项目概况
    private ProposeOverView getProposeOverView(JSONObject projectDetail, JSONArray follows) {
        JSONObject extendDetail = projectDetail.getJSONObject("extend_detail");
        JSONObject xmBasic = projectDetail.getJSONObject("xm_basic");
        ProposeOverView proposeOverView = new ProposeOverView();
        proposeOverView.setInfoId(xmBasic.getInteger("id").toString());
        // 设置项目概况
        proposeOverView.setProposeContent(xmBasic.getString("content") != null ? xmBasic.getString("content") : "无项目概况信息");
        if (follows != null && follows.size() > 0) {
            ProposeFollow proposeFollow = (ProposeFollow) follows.get(0);
            // 设置 最新跟进、进展阶段
            proposeOverView.setNewFollow(proposeFollow.getFollowVersion());
            proposeOverView.setFollowStage(proposeFollow.getFollowStage());
        }
        // 设置项目性质、获取投资金额
        proposeOverView.setProjectNature(xmBasic.getString("xmxz") != null ? xmBasic.getString("xmxz") : "未确定");

        String investment = "";
        String tze = xmBasic.getInteger("tze") != null ? xmBasic.getInteger("tze").toString() : "";
        if (StringUtils.isBlank(tze) || "0".equals(tze)) {
            tze = "";
        }
        // 当不为空且不等于0时
        if (StringUtils.isNotBlank(tze)) {
            investment = tze + "万元";
        }

        // 占地面积、建筑物层数、钢结构、装修情况、装修标准、外墙预算、项目地址
        if (extendDetail != null) {
            // 业主类型
            String ownerType = extendDetail.getString("yzlx") != null ? extendDetail.getString("yzlx") : "";
            if (StringUtils.isNotBlank(ownerType)) {
                proposeOverView.setOwnerType("1".equals(ownerType) ? "商业" : "政府");
            }
            // 项目类别
            String projectType = extendDetail.getString("lbA") != null ? extendDetail.getString("lbA") : "";
            if (StringUtils.isNotBlank(projectType)) {
                String[] projectTypeList = projectType.split(",");
                String lbAValueList = "";
                for (String lbAStr : projectTypeList) {
                    if (StringUtils.isNotBlank(lbAStr)) {
                        lbAValueList += KeyUtils.getItemTypeMap().get(lbAStr) + ",";
                    }
                }
                if (StringUtils.isNotBlank(lbAValueList)) {
                    proposeOverView.setProjectType(lbAValueList.substring(0, lbAValueList.length() - 1));
                }
            }

            // 项目子类别
            String projectChildType = extendDetail.getString("lbB") != null ? extendDetail.getString("lbB") : "";
            if (StringUtils.isNotBlank(projectChildType)) {
                String[] lbBs = projectChildType.split(",");
                String lbBValueList = "";
                for (String lbBStr : lbBs) {
                    if (StringUtils.isNotBlank(lbBStr)) {
                        lbBValueList += KeyUtils.getItemTypeMap().get(lbBStr) + ",";
                    }
                }
                if (StringUtils.isNotBlank(lbBValueList)) {
                    proposeOverView.setProjectChildType(lbBValueList.substring(0, lbBValueList.length() - 1));
                }
            }

            if (StringUtils.isBlank(investment)) {
                investment = extendDetail.getString("gsje") != null ? "估算" + extendDetail.getString("gsje") + "万元（此金额为千里马预估）" : "未确定";
            }
            // 项目投资
            proposeOverView.setInvestment(investment);
            // 竣工时间、开工时间、建筑面积
            proposeOverView.setStartOnTime(extendDetail.getString("kgrq") != null ? extendDetail.getString("kgrq") : "");
            proposeOverView.setEndOnTime(extendDetail.getString("jgrq") != null ? extendDetail.getString("jgrq") : "");
            proposeOverView.setCoveredArea(extendDetail.getString("jzmj") != null ? extendDetail.getString("jzmj") : "");
            // 占地面积
            String floorArea = extendDetail.getString("zdmj") != null ? extendDetail.getString("zdmj") : "";
            if (StringUtils.isBlank(floorArea) || "0".equals(floorArea)) {
                floorArea = "未确定";
            }
            proposeOverView.setFloorArea(floorArea);
            // 建筑物层数
            String storey = extendDetail.getString("jzcs") != null ? extendDetail.getString("jzcs") : "";
            if (StringUtils.isBlank(storey) || "0".equals(storey)) {
                storey = "未确定";
            }
            proposeOverView.setStorey(storey);

            // 钢结构
            String steelwork = extendDetail.getString("gjg") != null ? extendDetail.getString("gjg") : "";
            if (StringUtils.isNotBlank(steelwork)) {
                steelwork = "0".equals(steelwork) ? "不使用" : "使用";
            }
            proposeOverView.setSteelwork(steelwork);
            // 装修情况
            String decorationDesc = extendDetail.getString("zxqk") != null ? extendDetail.getString("zxqk") : "";
            if (StringUtils.isNotBlank(decorationDesc)) {
                decorationDesc = "0".equals(decorationDesc) ? "不装修" : "简装修";
            }
            proposeOverView.setDecorationDesc(decorationDesc);
            // 装修标准
            String decorationNorm = extendDetail.getString("zxbz") != null ? extendDetail.getString("zxbz") : "";
            if (StringUtils.isBlank(decorationNorm)) {
                decorationNorm = "未确定";
            }
            proposeOverView.setDecorationNorm(decorationNorm);
            // 外墙预算
            String wallBudget = extendDetail.getString("wqys") != null ? extendDetail.getString("wqys") : "";
            if (StringUtils.isBlank(wallBudget)) {
                wallBudget = "未确定";
            }
            proposeOverView.setWallBudget(wallBudget);
            // 项目地址
            proposeOverView.setProposeAddress(extendDetail.getString("xmdz") != null ? extendDetail.getString("xmdz") : "");
        }

        return proposeOverView;
    }

    private String getProposeTitle(JSONObject xmBasic, String infoId) {
        if (xmBasic == null) {
            log.info("infoId:{} 对应的拟在建数据的基本信息为空 xmBasic：{}", infoId, xmBasic);
            return null;
        }
        String title = xmBasic.getString("xmmc");
        if (StringUtils.isBlank(title)) {
            title = xmBasic.getString("title");
        }
        return title;
    }
    // 项目跟进信息列表
    private JSONArray getAllProposeFollows(JSONArray followTraceDetail) {
        JSONArray jsonArray = new JSONArray();
        if (followTraceDetail == null || followTraceDetail.size() == 0) {
            return jsonArray;
        }
        // 需要根据intime进行倒序排序  order by intime desc
        followTraceDetail.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("intime")).reversed());

        if (followTraceDetail != null && followTraceDetail.size() > 0) {
            for (int i = 0; i < followTraceDetail.size(); i++) {
                JSONObject jsonObject = followTraceDetail.getJSONObject(i);
                ProposeFollow proposeFollow = new ProposeFollow();
                proposeFollow.setFollowTime(DateFormatUtils.format(jsonObject.getInteger("intime") * 1000L, "yyyy-MM-dd"));
                proposeFollow.setFollowVersion("跟进" + (followTraceDetail.size() - i));
                proposeFollow.setFollowStage(jsonObject.getString("jzjd") != null ? jsonObject.getString("jzjd") : "");
                proposeFollow.setFollowDesc(jsonObject.getString("i_suggestion") != null ? jsonObject.getString("i_suggestion") : "暂无跟进描述");
                jsonArray.add(proposeFollow);
            }
        }
        return jsonArray;
    }
    // 获取项目联系人列表
    private JSONArray getAllProposeCompany(JSONArray contactDetail) {
        JSONArray jsonArray = new JSONArray();
        if (contactDetail == null || contactDetail.size() == 0) {
            return jsonArray;
        }
        // 需要根据业主类型进行排序  order by type asc
        contactDetail.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("type")));
        for (int i = 0; i < contactDetail.size(); i++) {
            ProposeCompany proposeCompany = new ProposeCompany();
            JSONObject jsonObject = contactDetail.getJSONObject(i);
            proposeCompany.setCompanyType(KeyUtils.getLinkmanTypeForKey(jsonObject.getInteger("type").toString()));
            proposeCompany.setCompanyName(jsonObject.getString("dwmc") != null ? jsonObject.getString("dwmc") : "");
            proposeCompany.setLinkman(jsonObject.getString("lxr") != null ? jsonObject.getString("lxr") : "");
            proposeCompany.setDuty(jsonObject.getString("zhiwei") != null ? jsonObject.getString("zhiwei") : "");
            proposeCompany.setPhone(jsonObject.getString("phone") != null ? jsonObject.getString("phone") : "");
            proposeCompany.setMobile(jsonObject.getString("mobile") != null ? jsonObject.getString("mobile") : "");
            proposeCompany.setFax(jsonObject.getString("fax") != null ? jsonObject.getString("fax") : "");
            proposeCompany.setCompanyAddress(jsonObject.getString("addr") != null ? jsonObject.getString("addr") : "");
            jsonArray.add(proposeCompany);
        }
        return jsonArray;
    }
    private List<ProposeCompany> getAllProposeCompanyToList(JSONArray contactDetail) {
        List<ProposeCompany> list = new ArrayList<>();
        if (contactDetail == null || contactDetail.size() == 0) {
            return list;
        }
        // 需要根据业主类型进行排序  order by type asc
        contactDetail.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("type")));
        for (int i = 0; i < contactDetail.size(); i++) {
            ProposeCompany proposeCompany = new ProposeCompany();
            JSONObject jsonObject = contactDetail.getJSONObject(i);
            //proposeCompany.setCompanyType(KeyUtils.getLinkmanTypeForKey(jsonObject.getInteger("type").toString()));
            proposeCompany.setCompanyType(jsonObject.getInteger("type").toString());
            proposeCompany.setCompanyName(jsonObject.getString("dwmc") != null ? jsonObject.getString("dwmc") : "");
            proposeCompany.setLinkman(jsonObject.getString("lxr") != null ? jsonObject.getString("lxr") : "");
            proposeCompany.setDuty(jsonObject.getString("zhiwei") != null ? jsonObject.getString("zhiwei") : "");
            proposeCompany.setPhone(jsonObject.getString("phone") != null ? jsonObject.getString("phone") : "");
            proposeCompany.setMobile(jsonObject.getString("mobile") != null ? jsonObject.getString("mobile") : "");
            proposeCompany.setFax(jsonObject.getString("fax") != null ? jsonObject.getString("fax") : "");
            proposeCompany.setCompanyAddress(jsonObject.getString("addr") != null ? jsonObject.getString("addr") : "");
            list.add(proposeCompany);
        }
        return list;
    }
}
