package com.qianlima.offline.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 美敦力
 * Created by Administrator on 2021/1/15.
 */
@Data
public class MdlTargetDetailsVo {

    private String serialNumber;
    private String name;
    private String brand;
    private String model;
    private String number;
    private String numberUnit;
    private String price;
    private String priceUnit;
    private String totalPrice;
    private String totalPriceUnit;

    List<MdlConfigurationsVo> configurations = new ArrayList<>();
}
