package com.qianlima.offline.dao;

import com.qianlima.offline.bean.TencentIndustryCompany;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TencentIndustryCompanyRepository {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    public TencentIndustryCompany findByName(String company) {
        if (StringUtils.isBlank(company)){
            return null;
        }
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT first_industry, second_industry,third_industry,company FROM tencent_industry_company where company = ?", company);
        if (maps != null && maps.size() > 0){
            TencentIndustryCompany tencentIndustryCompany = new TencentIndustryCompany();
            tencentIndustryCompany.setFirstIndustry(maps.get(0).get("first_industry").toString());
            tencentIndustryCompany.setSecondIndustry(maps.get(0).get("second_industry").toString());
            tencentIndustryCompany.setThirdIndustry(maps.get(0).get("third_industry").toString());
            tencentIndustryCompany.setCompany(maps.get(0).get("company").toString());
            return tencentIndustryCompany;
        }
        return null;
    }
}
