package com.qianlima.offline.service;

import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.BaiLianZhongTaiService;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.middleground.NiZaiJianService;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.util.FbsContentSolr;
import com.qianlima.offline.util.IctContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HyService {

    @Autowired
    private FbsContentSolr contentSolr;

    @Autowired
    private IctContentSolr ictContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NotBaiLianZhongTaiService notBaiLianZhongTaiService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private BaiLianZhongTaiService baiLianZhongTaiService;

    @Autowired
    private ZhongTaiBiaoDiWuService zhongTaiBiaoDiWuService;

    @Autowired
    private ZhongTaiBiaoDiWuServiceForOne zhongTaiBiaoDiWuServiceForOne;

    @Autowired
    private MyRuleUtils myRuleUtils;

    @Autowired
    private NiZaiJianService niZaiJianService;


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

    //调取中台数据  拟在建项目 catid 101
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = notBaiLianZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = notBaiLianZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            notBaiLianZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    //调取中台数据 并 匹配行业标签
    public void getDataFromZhongTaiAndSave2(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> map = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (map != null) {
            String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = myRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if (zhaobiaosplit[1].contains("通信") || zhaobiaosplit[1].contains("互联网") || zhaobiaosplit[1].contains("运营商") || zhaobiaosplit[1].contains("系统集成")){
                newZhongTaiService.saveIntoMysql(map);
            }
        }
    }

    //调取中台数据 多个关键词后追加 (有黑词)
    public void getDataFromZhongTaiAndSave3(NoticeMQ noticeMQ) throws Exception {

        String[] jjq = {"安防","报警","测速","电警","对讲","防爆","隔爆","慧眼","火眼","技防","监控","亮化","林火","门禁","天网","天眼","违停","雪亮","烟感","治超","抓拍","消防","安检","办案","安全","靶标","班牌","冰屏","车载","称重","触控","大屏","灯光","稽查","监管","矩阵","留置","陆侧","录播","门锁","拼接","拼控","视频","视讯","违法","显控","显示","巡检","巡视","云屏","闸口","照明","读表","矫正","联网","谈话","铁塔","停车","液晶","指挥","智慧","考场","卡口","固废","AFC","BRT","COB","DID","DLP","ETC","LCD","LED","PID"};
        String[] mmh = {"摄像头", "录像机", "摄像机", "拍摄仪", "摄影仪", "监控台", "高拍仪", "视频卡", "电子眼", "安联网", "智能锁", "监视器", "报警柱", "不停车", "布控球", "二道门", "非现场", "和慧眼", "红绿灯", "千里眼", "全球眼", "水雨情", "信号灯", "视频监控", "视频预警", "监控系统", "监控设备", "视频信号", "视频设备", "摄像设备", "摄录设备", "球机设备", "监视设备", "会议设备", "抓拍设备", "监督设备", "视频终端", "轻型云台", "手持云台", "液压云台", "全景云台", "拼接云台", "会议云台", "视频会议", "会议视频", "会议系统", "监控工程", "监控建设", "监控服务", "监控项目", "全景监控", "高清监控", "高点监控", "高空监控", "视频电警", "视频监察", "视频监测", "视频监督", "视频监管", "视频监视", "视频督促", "视频督察", "视频督查", "视频防控", "监视控制", "监测视频", "监控视频", "警务视频", "动环监控", "集中监控", "实时监控", "监控报警", "摄像系统", "电子警察", "电子停车", "安防监控", "监控专网", "视频安防", "视频联防", "视频安保", "视频巡查", "视频巡逻", "视频抓拍", "视频警察", "视频治安", "视频查勘", "视频考场", "报警视频", "安防视频", "监管视频", "监督视频", "视频图像", "视频系统", "视频采集", "视频分析", "视频解析", "视频侦查", "视频检测", "视频定位", "定位视频", "视频督导", "视频取证", "安全防卫", "安全护卫", "安全防范", "安防中控", "安防实训", "安防执勤", "安防指挥", "安防监管", "安防管控", "安防维保", "安防防火", "安防防范", "治安防控", "安防集成", "摄像安防", "移动安防", "安防联网", "云防系统", "安全系统", "安监系统", "安防系统", "人脸识别", "语音识别", "报警系统", "巡更系统", "安检安防", "门禁系统", "对讲系统", "可视对讲", "一键报警", "智能安防", "安防联动", "天网工程", "天眼工程", "平安城市", "雪亮工程", "天眼系统", "机房监控", "消防器材", "安检设备", "安全辅助", "安全管控", "安全管理", "安消联动", "车路协同", "车牌识别", "城市内涝", "城市排水", "城市停车", "堤坝防范", "堤防监管", "底盘扫描", "地质监测", "防火联网", "防火宣传", "分散管理", "辅助监控", "高空瞭望", "灌区监管", "河道安防", "河道监控", "集群对讲", "监管联网", "监控中心", "景区安防", "景区监控", "可视通讯", "蓝天卫士", "乐园安防", "联动预警", "联网监控", "粮仓监控", "粮库安防", "粮库安全", "排放监测", "平安景点", "平安景区", "平安乡村", "平安校园", "平安医院", "全程监控", "人脸考勤", "人员定位", "入侵检测", "森林防火", "森林火情", "视频矩阵", "视频联网", "视频巡检", "视频直存", "水厂监控", "水库监测", "水库监管", "水利巡检", "水位监测", "水位预警", "水务巡检", "水政执法", "寺庙安防", "图像识别", "卫星定位", "污染监测", "无感办税", "无人值守", "物体追踪", "箱号识别", "消防头盔", "消防装备", "校园安防", "烟草安防", "医院安防", "医院监控", "医院门禁", "婴儿防盗", "应急辅助", "应急救援", "应急通信", "应急指挥", "远程审讯", "远程探视", "远程提讯", "闸站监测", "执法记录", "智安小区", "周界入侵", "主动发现", "驻监驻所", "自动报靶", "自动导航", "自动导引", "综合视频", "智慧安防", "智慧停车", "智慧消防", "监控电子屏", "球形投影仪", "球形摄像机", "球型投影仪", "球型摄像机", "云台摄像仪", "音视频会议", "监控点建设", "监控点项目", "监控站项目", "监控音视频", "互联网监控", "视频监测站", "自动化安防", "安防云系统", "安防子系统", "可视化对讲", "防爆摄像机", "消防机器人", "火灾探测器", "生命探测器", "烟感报警器", "烟感探测器", "博物馆安防", "科技馆监控", "可视化应急", "林业局防火", "平安博物馆", "水资源监控", "探测与识别", "特殊监管区", "体育馆安防", "体育馆监控", "图书馆安防", "图书馆监控", "物联网消防", "消防物联网", "养殖场安防", "养殖场监控", "引调水监管", "站台端防护", "监控分析设备", "上线视频会议", "音视频会议室", "执法视频保全", "监控联网应用", "在线视频监测", "视频存储采购", "安全保障系统", "安全探测系统", "安全监管系统", "安全防护系统", "安全预警系统", "安监预警系统", "安防广播系统", "安防模拟系统", "安防集成系统", "智能视频分析", "智能消防头盔", "5G视频车载", "车辆动态监控", "车务视频联网", "船舶动态监控", "工业视频系统", "公路运输监管", "火灾检测预警", "景区安全防范", "粮仓安全防范", "山洪灾害监测", "视频监控系统", "视频综合平台", "水利工程监管", "围界报警系统", "消防指挥系统", "校车监管平台", "校园安全防范", "隐蔽报警系统", "应急视频会议", "综合视频监控", "图像及视频分析", "博物馆安全防范", "飞行区监控系统", "教育局安防联网", "热成像访客系统", "饮用水安全监管", "“T”一脸通关", "视频会议网络系统", "视频联合辅助运营", "矿用本安型摄像机", "独立式感烟报警器", "独立式烟感报警器", "全景视频监控系统", "文保单位安全防范", "医院安全防范系统", "指定居所监视居住", "视频会议室专用设备", "视频会议多点控制器", "矿用隔爆型摄像机 ", "飞行区道口管理系统", "污染源在线监测联网", "独立式感烟火灾探测器", "播放机", "查询机", "广告机", "叫号机", "取号机", "调度机", "投影机", "无人机", "信号机", "控制器", "拼接器", "显示器", "常规屏", "大屏幕", "带鱼屏", "电视墙", "多媒体", "格栅屏", "公交云", "交通屏", "可视化", "拼接屏", "拼接墙", "条形屏", "透明屏", "网格化", "微间距", "污染源", "显示屏", "小间距", "液晶屏", "一卡通", "移动源", "诱导屏", "云会议", "云课堂", "云录播", "云指挥", "震慑屏", "直播屏", "智能化", "智慧屏", "智能城市", "智能城镇", "城市大脑", "城市超脑", "数字城市", "感知城市", "无线城市", "数字城镇", "感知城镇", "无线城镇", "智能交通", "智能机场", "智能车站", "智能客运", "智能客流", "智能物流", "智能调度", "智能运输", "智能货运", "智能仓储", "智能分拣", "智能工厂", "智能家居", "智能建筑", "智能矿山", "智能理货", "智能小区", "智能冶炼", "智能云屏", "非机动车", "AI农业", "CCTV", "LED屏", "OLED", "北斗定位", "背投拼接", "表计识别", "殡葬联网", "超限检测", "车辆调度", "车载终端", "存储扩容", "存储设备", "大学设备", "单兵系统", "道路运输", "电子白板", "电子班牌", "电子巡查", "电子巡航", "电子巡考", "电子站牌", "动力环境", "动态监管", "断点续传", "辅助安全", "工业电视", "公交站牌", "供电系统", "国土卫士", "河道采砂", "互动课堂", "货运检测", "基层治理", "集成指挥", "交互电视", "交通大脑", "交通执法", "教室中控", "科技法庭", "扩容集中", "录播系统", "路网中心", "门架系统", "民政联网", "明厨亮灶", "末端配送", "拼接单元", "人大会议", "融合通信", "冗余备份", "三级诱导", "三维建模", "社会治理", "生态感知", "市域治理", "视讯教室", "手术示教", "数据底座", "数据平台", "数据中台", "数字标牌", "数字茶园", "数字城管", "数字法庭", "数字农业", "数字乡村", "数字油田", "税务纳服", "算法平台", "算法中台", "调度平台", "调度终端", "庭审直播", "同步课堂", "投影显示", "透明厨房", "图像处理", "图像控制", "网格管理", "违规采矿", "未来社区", "未来乡村", "文旅直播", "显示控制", "显示系统", "小学设备", "校车车载", "校园广播", "信息发布", "学校设备", "学院设备", "烟草物流", "阳光厨房", "养老联网", "液晶电视", "液晶拼接", "液晶显示", "一键调度", "移动稽查", "运行监测", "在线教育", "在线巡考", "执行指挥", "指挥平台", "指挥调度", "指挥系统", "中学设备", "专用电视", "综合布线", "综合执法", "综合指挥", "作业管控", "智慧城市", "智慧城镇", "智慧交通", "智慧机场", "智慧车站", "智慧客运", "智慧客流", "智慧物流", "智慧调度", "智慧运输", "智慧货运", "智慧办税", "智慧泵站", "智慧殡葬", "智慧菜场", "智慧城管", "智慧出行", "智慧畜牧", "智慧党建", "智慧灯杆", "智慧地铁", "智慧福利", "智慧港口", "智慧高速", "智慧工厂", "智慧工地", "智慧公交", "智慧管道", "智慧管网", "智慧轨道", "智慧国土", "智慧行政", "智慧化工", "智慧环保", "智慧家庭", "智慧监管", "智慧街道", "智慧景区", "智慧矿山", "智慧炼化", "智慧粮仓", "智慧粮库", "智慧林场", "智慧林业", "智慧旅游", "智慧农牧", "智慧农业", "智慧社区", "智慧水厂", "智慧水利", "智慧水务", "智慧小区", "智慧小镇", "智慧校园", "智慧烟草", "智慧养护", "智慧养老", "智慧养殖", "智慧医院", "智慧用电", "智慧渔业", "智慧园区", "智慧治理", "智慧种植", "灌区智能化", "触控一体机", "触摸一体机", "存储广告机", "会议一体机", "机场指挥部", "机房服务器", "交互一体机", "无人机反制", "信号控制机", "液晶一体机", "硬盘录像机", "多屏处理器", "液晶显示器", "LCD拼接", "安卓播放盒", "标准化考场", "菜场数字化", "车辆大数据", "车辆段设备", "创新实验室", "大棚温湿度", "堤防精细化", "电子显示屏", "多媒体教学", "古村落保护", "户外广告屏", "环保监测站", "环保一张图", "检委会会议", "交通诱导屏", "教学信息化", "教育信息化", "教育云平台", "可变情报板", "可视化系统", "可视化指挥", "粮仓可视化", "粮仓信息化", "林业信息化", "农产品溯源", "农业物联网", "农业信息化", "三维可视化", "数字化预案", "水利可视化", "水利信息化", "水务一体化", "无缝拼接屏", "无缝显示屏", "信息发布屏", "信息化大棚", "信息化设备", "信息化养殖", "蓄水量监测", "液晶拼接屏", "医院停车场", "饮用水源地", "油田物联网", "幼儿园设备", "智慧博物馆", "智慧电梯屏", "智慧公交屏", "智慧加油站", "智慧冶炼 ", "视音频资源库", "车载智能服务", "智能调度系统", "智能调度终端", "分布式广告机", "存储磁盘阵列", "电视电话会议", "防汛抗旱指挥", "户外LED屏", "交通应急指挥", "景区客流统计", "林业资源管理", "融合通信调度", "同步录音录像", "无缝显示系统", "校车联网系统", "信息发布系统", "信息管理系统", "信息管理终端", "自动物流叉车", "智能移动机器人", "人脸识别一体机", "可视化指挥调度", "空勤证管理系统", "指挥所信息系统", "自动导航运输车", "综合交通管理平台", "综合信息服务平台", "智慧行政监管中心", "飞行区", "云存储", "云发布", "云平台", "出入口", "数据中心", "指挥大厅", "指挥中心", "视频会议室改造", "视频会议室建设", "视频会议室技改", "AGV"};
        String[] ddw = {"消防","安防","安检"};
        String[] yyw = {"视频", "视频卡", "视频监控", "视频预警", "视频信号", "视频设备", "视频终端", "视频会议", "会议视频", "视频电警", "视频监察", "视频监测", "视频监督", "视频监管", "视频监视", "视频督促", "视频督察", "视频督查", "视频防控", "监测视频", "监控视频", "警务视频", "视频安防", "视频联防", "视频安保", "视频巡查", "视频巡逻", "视频抓拍", "视频警察", "视频治安", "视频查勘", "视频考场", "报警视频", "安防视频", "监管视频", "监督视频", "视频图像", "视频系统", "视频采集", "视频分析", "视频解析", "视频侦查", "视频检测", "视频定位", "定位视频", "视频督导", "视频取证", "视频矩阵", "视频联网", "视频巡检", "视频直存", "综合视频", "音视频会议", "监控音视频", "视频监测站", "上线视频会议", "音视频会议室", "执法视频保全", "在线视频监测", "视频存储采购", "智能视频分析", "5G视频车载", "车务视频联网", "工业视频系统", "视频监控系统", "视频综合平台", "应急视频会议", "综合视频监控", "图像及视频分析", "视频会议网络系统", "视频联合辅助运营", "全景视频监控系统", "视频会议室专用设备", "视频会议多点控制器", "视频会议室改造", "视频会议室建设", "视频会议室技改", "监控", "监控台", "监控系统", "监控设备", "监控工程", "监控建设", "监控服务", "监控项目", "全景监控", "高清监控", "高点监控", "高空监控", "动环监控", "集中监控", "实时监控", "监控报警", "安防监控", "监控专网", "机房监控", "辅助监控", "河道监控", "监控中心", "景区监控", "联网监控", "粮仓监控", "全程监控", "水厂监控", "医院监控", "监控电子屏", "监控点建设", "监控点项目", "监控站项目", "互联网监控", "科技馆监控", "水资源监控", "体育馆监控", "图书馆监控", "养殖场监控", "监控分析设备", "监控联网应用", "车辆动态监控", "船舶动态监控", "飞行区监控系统", "安防", "安防中控", "安防实训", "安防执勤", "安防指挥", "安防监管", "安防管控", "安防维保", "安防防火", "安防防范", "治安防控", "安防集成", "摄像安防", "移动安防", "安防联网", "安防系统", "安检安防", "智能安防", "安防联动", "河道安防", "景区安防", "乐园安防", "粮库安防", "寺庙安防", "校园安防", "烟草安防", "医院安防", "智慧安防", "自动化安防", "安防云系统", "安防子系统", "博物馆安防", "体育馆安防", "图书馆安防", "养殖场安防", "安防广播系统", "安防模拟系统", "安防集成系统", "教育局安防联网", "安全", "巡视", "安全管理", "消防", "消防器材", "消防头盔", "消防装备", "智慧消防", "消防机器人", "物联网消防", "消防物联网", "智能消防头盔", "消防指挥系统", "会议系统", "摄像系统", "云防系统", "安全系统", "安监系统", "报警系统", "巡更系统", "门禁系统", "对讲系统", "天眼系统", "安全保障系统", "安全探测系统", "安全监管系统", "安全防护系统", "安全预警系统", "安监预警系统", "围界报警系统", "隐蔽报警系统", "热成像访客系统", "医院安全防范系统", "飞行区道口管理系统", "报警", "摄像头", "录像机", "摄像机", "拍摄仪", "摄影仪", "高拍仪", "电子眼", "安联网", "智能锁", "监视器", "报警柱", "布控球", "二道门", "千里眼", "全球眼", "信号灯", "摄像设备", "摄录设备", "球机设备", "监视设备", "会议设备", "抓拍设备", "监督设备", "轻型云台", "手持云台", "液压云台", "全景云台", "拼接云台", "会议云台", "监视控制", "安检设备", "球形投影仪", "球形摄像机", "球型投影仪", "球型摄像机", "云台摄像仪", "可视化对讲", "防爆摄像机", "火灾探测器", "生命探测器", "烟感报警器", "烟感探测器", "矿用本安型摄像机", "独立式感烟报警器", "独立式烟感报警器", "矿用隔爆型摄像机 ", "污染源在线监测联网", "独立式感烟火灾探测器", "门锁", "信号机", "AGV", "硬盘录像机", "背投拼接", "测速", "电警", "对讲", "防爆", "隔爆", "慧眼", "火眼", "技防", "林火", "门禁", "天网", "天眼", "违停", "雪亮", "烟感", "抓拍", "安检", "不停车", "非现场", "红绿灯", "电子警察", "电子停车", "安全防卫", "安全护卫", "安全防范", "人脸识别", "语音识别", "可视对讲", "一键报警", "天网工程", "天眼工程", "平安城市", "雪亮工程", "安全辅助", "安全管控", "安消联动", "车路协同", "车牌识别", "城市内涝", "城市排水", "城市停车", "堤坝防范", "堤防监管", "底盘扫描", "地质监测", "防火联网", "防火宣传", "高空瞭望", "灌区监管", "集群对讲", "监管联网", "可视通讯", "蓝天卫士", "联动预警", "粮库安全", "排放监测", "平安景点", "平安景区", "平安乡村", "平安校园", "平安医院", "人脸考勤", "人员定位", "入侵检测", "森林防火", "森林火情", "水库监测", "水库监管", "水利巡检", "水位监测", "水位预警", "水务巡检", "水政执法", "图像识别", "卫星定位", "污染监测", "无感办税", "无人值守", "物体追踪", "箱号识别", "医院门禁", "婴儿防盗", "应急辅助", "应急救援", "应急通信", "应急指挥", "远程审讯", "远程探视", "远程提讯", "闸站监测", "执法记录", "智安小区", "周界入侵", "主动发现", "驻监驻所", "自动报靶", "自动导航", "自动导引", "可视化应急", "林业局防火", "平安博物馆", "探测与识别", "特殊监管区", "引调水监管", "站台端防护", "公路运输监管", "火灾检测预警", "景区安全防范", "粮仓安全防范", "山洪灾害监测", "水利工程监管", "校车监管平台", "校园安全防范", "博物馆安全防范", "饮用水安全监管", "“T”一脸通关", "文保单位安全防范", "指定居所监视居住", "视讯教室", "同步课堂", "视讯课堂", "矫正", "互动课堂"};
        String[] blacks = {"车辆采购","车辆购置","车辆维修","车辆保险","车辆维保","整车采购","车辆保管","Office","皮卡车采购","食品集中配送","采购硒鼓","车采购","印刷服务","宽带租赁","餐饮管理服务","车服务采购","宿舍租赁","房屋租赁","地铺出租","室出租","地块出租","铺出租","房出租","招租公告","房产招租","房招租","房屋招租","物业招租","厂出租","塔吊租赁","摊位出租","定点维修","车辆保养及维修","电子产品维修服务","办公家具","购置小型轿车","加油、维修","车辆集中采购","保洁服务","建设设计","建筑设计","项目设计","工程设计","施工设计","规划设计","设计规划","规划编制","编制规划","设计图","设计施工","勘察设计","设计建造"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
            String taskId = resultMap.get("task_id") != null ? resultMap.get("task_id").toString() : "";
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                if(taskId.equals("1")){
                    for (String jq : jjq) {
                        for (String dw : ddw) {
                            for (String yw : yyw) {
                                if (title.contains(jq) && content.contains(dw) && title.contains(yw)) {
                                    String key = jq + "&" + dw + "&" + yw;
                                    keyword += (key + "、");
                                }
                            }
                        }
                    }
                }
                if (taskId.equals("2")){
                    for (String mh : mmh) {
                        for (String dw : ddw) {
                            for (String yw : yyw) {
                                if (content.contains(mh) && content.contains(dw) && title.contains(yw)){
                                    String key = mh + "&" + dw + "&" + yw;
                                    keyword += (key + "、");
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
//            zhongTaiBiaoDiWuServiceForOne.getAllZhongTaiBiaoDIWu(contentId);
        }

    }

    //调取中台数据 多个关键词后追加 (有黑词)
    public void getDataFromZhongTaiAndSave33(NoticeMQ noticeMQ) throws Exception {

        String[] ffc = {"显控", "控制器", "拼接器", "显示器", "可视化", "拼接屏", "显示屏", "液晶屏", "诱导屏", "LED屏", "存储扩容", "存储设备", "拼接单元", "图像处理", "图像控制", "显示控制", "显示系统", "液晶拼接", "户外广告屏", "交通诱导屏", "信息发布屏", "云存储", "冰屏", "触控", "大屏", "矩阵", "拼控", "视讯", "显示", "云屏", "液晶", "常规屏", "大屏幕", "带鱼屏", "电视墙", "格栅屏", "交通屏", "拼接墙", "条形屏", "透明屏", "震慑屏", "直播屏", "智慧屏", "多屏处理器", "电子显示屏", "无缝拼接屏", "无缝显示屏", "液晶拼接屏", "户外LED屏", "液晶显示器", "液晶显示", "专用电视", "投影显示", "存储磁盘阵列", "无缝显示系统", "交互电视", "智能化", "智能城市", "智能城镇", "智能交通", "智能机场", "智能车站", "智能客运", "智能客流", "智能物流", "智能调度", "智能运输", "智能货运", "智能仓储", "智能分拣", "智能工厂", "智能家居", "智能建筑", "智能矿山", "智能理货", "智能小区", "智能冶炼", "智能云屏", "灌区智能化", "车载智能服务", "智能调度系统", "智能调度终端", "智能移动机器人", "智慧", "智慧城市", "智慧城镇", "智慧交通", "智慧机场", "智慧车站", "智慧客运", "智慧客流", "智慧物流", "智慧调度", "智慧运输", "智慧货运", "智慧办税", "智慧泵站", "智慧殡葬", "智慧菜场", "智慧城管", "智慧出行", "智慧畜牧", "智慧党建", "智慧灯杆", "智慧地铁", "智慧福利", "智慧港口", "智慧高速", "智慧工厂", "智慧工地", "智慧公交", "智慧管道", "智慧管网", "智慧轨道", "智慧国土", "智慧行政", "智慧化工", "智慧环保", "智慧家庭", "智慧监管", "智慧街道", "智慧景区", "智慧矿山", "智慧炼化", "智慧粮仓", "智慧粮库", "智慧林场", "智慧林业", "智慧旅游", "智慧农牧", "智慧农业", "智慧社区", "智慧水厂", "智慧水利", "智慧水务", "智慧小区", "智慧小镇", "智慧校园", "智慧烟草", "智慧养护", "智慧养老", "智慧养殖", "智慧医院", "智慧用电", "智慧渔业", "智慧园区", "智慧治理", "智慧种植", "智慧博物馆", "智慧电梯屏", "智慧公交屏", "智慧加油站", "智慧冶炼 ", "智慧行政监管中心", "智慧停车", "城市大脑", "城市超脑", "感知城市", "无线城市", "数字城镇", "无线城镇", "数字城市", "未来社区", "未来乡村", "交通大脑", "DLP", "ETC", "无人机", "信号机", "一卡通", "电子白板", "电子班牌", "电子巡查", "电子巡航", "电子巡考", "电子站牌", "数字茶园", "数字城管", "数字法庭", "数字农业", "数字乡村", "数字油田", "触控一体机", "触摸一体机", "交互一体机", "人脸识别一体机", "播放机", "查询机", "广告机", "叫号机", "取号机", "调度机", "投影机", "存储广告机", "会议一体机", "液晶一体机", "菜场数字化", "可变情报板", "数字标牌", "亮化", "多媒体", "录播", "巡检污染源", "移动源", "云课堂", "云录播", "北斗定位", "殡葬联网", "超限检测", "车辆调度", "车载终端", "单兵系统", "道路运输", "动态监管", "断点续传", "工业电视", "国土卫士", "河道采砂", "货运检测", "基层治理", "集成指挥", "交通执法", "科技法庭", "录播系统", "路网中心", "社会治理", "生态感知", "市域治理", "手术示教", "调度平台", "调度终端", "庭审直播", "同步课堂", "校车车载", "烟草物流", "养老联网", "一键调度", "移动稽查", "在线教育", "在线巡考", "执行指挥", "指挥平台", "指挥调度", "指挥系统", "综合执法", "综合指挥", "作业管控", "无人机反制", "信号控制机", "车辆大数据", "多媒体教学", "教学信息化", "教育信息化", "教育云平台", "可视化系统", "可视化指挥", "粮仓可视化", "粮仓信息化", "林业信息化", "分布式广告机", "交通应急指挥", "景区客流统计", "融合通信调度", "校车联网系统", "信息管理终端", "可视化指挥调度", "综合交通管理平台", "云发布", "云平台", "公交云", "云会议", "云指挥", "辅助安全", "教室中控", "门架系统", "运行监测检委会会议", "农产品溯源", "农业物联网", "农业信息化", "三维可视化", "水利可视化", "水利信息化", "水务一体化", "信息化大棚", "信息化设备", "信息化养殖", "蓄水量监测", "油田物联网", "视音频资源库", "电视电话会议", "防汛抗旱指挥", "林业资源管理", "同步录音录像", "自动物流叉车", "空勤证管理系统", "指挥所信息系统", "自动导航运输车", "AFC", "BRT", "PID", "LED", "OLED", "LCD", "CCTV"};
        String[] blacks = {"车辆采购","车辆购置","车辆维修","车辆保险","车辆维保","整车采购","车辆保管","Office","皮卡车采购","食品集中配送","采购硒鼓","车采购","印刷服务","宽带租赁","餐饮管理服务","车服务采购","宿舍租赁","房屋租赁","地铺出租","室出租","地块出租","铺出租","房出租","招租公告","房产招租","房招租","房屋招租","物业招租","厂出租","塔吊租赁","摊位出租","定点维修","车辆保养及维修","电子产品维修服务","办公家具","购置小型轿车","加油","车辆集中采购","保洁","车辆采购","车辆购置","车辆维修","车辆保险","车辆维保","整车采购","硫化","矫正心理","矫正法","矫正印刷","矫正教育"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {

            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";

            String keyword = "";
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                for (String fc : ffc) {
                    if (title.contains(fc)){
                        keyword += (fc + "、");
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
//            zhongTaiBiaoDiWuServiceForOne.getAllZhongTaiBiaoDIWu(contentId);
        }

    }

}
