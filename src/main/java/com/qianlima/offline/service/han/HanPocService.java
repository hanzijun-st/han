package com.qianlima.offline.service.han;

public interface HanPocService {
    /**
     * 新接口
     */
    void getNew(Integer type,String date);

    /**
     * 浙江纽若思医疗科技有限公司
     * @param date
     */
    void getZheJiangNiuRuoSi(String date,Integer type);
}