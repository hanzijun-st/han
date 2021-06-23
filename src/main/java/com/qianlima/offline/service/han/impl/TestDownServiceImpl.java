package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.entity.ZhongXinBean;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestDownService;
import com.qianlima.offline.util.CollectionUtils;
import com.qianlima.offline.util.FileHelperUtil;
import com.qianlima.offline.util.MapUtil;
import com.qianlima.offline.util.ReadExcelUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TestDownServiceImpl implements TestDownService {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    @Override
    public ResponseEntity<byte[]> downFile() {
        return FileHelperUtil.downloadFile("任务说明_file0.txt", "E:\\downExcelFile\\任务说明_file0.txt");
    }

    @Override
    public void downExcel() {

    }

    @Override
    public void upFileToMySql_1(String url) throws Exception {
        //获取企业名称
        List<String> list = ReadExcelUtil.readExcel(url);
        if (CollectionUtils.isEmpty(list)){
            throw new RuntimeException("输入的地址文件中无数据，请重新换地址文件");
        }
        currencyService.getZhongXinZiDong1(list);
        log.info("调用天眼查接口成功");
    }
    @Override
    public void upFileToMySql_2(String url) throws Exception {
        //获取企业名称
        List<String> list = ReadExcelUtil.readExcel(url);
        if (CollectionUtils.isEmpty(list)){
            throw new RuntimeException("输入的地址文件中无数据，请重新换地址文件");
        }
        currencyService.getZhongXinZiDong2(list);
        log.info("调用天眼查接口成功");
    }

    @Override
    public void del1() {
        bdJdbcTemplate.update("DELETE  FROM han_zhongxin_gy1 ");
    }

    @Override
    public void del2() {
        bdJdbcTemplate.update("DELETE  FROM han_zhongxin_gy2 ");
    }

    @Override
    public List<ZhongXinBean> getList1() {
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT * FROM han_zhongxin_gy1 ");
        List<ZhongXinBean> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(mapList)){
            for (Map<String, Object> map : mapList) {
                list.add(MapUtil.mapToBean(map,ZhongXinBean.class));
            }
        }
        return list;
    }

    @Override
    public List<ZhongXinBean> getList2() {
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT * FROM han_zhongxin_gy2 ");
        List<ZhongXinBean> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(mapList)){
            for (Map<String, Object> map : mapList) {
                list.add(MapUtil.mapToBean(map,ZhongXinBean.class));
            }
        }
        return list;
    }
}