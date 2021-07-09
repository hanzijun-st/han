package com.qianlima.offline.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KeyUtils {

    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();

    // 项目类别
    private final static HashMap<String, String> itemType = new HashMap<String, String>();

    // 联系人类型
    private final static HashMap<String, String> linkmanType = new HashMap<String, String>();

    public static String getLinkmanTypeForKey(String key) {
        if (linkmanType.isEmpty()) {
            linkmanType.put("1", "业主单位");
            linkmanType.put("2", "设计单位");
            linkmanType.put("3", "施工单位");
            linkmanType.put("4", "开发商");
            linkmanType.put("5", "政府机关");
            linkmanType.put("6", "代建公司");
            linkmanType.put("7", "酒店管理");
            linkmanType.put("8", "幕墙分包商");
            linkmanType.put("9", "室内装修分包商");
            linkmanType.put("10", "钢结构分包商");
            linkmanType.put("11", "机电分包商");
            linkmanType.put("12", "电气分包商");
            linkmanType.put("13", "空调分包商");
            linkmanType.put("14", "电梯分包商");
            linkmanType.put("15", "弱电分包商");
            linkmanType.put("16", "消防分包商");
        }
        if (linkmanType.containsKey(key)) {
            return linkmanType.get(key);
        }
        return null;
    }

    public static synchronized Map<String, String> getAreaMap(String areaId) {
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


    public static synchronized HashMap<String, String> getItemTypeMap() {
        if (itemType.isEmpty()) {
            try {
                ClassPathResource classPathResource = new ClassPathResource("map/nzjType.txt");
                InputStream inputStream = classPathResource.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bufferedReader.readLine();
                while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
                    String[] split = line.split(":");
                    itemType.put(split[0], split[1]);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                log.error("读取nzjType 失败, 请查证原因");
            }
        }
        return itemType;
    }

}
