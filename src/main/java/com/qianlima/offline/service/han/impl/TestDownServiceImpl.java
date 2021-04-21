package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.TestDownService;
import com.qianlima.offline.util.FileHelperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestDownServiceImpl implements TestDownService {
    @Override
    public ResponseEntity<byte[]> downFile() {
        return FileHelperUtil.downloadFile("任务说明_file0.txt", "E:\\downExcelFile\\任务说明_file0.txt");
    }

    @Override
    public void downExcel() {

    }
}