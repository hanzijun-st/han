package com.qianlima.offline.controller;

import com.qianlima.offline.bean.Params;
import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/aolinbasi")
@Slf4j
@Api("hanpoc")
public class HanTestController {


    @Autowired
    private TestService testService;
    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private AoLinBaSiService aoLinBaSiService;

    @GetMapping("/start/getAolinbasiDatas")
    @ApiOperation("获取奥林巴斯的数据")
    public String getTestAllDatas(){
        //aoLinBaSiService.getAoLinBaSiAndSave();
        return "第一次测试数据---获取成功";
    }

    @GetMapping("/start/getUrl/{num}")
    @ApiOperation("获取原链接地址的数据")
    public String getUrl(@PathVariable("num") String num){
        //String urlOriginalLink = aoLinBaSiService.getUrlOriginalLink(num);
        return "成功获取url原链接地址---";
    }

    @GetMapping("/start/getBdw")
    @ApiOperation("获取标的物的数据")
    public String getBdw(Integer type){
        testService.getBdw(type);
        return "请求成功---成功获取标的物";
    }

    /**
     *  获取标的物
     * @param type
                    1、迈瑞接口地址：http://47.104.4.12:5001/to_json_v3/
                    2、[模型识别侧重“ICT行业”]：http://47.104.4.12:2022/inspect
                    3、[模型识别侧重“医疗行业”]：http://47.104.4.12:2023/inspect
                    4、[模型识别没有侧重点]：http://47.104.4.12:2024/inspect
     * @return
     */
    @GetMapping("/getNewBdw")
    @ApiOperation("最新方式-获取标的物的数据（1:迈瑞；2:ICT；3:医疗；4:没有侧重点；）")
    public String getNewBdw(Integer type){
        testService.getNewBdw(type);
        return "请求成功---最新方式-获取标的物的数据";
    }

    /**
     * 1个关键词
     * @param params
     * @return
     */
    @ApiOperation("一个关键词")
    @PostMapping("/start/getOne")
    public String getOne(@RequestBody Params params){
        currencyService.getOnePoc(params);
        return "---测试---";
    }


    @GetMapping("/start/updateKeyword")
    @ApiOperation("修改关键词")
    public String updateKeyword(){
        testService.updateKeyword();
        return "-----------修改关键词成功-----------";
    }

    @PostMapping("/start/test")
    @ApiOperation("测试接口返回")
    public String test(){
        return "-----------测试成功-----------";
    }

    @RequestMapping(value = "/downLoad", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    @ApiOperation("将数据导出excel")
    public String downLoad(){
        return testService.downLoad();
    }

    /**
     * 多个关键词
     * @param params
     * @return
     */
    @ApiOperation("天津众泰")
    @PostMapping("/start/getTianjin")
    public String getTianjin(@RequestBody Params params){
        String time1 = params.getTime1();
        String time2 = params.getTime2();
        String type = params.getType();
        String title = params.getTitle();
        aoLinBaSiService.getTianjin(time1,time2,type,title);
        return "---天津众泰---";
    }
    @ApiOperation("佳电(上海)管理有限公司")
    @PostMapping("/start/getJdgl")
    public String getJdgl(@RequestBody Params params){
        String time1 = params.getTime1();
        String time2 = params.getTime2();
        String type = params.getType();
        String title = params.getTitle();
        aoLinBaSiService.getJdgl(time1,time2,type,title);
        return "---佳电(上海)管理有限公司---";
    }

    @ApiOperation("测试批量导入数据库")
    @PostMapping("/start/save")
    public String save(){
        currencyService.saveList();
        return "123456789";
    }

    @ApiOperation("行业标签")
    @PostMapping("/start/getBiaoQian")
    public String getBiaoQian(@RequestParam("type") Integer type) throws Exception{
        currencyService.getBiaoQian(type);
        return "---123---";
    }

    @ApiOperation("匹配数据")
    @PostMapping("/start/getPpei")
    public String getPpei(){
        currencyService.getPpei();
        return "---123---";
    }

    @ApiOperation("校验-匹配数据")
    @PostMapping("/start/getPpeiJy")
    public String getPpeiJy(){
        currencyService.getPpeiJy();
        return "---123---";
    }
    @ApiOperation("上海联影医疗")
    @PostMapping("/start/getShangHaiLy")
    public String getShangHaiLy(){
        testService.getShangHaiLy();
        return "---getShangHaiLy---";
    }

    @ApiOperation("重庆地区2019至今每月的")
    @PostMapping("/start/getChongqi")
    public String getChongqi(){
        testService.getChongqi();
        return "---getChongqi---";
    }

    @ApiOperation("纵横大鹏无人机-规则三")
    @PostMapping("/getZongHengDaPeng3")
    public String getZongHengDaPeng3(Integer type,String date) throws Exception{
        testService.getZongHengDaPeng3(type,date);
        return "---getZongHengDaPeng3---";
    }

    @ApiOperation("合肥航联")
    @PostMapping("/getHefeiHanglian")
    public String getHefeiHanglian(Integer type,String date) throws Exception{
        testService.getHefeiHanglian(type,date);
        return "---getHefeiHanglian---";
    }
    @ApiOperation("邯郸开发区中电环境科技有限公司")
    @PostMapping("/getHanDanKaiFaQu")
    public String getHanDanKaiFaQu(Integer type,String date) throws Exception{
        testService.getHanDanKaiFaQu(type,date);
        return "---getHanDanKaiFaQu---";
    }
    @ApiOperation("四川羽医医疗管理有限公司")
    @PostMapping("/getSiChuanYuYiYiLiao")
    public String getSiChuanYuYiYiLiao(Integer type,String date) throws Exception{
        testService.getSiChuanYuYiYiLiao(type,date);
        return "---getSiChuanYuYiYiLiao---";
    }

    @ApiOperation("北京金万维科技有限公司")
    @PostMapping("/getJingWanWei")
    public String getJingWanWei(Integer type,String date) throws Exception{
        testService.getJingWanWei(type,date);
        return "---getJingWanWei---";
    }

    @GetMapping("/start/cs")
    @ApiOperation("测试")
    public String cs(){
        testService.getDaoJinSolrAllField();
        return "请求成功--";
    }

    @ApiOperation("北京宇信科技集团股份有限公司")
    @PostMapping("/getYuxin")
    public String getYuxin(Integer type,String date) throws Exception{
        testService.getYuxin(type,date);
        return "---getYuxin is ok---";
    }
    @ApiOperation("北京宇信科技集团股份有限公司-第二回合")
    @PostMapping("/getYuxin2")
    public String getYuxin2(Integer type,String date) throws Exception{
        testService.getYuxin2(type,date);
        return "---getYuxin is ok---";
    }

    @ApiOperation("北京宇信科技集团股份有限公司-第三回合")
    @PostMapping("/getYuxin3")
    public String getYuxin3(Integer type,String date) throws Exception{
        testService.getYuxin3(type,date);
        return "---getYuxin3 is ok---";
    }

    @ApiOperation("北京宇信科技集团股份有限公司-第一回合4.0")
    @PostMapping("/getYuxin1_4")
    public String getYuxin1_4(Integer type,String date) throws Exception{
        testService.getYuxin1_4(type,date);
        return "---getYuxin is ok---";
    }

    @ApiOperation("查找行业标签的错误问题")
    @PostMapping("/getError")
    public String getError(Integer type,String date) throws Exception{
        testService.getError(type,date);
        return "---getError is ok---";
    }

    @ApiOperation("北建工的穿透单位数据")
    @PostMapping("/getBeiJianGong")
    public String getBeiJianGong(String unit) throws Exception{
        //List<String> keyWords = LogUtils.readRule("keyWords");
        testService.getBeiJianGong(unit);
        return "---getBeiJianGong is ok---";
    }

    @ApiOperation("ICT大金额id")
    @PostMapping("/getDaJinE")
    public String getDaJinE() throws Exception{
        testService.getKaHangYeSolrAllField();
        log.info("===============================数据运行结束===================================");
        return "---getDaJinE is ok---";
    }

    @ApiOperation("文思海辉")
    @PostMapping("/getWenSiHaiHui")
    public String getWenSiHaiHui(Integer type,String date) throws Exception{
        testService.getWenSiHaiHuib( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getWenSiHaiHui is ok---";
    }

    @ApiOperation("文思海辉-2-规则一")
    @PostMapping("/getWenSiHaiHui2_1")
    public String getWenSiHaiHui2_1(Integer type,String date) throws Exception{
        testService.getWenSiHaiHuib2_1( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getWenSiHaiHui2 is ok---";
    }

    @ApiOperation("文思海辉-2-规则二")
    @PostMapping("/getWenSiHaiHui2_2")
    public String getWenSiHaiHui2_2(Integer type,String date) throws Exception{
        testService.getWenSiHaiHuib2_2( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getWenSiHaiHui2_2 is ok---";
    }

    @ApiOperation("奥林巴斯-第二回合")
    @PostMapping("/getAolinbasi2")
    public String getAolinbasi2(Integer type,String date) throws Exception{
        testService.getAolinbasi2( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getAolinbasi2 is ok---";
    }
    @ApiOperation("奥林巴斯-第二回合(全文检索关键词b)")
    @PostMapping("/getAolinbasi2_qw")
    public String getAolinbasi2_qw(Integer type,String date) throws Exception{
        testService.getAolinbasi2_qw( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getAolinbasi2_qw is ok---";
    }

    @ApiOperation("奥林巴斯-第二回合_规则3")
    @PostMapping("/getAolinbasi2_3")
    public String getAolinbasi2_3(Integer type,String date) throws Exception{
        testService.getAolinbasi2_3( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getAolinbasi2_3 is ok---";
    }

    @ApiOperation("贝登")
    @PostMapping("/getBeiDeng")
    public String getBeiDeng(Integer type,String date) throws Exception{
        testService.getBeiDeng( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getBeiDeng is ok---";
    }

    @ApiOperation("文思海辉-交付数据")
    @PostMapping("/getWensihaihui_Jiaofu")
    public String getWensihaihui_Jiaofu(Integer type,String date) throws Exception{
        testService.getWensihaihui_Jiaofu( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getWensihaihui_Jiaofu is ok---";
    }

    /**
     * 贝登第二次
     * @param type
     * @param date
     * @return
     * @throws Exception
     */
    @ApiOperation("贝登-2")
    @PostMapping("/getBeiDeng2")
    public String getBeiDeng2(Integer type,String date) throws Exception{
        testService.getBeiDeng2( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getBeiDeng2 is ok---";
    }

    @ApiOperation("云南獾少科技")
    @PostMapping("/getYuNanMaoShao")
    public String getYuNanMaoShao(Integer type,String date) throws Exception{
        testService.getYuNanMaoShao( type, date);
        log.info("===============================数据运行结束===================================");
        return "---getYuNanMaoShao is ok---";
    }
}
