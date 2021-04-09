package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.entity.HanTestMy;
import com.qianlima.offline.mapper.TestUserMapper;
import com.qianlima.offline.service.han.TestMyBatisService;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class TestMyBatisServiceImpl implements TestMyBatisService {
    @Resource
    private TestUserMapper testUserMapper;

    @Override
    public List testMyBatis() {
        List testList = testUserMapper.getTestList();
        return testList;
    }

    @Override
    public void saveDatas() {
        try {
            List<HanTestMy> datas = new ArrayList<>();
            List<String> list = LogUtils.readRule("bjDatasD");
            for (String s : list) {
                HanTestMy hanTestMy = new HanTestMy();
                hanTestMy.setContentId(Long.valueOf(s));
                datas.add(hanTestMy);
            }
            log.info("开始时间：{}",System.currentTimeMillis());
            testUserMapper.saveDatas(datas);
            log.info("结束时间:{}",System.currentTimeMillis());
        } catch (IOException e) {
            e.getMessage();
        }
    }
}