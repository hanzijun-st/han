package com.qianlima.offline.service.han;

public interface TestYiLiaoHaoCaiService {

    /**
     *  医疗耗材
     * @param type
     * @param date
     * @param progidStr
     */
    void getYiliaohaocai(Integer type, String date, String progidStr);
}