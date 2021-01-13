package com.qianlima.offline.util;


import com.qianlima.offline.bean.Area;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyUtils {

    //地区
    private final static HashMap<String, Area> areaMap = new HashMap<String, Area>();


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


}
