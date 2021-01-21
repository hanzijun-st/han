package com.qianlima.offline.util;


import com.qianlima.offline.bean.Area;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KeyUtils {

    //地区
    private final static HashMap<String, Area> areaMap = new HashMap<String, Area>();

    //地区
    private final static HashMap<String, String> simpleAreaMap = new HashMap<>();

    public static synchronized HashMap<String, Area> getAreaMap(JdbcTemplate primaryJdbcTemplate) {
        if (areaMap.isEmpty()) {
            String sqlArea = "select areaid,name,arrparentid,parentid from  phpcms_area ";
            List<Map<String, Object>> areaList = primaryJdbcTemplate.queryForList(sqlArea);
            if (null != areaList && areaList.size() > 0) {
                for (Map<String, Object> map : areaList) {
                    Area area = new Area();
                    area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
                    area.setName((String) map.get("name"));
                    area.setArrparentid(String.valueOf(map.get("arrparentid")));
                    area.setParentid(String.valueOf(map.get("parentid")));
                    areaMap.put(String.valueOf(map.get("areaid")), area);
                }
            }
            Area area = new Area();
            area.setAreaid(0);
            area.setName("全国");
            area.setArrparentid("0");
            area.setParentid("0");
            areaMap.put("0", area);
        }
        return areaMap;
    }


    public static synchronized HashMap<String, String> getSimpleAreaMap() {
        if (simpleAreaMap.isEmpty()) {
            try {
                ClassPathResource classPathResource = new ClassPathResource("map/yiyuan.txt");
                InputStream inputStream = classPathResource.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bufferedReader.readLine();
                while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
                    line = line.trim();
                    String[] arr = line.split(":");
                    simpleAreaMap.put(arr[0], arr[1]);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                log.error("读取ka_simple_area 失败, 请查证原因");
            }
        }
        return simpleAreaMap;
    }


}
