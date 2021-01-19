package com.qianlima.offline.util;

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
public class CleanUtils {

    @Autowired
    private MyRuleUtils myRuleUtils;

    // 通用规则（全局）
    private static String cleanAll(String content){
        if (StringUtils.isBlank(content)){
            return "";
        }
        String[] keys = { "&quotjyfs","&nbsp","&#","&lt","&gt","&amp","&quot" };
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
        String[] keys = { "支持" ,"手机" ,"委托" ,"售价" ,"需求" ,"可向" ,"或" ,"拉杆式" ,"容积率" ,"登录人员" ,"邮编" ,"万元" ,"摄像机" ,"定稿" ,"报价" ,"地址" ,"音频" ,"格式H" ,"第一次" ,"方式" ,"按键" ,"评分" ,"时间" ,"日期" ,"全网通" ,"www" ,"交换机" ,"便携式" ,"负责人" ,"经理" ,"加速老化" ,"招标文件" ,"税款" ,"更正" ,"专用设备" ,"采购项目" ,"系统升级" ,"租赁" ,"例如" ,"赫兹" ,"线下" ,"采样" ,"安装" ,"服务器" ,"加速卡" ,"论证意见" ,"报名" ,"1080P" ,"fps" ,"flag" ,"标记" ,"DNS" ,"IP" ,"金额" ,"格式" ,"信噪" ,"照度" ,"证书" ,"激活" ,"控制价" ,"磁盘" ,"兼容" ,"链路" ,"接口" ,"参与者" ,"茶盏" ,"文件费" ,"公司" ,"联系人" ,"免提" ,"选型" ,"陶板" ,"㎡" ,"可查" ,"流率" ,"工期" ,"扩容" ,"至今" ,"测试" ,"模块" ,"</p>" ,"</td>" ,"()" ,"（/）" ,"</span>" ,"High" ,"（SVC)" ,"押金" ,"年度" ,"预算" ,"发货" ,"内容" ,"nbsp" ,"\r" ,"\n" ,"\t" ,"\\a" ,"\b" ,"\f" ,"\\v" ,"\\" ,"\'" ,"\"" ,"\\?" ,"\0" ,"\\x" ,"\\h" ,"[]" ,"&quotjyfs" ,"&nbsp" ,"&#" ,"&lt" ,"&gt" ,"&amp" ,"&quot" ,"&copy" ,"&apos" };
        for (String key : keys) {
            if (xmNumber.contains(key)){
                return "";
            }
        }
        xmNumber = xmNumber.replaceAll(":", "");
        xmNumber = xmNumber.replaceAll(";", "");
        xmNumber = xmNumber.replaceAll("\"", "");
        return xmNumber;
    }

    /**
     * 处理招标预算、中标金额
     */
    public String cleanAmount(String amount){
        if (StringUtils.isBlank(amount)){
            return "";
        }
        amount = cleanAll(amount);
        if (! match(amount)){
            return "";
        }
        if (new BigDecimal(amount).compareTo(new BigDecimal(100)) < 0){
            return "";
        }
        String[] keys = { "/","元","%","‰","*","?"};
        for (String key : keys) {
            if (amount.contains(key)){
                return "";
            }
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
        String industry = myRuleUtils.getIndustry(zhaoBiaoUnit);
        if ("行业待分类".equals(industry)){
            return "";
        }
        return zhaoBiaoUnit;
    }

    /**
     * 处理中标单位
     */
    public String cleanZhongBiaoUnit(String zhongBiaoUnit){
        if (StringUtils.isBlank(zhongBiaoUnit)){
            return "";
        }
        String newZhongBiaoUnit = "";
        zhongBiaoUnit = cleanAll(zhongBiaoUnit);
        String[] split = zhongBiaoUnit.split("、");
        for (String s : split) {
            String industry = myRuleUtils.getIndustry(s);
            if (! "行业待分类".equals(industry)){
                newZhongBiaoUnit += s + ConstantBean.RULE_SEPARATOR_NAME;
            }
        }
        newZhongBiaoUnit = StringUtils.isNotBlank(newZhongBiaoUnit) ?
                newZhongBiaoUnit.substring(0, newZhongBiaoUnit.length() - 1) : "";
        return newZhongBiaoUnit;
    }

    /**
     * 处理代理机构
     */
    public String cleanAgentUnit(String agentUnit){
        if (StringUtils.isBlank(agentUnit)){
            return "";
        }
        agentUnit = cleanAll(agentUnit);
        String industry = myRuleUtils.getIndustry(agentUnit);
        if ("行业待分类".equals(industry)){
            return "";
        }
        return agentUnit;
    }

    /**
     * 处理联系人
     */
    public String cleanLinkMan(String linkMan){
        if (StringUtils.isBlank(linkMan)){
            return "";
        }
        linkMan = cleanAll(linkMan);
        String[] keys = { "保密","原件","文件","公司","印章","盖章","名称","方式","地址","方法","成交","单位","材料","明书","复印","元件","项目","相关","相应","印刷"};
        for (String key : keys) {
            if (linkMan.contains(key)){
                return "";
            }
        }
        return linkMan;
    }

    /**
     * 处理联系方式
     */
    public String cleanLinkWay(String linkWay){
        if (StringUtils.isBlank(linkWay)){
            return "";
        }
        linkWay = cleanAll(linkWay);
        linkWay = linkWay.length() <= 7 ? "" : linkWay;
        if (StringUtils.isBlank(linkWay)){
            return "";
        }
        String[] keys = { "*"};
        for (String key : keys) {
            if (linkWay.contains(key)){
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
        dateTime = cleanAll(dateTime);
        Long dateForTime = 0L;
        Long oldTime = 0L;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR,3);
        Long currentForThreeYearTime = calendar.getTime().getTime();
        try {
            dateForTime = DateUtils.parseDate(dateTime, "yyyy-MM-dd HH:mm:ss").getTime();
            oldTime = DateUtils.parseDate("2020-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime();
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
