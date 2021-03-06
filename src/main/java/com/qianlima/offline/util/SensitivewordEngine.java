package com.qianlima.offline.util;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * 敏感词过滤工具类
 *
 */
public class SensitivewordEngine {


    /**
     * 基于多叉树的查找。
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        List<String> list = readKeyWord("D:\\id.txt");
        SensitivewordEngine.initKeyWord(list);

        String content = "<div> <div> 永昌传媒中心1#楼北侧专业用房装修工程- </div> <div> 发布时间： 2019-08-27 </div> <p>中标公示发布登记表</p><p>  编号： </p><table border=\"1\"><tr><td><p>招标人</p></td><td colspan=\"8\"><p>保山市永昌投资开发（集团）有限公司</p></td><td colspan=\"3\"><p>联系人</p></td><td colspan=\"4\"><p>王东伟：13094360177 </p></td></tr><tr><td><p>工程名称</p></td><td colspan=\"15\"><p>永昌传媒中心1#楼北侧专业用房装修工程</p></td></tr><tr><td><p>招标代理机构</p></td><td colspan=\"15\"><p>保山市东升建设工程招标代理有限公司</p></td></tr><tr><td><p>招标备案编号</p></td><td colspan=\"7\"><p>ZBBA53050019071901</p></td><td colspan=\"4\"><p>招标方式</p></td><td colspan=\"4\"><p>√公开 □邀请</p></td></tr><tr><td><p>招标类别</p></td><td colspan=\"15\"><p>√施工 ￡ 监理 □ 设计 □ 勘察 □ 设备 ￡ 材料 □其他</p></td></tr><tr><td><p>开标时间</p></td><td colspan=\"15\"><p>2019年8月23日15时00分</p></td></tr><tr><td><p>建设规模</p></td><td colspan=\"13\"><p>永昌传媒中心1#楼北侧专业用房装修工程，主要建设内容包括：3-18层专业用房声学装修；1000m2、380m2演播厅声学装修； 1000m2、380m2演播厅专业装修,具体内容见工程量清单。</p></td><td><p>结构</p><p>类型</p></td><td><p> </p></td></tr><tr><td><p>公示时间</p></td><td colspan=\"15\"><p>2019年8月27日至2019年8月29日</p></td></tr><tr><td><p>采用评标办法</p></td><td colspan=\"3\"><p>综合评分法</p></td><td colspan=\"3\"><p>拦标价(万元)</p></td><td colspan=\"5\"><p>1092.275373</p></td><td colspan=\"3\"><p>投标人数量</p></td><td><p>4</p></td></tr><tr><td><p>拟中标人</p></td><td colspan=\"6\"><p>云南京昌建设工程有限公司</p></td><td colspan=\"5\"><p>联系人及电话</p></td><td colspan=\"4\"><p>唐文鑫0875-2120696</p></td></tr><tr><td><p>中标价(万元)</p></td><td colspan=\"4\"><p>1081.352148</p></td><td colspan=\"2\"><p>投标质量</p></td><td colspan=\"9\"><p>合格</p></td></tr><tr><td colspan=\"7\"><p>√工期 □交货期 □监理期限 □设计/勘察期限</p></td><td colspan=\"9\"><p>180日历天</p></td></tr><tr><td colspan=\"2\"><p>项目经理</p></td><td colspan=\"4\"><p>唐文鑫</p></td><td colspan=\"4\"><p>注册编号</p></td><td colspan=\"6\"><p>滇205181900021</p></td></tr><tr><td colspan=\"16\"><p>该工程经评标委员会评审，推荐出中标候选人情况如下：</p></td></tr><tr><td colspan=\"3\"><p>第一中标候选人</p></td><td colspan=\"8\"><p>云南京昌建设工程有限公司</p></td><td colspan=\"2\"><p>得分</p></td><td colspan=\"3\"><p>96.96</p></td></tr><tr><td colspan=\"3\"><p>第二中标候选人</p></td><td colspan=\"8\"><p>云南维琦建设工程有限公司</p></td><td colspan=\"2\"><p>得分</p></td><td colspan=\"3\"><p>95.62</p></td></tr><tr><td colspan=\"3\"><p>第三中标候选人</p></td><td colspan=\"8\"><p>云南保山鹏程建筑工程有限责任公司</p></td><td colspan=\"2\"><p>得分</p></td><td colspan=\"3\"><p>94.87</p></td></tr><tr><td colspan=\"16\"><p>中标情况说明：</p><p>推选第一中标候选人：云南京昌建设工程有限公司</p></td></tr><tr><td colspan=\"16\"><p>根据《中华人民共和国招标投标法》及相关法律、法规的规定，现将该项目中标结果予以公示，接受社会监督。如有异议请于公示结束日期前向招标人、同级监管部门、行业主管部门实名书面投诉。</p><p>招标人公章：</p><p> 2019年8月26日</p></td></tr></table><p> </p> </div>";
        long start = System.currentTimeMillis();
        Set<String> sensitiveWord = getSensitiveWord(content, 1);
        long end = System.currentTimeMillis();
        System.out.println(end - start +":"+ sensitiveWord.size());

        //
        HashSet<String> strings = new HashSet<>();
        long start01 = System.currentTimeMillis();
        for (String s : list) {
            if (content.contains(s)){
                strings.add(s);
            }
        }

        long end01 = System.currentTimeMillis();
        System.out.println(end01 - start01+":"+ strings.size());
    }



    public static List<String> readKeyWord(String path) throws Exception {
        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists() || file.isDirectory()){
            file.createNewFile();
        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
            list.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return list;
    }

    /**
     * 敏感词库
     */
    public static HashMap sensitiveWordMap = new HashMap();


    public static synchronized void initKeyWord(List<String> list) {
        if (sensitiveWordMap.isEmpty()){
            if (list != null && list.size() > 0){
                addSensitiveWordToHashMap(list);
            }
        }
    }

    /**
     * 封装敏感词库
     */
    private static void addSensitiveWordToHashMap(List<String> keyWordSet) {
        // 初始化HashMap对象并控制容器的大小
        sensitiveWordMap = new HashMap(keyWordSet.size());
        // 敏感词
        String key = null;
        // 用来按照相应的格式保存敏感词库数据
        Map nowMap = null;
        // 用来辅助构建敏感词库
        Map<String, String> newWorMap = null;
        // 使用一个迭代器来循环敏感词集合
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            // 等于敏感词库，HashMap对象在内存中占用的是同一个地址，所以此nowMap对象的变化，sensitiveWordMap对象也会跟着改变
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                // 截取敏感词当中的字，在敏感词库中字为HashMap对象的Key键值
                char keyChar = key.charAt(i);
                // 判断这个字是否存在于敏感词库中
                Object wordMap = nowMap.get(keyChar);
                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    newWorMap = new HashMap<String, String>();
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }
                // 如果该字是当前敏感词的最后一个字，则标识为结尾字
                if (i == key.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 过滤所有敏感词
     */
    public static int maxMatchType = 2;

    /**
     * 获取敏感词内容
     */
    public static Set<String> getSensitiveWord(String txt, int matchType) {
        Set<String> sensitiveWordList = new HashSet<String>();
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i, matchType);
            if (length > 0) {
                // 将检测出的敏感词保存到集合中
                sensitiveWordList.add(txt.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return sensitiveWordList;
    }

    /**
     * 检查敏感词数量
     */
    public static int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        boolean flag = false;
        // 记录敏感词数量
        int matchFlag = 0;
        char word = 0;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            // 判断该字是否存在于敏感词库中
            nowMap = (Map) nowMap.get(word);
            if (nowMap != null) {
                matchFlag++;
                // 判断是否是敏感词的结尾字，如果是结尾字则判断是否继续检测
                if ("1".equals(nowMap.get("isEnd"))) {
                    flag = true;
                    // 判断过滤类型，如果是小过滤则跳出循环，否则继续循环
                    if (SensitivewordEngine.maxMatchType == matchType) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        if (!flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }

}