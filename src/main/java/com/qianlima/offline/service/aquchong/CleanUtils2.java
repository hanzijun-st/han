package com.qianlima.offline.service.aquchong;

import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.rule02.MyRuleUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CleanUtils2 {

    @Autowired
    private MyRuleUtils myRuleUtils;

    // 通用规则（全局）
    private static String cleanAll(String content){
        if (StringUtils.isBlank(content)){
            return "";
        }
        String[] keys = { "&quotjyfs","&nbsp","&#","&lt","&gt","&amp","&quot", " "};
        for (String key : keys) {
            content = content.replaceAll(key, "");
        }
        content = content.replaceAll("【", "[");
        content = content.replaceAll("】", "]");
        content = content.replaceAll("（", "(");
        content = content.replaceAll("）", ")");
        content = content.replaceAll("：", ":");
        content = content.replaceAll("“", "\"");
        content = content.replaceAll("”", "\"");
        content = content.replaceAll("——", "-");
        content = content.replaceAll("_", "-");
        content = content.replaceAll("《", "<");
        content = content.replaceAll("》", ">");
        content = content.replaceAll("？", "?");
        content = content.replaceAll("！", "!");
        content = content.replaceAll("；", ";");
        content = content.replaceAll("，", ",");
        content = content.replaceAll("。", ".");
        content = content.replaceAll("∶", ":");
        content = content.replaceAll("－", "-");
        content = content.replaceAll("—", "-");
        content = content.replaceAll("·", ".");
        return content;
    }

    /**
     * 处理标题
     */
    public String cleanTitle(String title){
        if (StringUtils.isBlank(title)){
            return "";
        }
        title = cleanAll(title);
        title = title.replaceAll("公告", "公示");
        return title;
    }
    /**
     * 处理省市县
     */
    public String cleanProvince(String province){
        if (StringUtils.isBlank(province)){
            return "";
        }
        province = cleanAll(province);
        province = province.replaceAll("全国", "");
        return province;
    }

    /**
     * 处理项目编号
     */
    public String cleanXmNumber(String xmNumber){
        if (StringUtils.isBlank(xmNumber)){
            return "";
        }
        xmNumber = cleanAll(xmNumber);
        xmNumber = xmNumber.length() <= 2 ? "" : xmNumber;
        if (StringUtils.isBlank(xmNumber)){
            return "";
        }
        String[] keys = { "|","φ","null","控制","根据","甲方","搜索","位置","规格","合同额","名称","联系电话","主线长","资信证明","1元","2元","3元","4元","5元","6元","7元","8元","9元","0元","修改版","二维码","护套线","基本信息","有效期", "强度","面积","上午","下午","中午","屈服","弯曲","伸缩","拉伸", "支持" ,"手机" ,"委托" ,"售价" ,"需求" ,"可向" ,"或" ,"拉杆式" ,"容积率" ,"登录人员" ,"邮编" ,"万元" ,"摄像机" ,"定稿" ,"报价" ,"地址" ,"音频" ,"格式H" ,"第一次" ,"方式" ,"按键" ,"评分" ,"时间" ,"日期" ,"全网通" ,"www" ,"交换机" ,"便携式" ,"负责人" ,"经理" ,"加速老化" ,"招标文件" ,"税款" ,"更正" ,"专用设备" ,"采购项目" ,"系统升级" ,"租赁" ,"例如" ,"赫兹" ,"线下" ,"采样" ,"安装" ,"服务器" ,"加速卡" ,"论证意见" ,"报名" ,"1080P" ,"fps" ,"flag" ,"标记" ,"DNS" ,"IP" ,"金额" ,"格式" ,"信噪" ,"照度" ,"证书" ,"激活" ,"控制价" ,"磁盘" ,"兼容" ,"链路" ,"接口" ,"参与者" ,"茶盏" ,"文件费" ,"公司" ,"联系人" ,"免提" ,"选型" ,"陶板" ,"㎡" ,"可查" ,"流率" ,"工期" ,"扩容" ,"至今" ,"测试" ,"模块" ,"</p>" ,"</td>" ,"()" ,"（/）" ,"</span>" ,"High" ,"（SVC)" ,"押金" ,"年度" ,"预算" ,"发货" ,"内容" ,"nbsp" ,"\r" ,"\n" ,"\t" ,"\\a" ,"\b" ,"\f" ,"\\v" ,"\\" ,"\'" ,"\"" ,"\\?" ,"\0" ,"\\x" ,"\\h" ,"[]" ,"&quotjyfs" ,"&nbsp" ,"&#" ,"&lt" ,"&gt" ,"&amp" ,"&quot" ,"&copy" ,"&apos","负偏离","项目名称","作者","XX]","万条","---","规范","年月日","年至","web","及在" ,"(/)","(SVC)","身份证","竞价人","选用","公示期","计划","应用功能建设","RDF油滤芯","人制","/span","投资总额","吨/天","应答人", "税价", "开标","作废", "xx号","注册建造师","添加剂","投标人","待定","采购人","代理机构编号","任务书编号","类型","结果公告","垃圾清运","评标情况","总承包","使用","选址","船级社检","绿化服务框架","2018年","2019年","2020年","2021年","2022年","2023年","代收付服务","2021-2023年","招标人","2020年-2021年","专业分包","住宅楼等","注册" };
        for (String key : keys) {
            if (xmNumber.toUpperCase().contains(key.toUpperCase())){
                return "";
            }
        }
        xmNumber = xmNumber.replaceAll(":", "");
        xmNumber = xmNumber.replaceAll(";", "");
        xmNumber = xmNumber.replaceAll("\"", "");
        xmNumber = xmNumber.replaceAll("\\?", "");
        xmNumber = xmNumber.replaceAll("\\.", "");
        xmNumber = xmNumber.replaceAll("\\*", "");
        xmNumber = xmNumber.replaceAll("#", "");
        xmNumber = xmNumber.replaceAll("\\\\", "");
        xmNumber = xmNumber.replaceAll("&", "");
        xmNumber = xmNumber.replaceAll("null", "");
        return xmNumber;
    }


//    public static void main(String[] args) {
//        CleanUtils cleanUtils = new CleanUtils();
//        String s = cleanUtils.cleanXmNumber("\"GXZH2019HW031");
//        System.out.println(s);
//    }


    // 招标预算（自提接口）
    public String cleanBudget(String amount, String budget){
        // 处理招标预算、中标金额
        budget = cleanAmount(budget);
        if (StringUtils.isBlank(budget)){
            return "";
        }
        budget = cleanAmount(budget);
        //  覆盖清除“招标预算-有值”＜“中标金额-有值”内容
        if (StringUtils.isNotBlank(amount) && new BigDecimal(budget).compareTo(new BigDecimal(amount)) < 0){
            return "";
        }
        return budget;
    }


    // 中标金额（自提接口）
    private String cleanAmount(String amount){
        if (StringUtils.isBlank(amount)){
            return "";
        }
        amount = cleanAll(amount);
        if (amount.startsWith("0")){
            return "";
        }
        if (! match(amount)){
            return "";
        }
        if (new BigDecimal(amount).compareTo(new BigDecimal(100)) < 0){
            return "";
        }
        if (new BigDecimal(amount).compareTo(new BigDecimal(1000000000000L)) > 0){
            return "";
        }
        return amount;
    }

    /**
     * 处理招标预算、中标金额
     */
    public String cleanAmount(String amount, String title, String progid){
        amount = cleanAmount(amount);
        // 覆盖清除（标题不含有<字符G>，同时progid为0、1，且中标金额有值）内容
        if (! title.contains("单一") && ("0".equals(progid) || "1".equals(progid)) && StringUtils.isNotBlank(amount)){
            return "";
        }
        return amount;
    }


    private static boolean match(String amount) {
        String reg = "^\\d+$|^\\d+.\\d+$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(amount);
        return m.matches();
    }

    /**
     * 处理招标单位
     */
    public String cleanZhaoBiaoUnit(String zhaoBiaoUnit){
        if (StringUtils.isBlank(zhaoBiaoUnit)){
            return "";
        }
        zhaoBiaoUnit = cleanAll(zhaoBiaoUnit);
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll(":", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll(";", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("\"", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("\\?", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("\\.", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("\\*", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("#", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("\\\\", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("&", "");
        zhaoBiaoUnit = zhaoBiaoUnit.replaceAll("null", "");
        return zhaoBiaoUnit;
    }


    /**
     * 处理中标单位
     */
    public String cleanZhongBiaoUnit(String zhongBiaoUnit, String zhaoBiaoUnit, String title, String progid){
        if (StringUtils.isBlank(zhongBiaoUnit)){
            return "";
        }
        String newZhongBiaoUnit = "";
        zhongBiaoUnit = cleanAll(zhongBiaoUnit);
        String[] split = zhongBiaoUnit.split("、");

        for (String s : split) {
            String industry = myRuleUtils.getIndustry(s);
            if (! "行业待分类".equals(industry)){
                newZhongBiaoUnit += s + ConstantBean.RULE_SEPARATOR_02;
            } else {
                String[] suffixs = {"商场","商贸","事务所","工作室","超市","工坊","文印","图文","数码","装饰","会","店","厂","场","行","队","社","城","站","院","电器","电脑","工业园","基地","馆","科技","保险","室","(有限合伙)","(普通合伙)","(联合体)","(小微企业)","(联合体牵头人)","(联合体成员)","(特殊普通合伙)","药房","车行","文印","广告","总汇","批发"};
                for (String suffix : suffixs) {
                    if (s.endsWith(suffix)){
                        newZhongBiaoUnit += s + ConstantBean.RULE_SEPARATOR_02;
                        break;
                    }
                }
            }
        }
        newZhongBiaoUnit = StringUtils.isNotBlank(newZhongBiaoUnit) ?
                newZhongBiaoUnit.substring(0, newZhongBiaoUnit.length() - 1) : "";

        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll(":", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll(";", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("\"", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("\\?", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("\\.", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("\\*", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("#", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("\\\\", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("&", "");
        newZhongBiaoUnit = newZhongBiaoUnit.replaceAll("null", "");
        // 覆盖清除 覆盖清除“招标单位=中标单位”内容
        if (StringUtils.isNotBlank(newZhongBiaoUnit)){
            zhaoBiaoUnit = cleanZhaoBiaoUnit(zhaoBiaoUnit);
            if (zhaoBiaoUnit.equals(newZhongBiaoUnit)){
                newZhongBiaoUnit = "";
            }
        }
        // 覆盖清除（标题不含有<字符G>，同时progid为0、1，且中标单位有值）内容
        if (StringUtils.isNotBlank(newZhongBiaoUnit) && ("0".equals(progid) || "1".equals(progid)) && ! title.contains("单一")){
            newZhongBiaoUnit = "";
        }
        // 只输出对应符合要求的前三家单位
        if (StringUtils.isNotBlank(newZhongBiaoUnit)){
            String tempZhongBiaoUnit = "";
            String[] split1 = newZhongBiaoUnit.split(ConstantBean.RULE_SEPARATOR_02);
            for (int i = 0; i < split1.length; i++) {
                if (i >= 3){
                    break;
                }
                tempZhongBiaoUnit += split1[i] + ConstantBean.RULE_SEPARATOR_02;
            }
            tempZhongBiaoUnit = StringUtils.isNotBlank(tempZhongBiaoUnit) ? tempZhongBiaoUnit.substring(0, tempZhongBiaoUnit.length() - 1) : "";
            newZhongBiaoUnit = tempZhongBiaoUnit;
        }
        return newZhongBiaoUnit;
    }

    /**
     * 处理代理机构
     */
    public String cleanAgentUnit(String agentUnit){
        if (StringUtils.isBlank(agentUnit)){
            return "";
        }
        if (agentUnit.contains("null")){
            return "";
        }
        agentUnit = cleanAll(agentUnit);
        String industry = myRuleUtils.getIndustry(agentUnit);
        if ("行业待分类".equals(industry)){
            return "";
        }
        agentUnit = agentUnit.replaceAll(":", "");
        agentUnit = agentUnit.replaceAll(";", "");
        agentUnit = agentUnit.replaceAll("\"", "");
        agentUnit = agentUnit.replaceAll("\\?", "");
        agentUnit = agentUnit.replaceAll("\\.", "");
        agentUnit = agentUnit.replaceAll("\\*", "");
        agentUnit = agentUnit.replaceAll("#", "");
        agentUnit = agentUnit.replaceAll("\\\\", "");
        agentUnit = agentUnit.replaceAll("&", "");
        agentUnit = agentUnit.replaceAll("null", "");
        return agentUnit;
    }

    /**
     * 处理联系人
     */
    public String cleanLinkMan(String linkMan, String progid, String title, boolean isZhongBiaoWay){
        if (StringUtils.isBlank(linkMan)){
            return "";
        }
        String[] key02s = {"电"};
        for (String key : key02s) {
            linkMan = linkMan.replaceAll(key, "");
        }
        linkMan = cleanAll(linkMan);
        String[] keys = { "时间","明的接","平台","参加","关注","文书","明和身","个人","废标","正式","明及授","明及法","委托","私章","商务","指定","段落","季节","来的须","工程","明或有","计划","石油","能够","详细","包括","杭州","公告","双方","符合","必须","保持","明外还","联系","过程","高级","全称","时不需","迟到","谈判","邮箱","办事","山西","保证","应急","应答","保养","招标","招采","扶贫","操作","修改","财险","明文","电话","支持", "保密","原件","文件","公司","印章","盖章","名称","方式","地址","方法","成交","单位","材料","明书","复印","元件","项目","相关","相应","印刷", "后在我","公开","后可见","公章","印件","商业","授权","山东","单一","盖本","包人","明或法","省","市","县","乡","镇","区","保证金","终止","煤矿","人员","具有","附法","标时","组织","中心","受理","咨询","相互","项的基","司的合","负责","武装","武警","全权","上海","宁波","到场","产品","北京","明竞商","明及其","明或授","满足","居民","经验","经办","广东","广州","简要","税务","管理","部门","关于","房屋","范围","注册","概况","安保","安徽","包含","保定","诚信","经营","包钢","准确","代建","持有","单价","甘肃","金属","阀门","包装","锻造","具备","原则","全面","编号","设计","包号","公平","工期","查实","单体","须在","初中","高中","公路","房租","弓印","承办","部队","原帅","文档","位于","书房","全国","权利","平凡","利用","劳务","经贸","金融","计统","过户","国有","人民","单号","成套","支票","包一","开机","纺织", "null", "路由器","报名","保障","客服","施工","管理","职务","采购","服务","沈阳","住所","粗糙","废钢","辐射","企业","资质","指定","一名","组织","最近","须附","房产","应在委"};
        for (String key : keys) {
            if (linkMan.contains(key)){
                return "";
            }
        }
        // 覆盖清除（标题不含有<字符G>，同时progid为0、1，且中标单位联系人有值）内容
        if (isZhongBiaoWay && StringUtils.isNotBlank(linkMan)){
            if ((! title.contains("单一")) && ("0".equals(progid) || "1".equals(progid))){
                return "";
            }
        }
        return linkMan;
    }





    /**
     * 处理联系方式
     */
    public String cleanLinkWay(String linkWay, String progid, String title, boolean isZhongBiaoWay){
        if (StringUtils.isBlank(linkWay)){
            return "";
        }
        linkWay = cleanAll(linkWay);
        linkWay = linkWay.length() < 7 ? "" : linkWay;
        if (StringUtils.isBlank(linkWay)){
            return "";
        }
        String[] keys = { "*"};
        for (String key : keys) {
            if (linkWay.contains(key)){
                return "";
            }
        }
        if (linkWay.startsWith("000")){
            return "";
        }
        if (linkWay.startsWith("1") && linkWay.length() < 11){
            return "";
        }
        // 覆盖清除（标题不含有<字符G>，同时progid为0、1，且中标单位联系人有值）内容
        if (isZhongBiaoWay && StringUtils.isNotBlank(linkWay)){
            if ((! title.contains("单一")) && ("0".equals(progid) || "1".equals(progid))){
                return "";
            }
        }

        return linkWay;
    }

    /**
     * 处理时间参数
     */
    public String cleanDateTime(String dateTime){
        if (StringUtils.isBlank(dateTime)){
            return "";
        }
        Long dateForTime = 0L;
        Long oldTime = 0L;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR,5);
        Long currentForThreeYearTime = calendar.getTime().getTime();
        try {
            dateForTime = DateUtils.parseDate(dateTime, "yyyy-MM-dd HH:mm:ss").getTime();
            oldTime = DateUtils.parseDate("2012-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime();
        } catch (Exception e){
            return "";
        }
        if (new BigDecimal(dateForTime).compareTo(new BigDecimal(oldTime)) < 0){
            return "";
        }
        if (new BigDecimal(dateForTime).compareTo(new BigDecimal(currentForThreeYearTime)) > 0){
            return "";
        }
        return dateTime;
    }
}
