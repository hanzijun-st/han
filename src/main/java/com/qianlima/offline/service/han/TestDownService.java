package com.qianlima.offline.service.han;

import com.qianlima.offline.entity.ZhongXinBean;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TestDownService {
    ResponseEntity<byte[]> downFile();

    void downExcel();

    void upFileToMySql_1(String url) throws Exception;

    void upFileToMySql_2(String url) throws Exception;

    /**
     * 删除数据-企业名录-天眼查库表1
     */
    void del1();
    void del2();

    /**
     * 企业名录-数据一
     * @return
     */
    List<ZhongXinBean> getList1();
    List<ZhongXinBean> getList2();
}