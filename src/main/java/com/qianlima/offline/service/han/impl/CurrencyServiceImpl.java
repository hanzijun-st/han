package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.CurrencyService;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2021/1/14.
 */
@Service
public class CurrencyServiceImpl implements CurrencyService {
    @Override
    public String getProgidStr(String str) {
        if ("1".equals(str)){
            //全部
            str = "[0 TO 3] OR progid:5";
        }else if ("2".equals(str)){
            //招标
            str = "[0 TO 2]";
        }else if ("3".equals(str)){
            //中标
            str = "3 OR progid:5";
        }
        return str;
    }
}
