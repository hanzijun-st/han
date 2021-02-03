package com.qianlima.offline.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.TargetService;
import com.qianlima.offline.rule02.BiaoDiWuRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NewBiaoDiWuService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    // 标的物匹配到的关键词
    private String[] keywords = {"拍片机","医用X线机","高频X线机","X线摄影系统","医用诊断X光","高频X光机","摄影X光机","医用x光机","数字x光机","X光机","摄影X射线机","高频X射线摄影机","X射线拍片机","X线拍片机",
            "X射线机","体检透视机","医学拍片","医用拍片","医疗拍片","拍片仪","拍片设备","床旁机","床边X线机","床旁X光机","移动X光机","移动DR","床边机","骨科小C","C型臂","C形臂","C臂",
            "小C臂","介入C臂","影增C臂","移动式C形臂","骨科小型C","小型C臂","移动式C形臂X射线机","胃肠机","多功能X线机","遥控X线机","胃肠系统","遥控X光机","胃肠诊断","遥控医用诊断X射线机",
            "X光透视拍片机","多功能数字化胃肠X线机","多功能数字化胃肠造影X光机","数字化X射线遥控透视摄影系统","数字化遥控胃肠X光机","数字胃肠","数字化透视摄影系统","数字多功能X光",
            "平板多功能X线透视","动态平板透视摄影系统","动态平板","透视摄影X射线机","数字化透视摄影X射线机","胃肠X射线机","医用诊断X射线透视摄影系统","X射线胃肠诊断床","数字化透视X射线机",
            "医学透视摄影","医用透视摄影","医疗透视摄影","透视摄影仪","透视摄影机","透视摄影设备","x光透视机","透视X射线机","数字X","数字化X","平板X","平板摄影","平板摄片","直接X","X线数字",
            "数字化X射线成像系统","平板DR","医用诊断X射线机","数字X线摄影","计算机X线摄影","动态DR","U臂DR","数字化医用X射线影像系统","悬吊DR","医学X光","医学X线","医学X射线","医学DR","医用X光",
            "医用X线","医用X射线","医用DR","医疗X光","医疗X线","医疗X射线","医疗DR","X光设备","X线机","X线设备","X射线设备","DR仪","DR机","DR设备","DR","DSA","血管机组","血管机","血管造影","大C",
            "大型血管介入治疗","外周血管造影机","大型血管介入治疗系统","大型心血管介入治疗系统","平板血管机","平板血管造影机","大型平板心血管介入治疗系统","直接转换型平板血管机",
            "直接转换式平板血管造影机","数字减影","血管造影X射线机","数字X线血管机","血管造影设备","减影仪","减影机","减影设备","剪影血管造影仪","正电子发射型计算机断层显像","正电子发射断层成像设备",
            "PET","PET-CT","PET/CT","PETCT","骨密度","X线双能量","骨密度仪","骨密度检测仪","双能量X线","双能X线","X射线骨密度仪","双能X射线骨密度仪","骨密度机","骨密度设备","X射线"};

    private String SQL = "insert into han_new_bdw(infoId, sum, sum_unit, keyword, serial_number, name, brand, model, " +
            "number, number_unit, price, price_unit, total_price, total_price_unit, configuration) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void handleForData(Long contentId,Integer type){

        String url = "";
        for (BiaoDiWuRule value : BiaoDiWuRule.values()) {
            if (value.getValue().intValue() == type){
                url = value.getName();
            }
        }
        String result = TargetService.extract(contentId,url);
        if (StringUtils.isNotBlank(result)){
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject != null && jsonObject.containsKey("content_target")){
                JSONObject resultObject = jsonObject.getJSONObject("content_target");
                if (resultObject != null && resultObject.containsKey("target_details")){
                    String sum = resultObject.getString("sum");
                    String sum_unit = resultObject.getString("sum_unit");
                    JSONArray targetDetails = resultObject.getJSONArray("target_details");
                    if (targetDetails != null && targetDetails.size() > 0){
                        for (int i = 0; i < targetDetails.size(); i++) {
                            String serial_number = "";
                            String name = "";
                            String brand = "";
                            String model = "";
                            String number = "";
                            String number_unit = "";
                            String price = "";
                            String price_unit = "";
                            String total_price = "";
                            String total_price_unit = "";
                            String configuration = "";
                            String keyword = "";
                            JSONObject finalObject = targetDetails.getJSONObject(i);
                            if (finalObject != null){
                                serial_number = finalObject.getString("serial_number");
                                name = finalObject.getString("name");
                                brand = finalObject.getString("brand");
                                model = finalObject.getString("model");
                                number = finalObject.getString("number");
                                number_unit = finalObject.getString("number_unit");
                                price = finalObject.getString("price");
                                price_unit = finalObject.getString("price_unit");
                                total_price = finalObject.getString("total_price");
                                total_price_unit = finalObject.getString("total_price_unit");
                                JSONArray configurations = finalObject.getJSONArray("configurations");
                                if (configurations != null && configurations.size() > 0){
                                    for (int j = 0; j < configurations.size(); j++) {
                                        JSONObject jsonObject1 = configurations.getJSONObject(j);
                                        String key = jsonObject1.getString("key");
                                        String value = jsonObject1.getString("value");
                                        configuration += key + "：" + value + "：";
                                    }
                                }
                                if (StringUtils.isNotBlank(configuration)){
                                    configuration = configuration.substring(0, configuration.length() - 1);
                                }
                                // 进行匹配关键词操作
                                if (keywords != null && keywords.length > 0){
                                    String allField = name + "&" + brand + "&" + model + "&" + configuration;
                                    for (String key : keywords) {
                                        if (allField.toUpperCase().contains(key.toUpperCase())){
                                            keyword += key + "，";
                                        }
                                    }
                                    if (StringUtils.isNotBlank(keyword)){
                                        keyword = keyword.substring(0, keyword.length() - 1);
                                    }
                                }
                                // 进行数据保存操作
                            }
                            // 进行数据库保存操作
                            bdJdbcTemplate.update(SQL, contentId, sum, sum_unit, keyword, serial_number, name, brand, model, number, number_unit, price, price_unit, total_price, total_price_unit, configuration);
                        }
                    }
                }
            }
        }
    }

}
