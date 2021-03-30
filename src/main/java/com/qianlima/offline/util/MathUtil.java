package com.qianlima.offline.util;

import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigInteger;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MathUtil {

    private static final String DATE_SDF = "yyyy-MM-dd hh:mm:ss";
    /**
     *  校验金额是否是纯数字
     * @param amount
     * @return
     */
    public static boolean match(String amount){
        String reg = "^\\d+$|^\\d+.\\d+$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(amount);
        return  m.matches();
    }

    /**
     * 去除正文中的html内容
     */
    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
        Pattern p_script = Pattern.compile(
                regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");//过滤script标签
        Pattern p_style = Pattern.compile(
                regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签
        Pattern p_html = Pattern.compile(
                regEx_html, Pattern.CASE_INSENSITIVE);

        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签
        htmlStr = htmlStr.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n");
        htmlStr = htmlStr.trim(); //返回文本字符串
        return htmlStr;
    }

    public static String delHTMLAndBlock(String htmlStr) {
        int idx = htmlStr.indexOf("<block>");
        if (-1 != idx) {
            htmlStr = htmlStr.substring(0, idx);
        }
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
        java.util.regex.Pattern p_script = java.util.regex.Pattern.compile(
                regEx_script, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");//过滤script标签
        java.util.regex.Pattern p_style = java.util.regex.Pattern.compile(
                regEx_style, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签
        java.util.regex.Pattern p_html = java.util.regex.Pattern.compile(
                regEx_html, java.util.regex.Pattern.CASE_INSENSITIVE);

        java.util.regex.Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签
        htmlStr = htmlStr.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n");
        htmlStr = htmlStr.trim();
        //返回文本字符串
        return htmlStr;
    }

    /**
     * 校验日期
     *
     * @param updateTimeStr
     * @return
     */
    public static boolean checkDate(String updateTimeStr) {
        boolean result = false;
        try {
            Date updateTimeData = DateUtils.parseDate(updateTimeStr, DATE_SDF);
            Date nowData = new Date();
            long updateTime = updateTimeData.getTime() + 24*60*60*1000*90L;
            if (updateTime > nowData.getTime()) {
                result = true;
            }
        } catch (Exception e) {
            log.info("日期格式不正确, 日期转换异常");
        }
        return result;
    }

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
                    element.select("a").remove();
                }
            }
        }
        return document.body().html();
    }

    /**
     * 将数值的人民币转化为大写
     * 例如：123456789  转化为大写后变成：壹亿贰仟叁佰肆拾伍万陆仟柒佰捌拾玖元
     */
    public static String getAmountToDaXie(Double money){


        if (money == null || money <0){
            return null;
        }

        final char[] data = new char[]{'零','壹','贰','叁','肆','伍','陆','柒','捌','玖'};
        final char[] units = new char[]{'元','拾','佰','仟','万','拾','佰','仟','亿'};


        String[] split = money.toString().split("\\.");
        int zhengshu =Integer.valueOf(split[0].toString());
        int xiaoshu = Integer.valueOf(split[1].toString());
        StringBuffer sb = new StringBuffer();
        int unit = 0;
        while (zhengshu != 0){
            sb.insert(0, units[unit++]);
            int number = zhengshu % 10;
            sb.insert(0, data[number]);
            zhengshu /= 10;
        }

        if (xiaoshu > 0){
            if (xiaoshu <10){
                xiaoshu = xiaoshu * 10;
            }
            int g=xiaoshu%10;
            int sw=xiaoshu/10%10;

            sb.append(data[sw]+"角");

            if (g !=0){
                sb.append(data[g]+"分");
            }
        }else {
            sb.append("整");
        }
        return sb.toString();
    }

    /**
     *阶乘
     */
    public static BigInteger sum(int i){
        if (i == 1){
            return BigInteger.ONE;
        }
        return BigInteger.valueOf(i).multiply(sum(i-1));
    }

    /**
     * 累加 （例如：3以内之和------- 3+2+1=6）
     * @param i
     * @return
     */
    public static int plus(int i){
        if (i == 1){
            return 1;
        }
        return i+plus(i-1);
    }

}
