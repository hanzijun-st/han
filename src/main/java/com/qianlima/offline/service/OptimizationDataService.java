package com.qianlima.offline.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 建工优选算法--线下实现
 */
@Service
@Slf4j
public class OptimizationDataService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;



    /**
     * 字段去重 - 方法入口
     * 注意： 项目去重只能顺序执行
     * @throws Exception
     */
    public void handleKillRepeat() throws Exception{
        bdJdbcTemplate.update(DELETE_SQL);
        bdJdbcTemplate.update(UPDATE_SQL, null, null, null, null, null, null,null);
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
        if (maps != null && maps.size() > 0){
            for (Map<String, Object> map : maps) {
                handleForDate(map);
            }
        }
    }

    private String UPDATE_SQL = "update han_ali_offline_data set code =?,oneCode = ?, twoCode = ?, threeCode = ?, fourCode = ?, fiveCode = ?, sixCode = ? ";

    private String DELETE_SQL = "delete FROM han_ali_offline_data where code2 is not null";

    private String SELECT_SQL = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType from han_ali_offline_data where code is null order by infoId asc";

    private String SELECT_FOR_ONE = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType,code from han_ali_offline_data where oneCode = ? AND infoPublishForData >= ? order by infoId asc";

    private String SELECT_FOR_TWO = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType,code from han_ali_offline_data where twoCode = ? AND infoPublishForData >= ? order by infoId asc";

    private String SELECT_FOR_THREE = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType,code from han_ali_offline_data where threeCode = ? AND infoPublishForData >= ? order by infoId asc";

    private String SELECT_FOR_FOUR = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType,code from han_ali_offline_data where fourCode = ? AND infoPublishForData >= ? order by infoId asc";

    private String SELECT_FOR_FIVE = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType,code from han_ali_offline_data where fiveCode = ? AND infoPublishForData >= ? order by infoId asc";

    private String SELECT_FOR_SIX = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType,code from han_ali_offline_data where sixCode = ? AND infoPublishForData >= ? order by infoId asc";

    // 标段词信息
    private String[] blacks = { "0次","1次","2次","3次","4次","5次","6次","7次","8次","9次","一次","二次","三次","四次","五次","六次","七次","八次","九次","十次","0回","1回","2回","3回","4回","5回","6回","7回","8回","9回","一回","二回","三回","四回","五回","六回","七回","八回","九回","十回","0标","1标","2标","3标","4标","5标","6标","7标","8标","9标","一标","二标","三标","四标","五标","六标","七标","八标","九标","十标","0包","1包","2包","3包","4包","5包","6包","7包","8包","9包","一包","二包","三包","四包","五包","六包","七包","八包","九包","十包","a包","b包","c包","d包","e包","f包","g包","h包","i包","j包","k包","l包","m包","n包","o包","p包","q包","r包","s包","t包","o包","v包","w包","x包","y包","z包","A包","B包","C包","D包","E包","F包","G包","H包","I包","J包","K包","L包","M包","N包","O包","P包","Q包","R包","S包","T包","O包","V包","W包","X包","Y包","Z包","0期","1期","2期","3期","4期","5期","6期","7期","8期","9期","一期","二期","三期","四期","五期","六期","七期","八期","九期","十期","包0","包1","包2","包3","包4","包5","包6","包7","包8","包9","包一","包二","包三","包四","包五","包六","包七","包八","包九","包十","包a","包b","包c","包d","包e","包f","包g","包h","包i","包j","包k","包l","包m","包n","包o","包p","包q","包r","包s","包t","包o","包v","包w","包x","包y","包z","包A","包B","包C","包D","包E","包F","包G","包H","包I","包J","包K","包L","包M","包N","包O","包P","包Q","包R","包S","包T","包O","包V","包W","包X","包Y","包Z","段0","段1","段2","段3","段4","段5","段6","段7","段8","段9","段一","段二","段三","段四","段五","段六","段七","段八","段九","段十","段a","段b","段c","段d","段e","段f","段g","段h","段i","段j","段k","段l","段m","段n","段o","段p","段q","段r","段s","段t","段o","段v","段w","段x","段y","段z","段A","段B","段C","段D","段E","段F","段G","段H","段I","段J","段K","段L","段M","段N","段O","段P","段Q","段R","段S","段T","段O","段V","段W","段X","段Y","段Z","批次1","批次2","批次3","批次4","批次5","批次6","批次7","批次8","批次9","批次一","批次二","批次三","批次四","批次五","批次六","批次七","批次八","批次九","批次十","1批次","2批次","3批次","4批次","5批次","6批次","7批次","8批次","9批次","一批次","二批次","三批次","四批次","五批次","六批次","七批次","八批次","九批次","十批次","标的1","标的2","标的3","标的4","标的5","标的6","标的7","标的8","标的9","标的0","标的一","标的二","标的三","标的四","标的五","标的六","标的七","标的八","标的九","标的十","标项1","标项2","标项3","标项4","标项5","标项6","标项7","标项8","标项9","标项0","标项一","标项二","标项三","标项四","标项五","标项六","标项七","标项八","标项九","标项十","品目一","品目二","品目三","品目四","品目五","品目六","品目七","品目八","品目九","品目十" };

    AtomicInteger atomicInteger = new AtomicInteger(0);

    private static final String DATE_SDF = "yyyy-MM-dd hh:mm:ss";


    // 发布时间往前推60天，670天之前的数据，不参与去重处理
    private static Long getDayAgo(String updateTime) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date parseDate = DateUtils.parseDate(updateTime, DATE_SDF);
            calendar.setTime(parseDate);
            calendar.add(Calendar.DATE, -60);
        } catch (Exception e) {
            log.info("日期格式不正确, 日期转换异常");
        }
        return calendar.getTime().getTime();
    }

    private static boolean simAndDid(String oneElement, String twoElement){
        if (StringUtils.isNotBlank(oneElement) && StringUtils.isNotBlank(twoElement)){
            if (oneElement.equals(twoElement)){
                return true;
            } else {
                return false;
            }
        }
        return true;
    }



    private synchronized void handleForDate(Map<String, Object> map) throws Exception{

        String infoId = map.get("infoId") != null ? map.get("infoId").toString() : ""; //
        String infoTitle = map.get("infoTitle") != null ? map.get("infoTitle").toString() : "";
        String infoPublishTime = map.get("infoPublishTime") != null ? map.get("infoPublishTime").toString() : "";
        String areaProvince = map.get("areaProvince") != null ? map.get("areaProvince").toString() : "";
        String areaCity = map.get("areaCity") != null ? map.get("areaCity").toString() : "";
        String areaCountry = map.get("areaCountry") != null ? map.get("areaCountry").toString() : "";
        String xmNumber = map.get("xmNumber") != null ? map.get("xmNumber").toString() : "";
        String zhongBiaoUnit = map.get("zhongBiaoUnit") != null ? map.get("zhongBiaoUnit").toString() : "";
        String zhaoBiaoUnit = map.get("zhaoBiaoUnit") != null ? map.get("zhaoBiaoUnit").toString() : "";
        String budget = map.get("budget") != null ? map.get("budget").toString() : "";
        String winnerAmount = map.get("winnerAmount") != null ? map.get("winnerAmount").toString() : "";
        String extractProjName = map.get("extractProjName") != null ? map.get("extractProjName").toString() : "";
        String biddingType = map.get("biddingType") != null ? map.get("biddingType").toString() : "";
        String infoTypeSegment = map.get("infoTypeSegment") != null ? map.get("infoTypeSegment").toString() : "";

        // 10,11,12,13,15,9

        // 当前数据，往前推 60天  60天之前的数据 不进行去重处理  但是要生成编号
        Long dayAgo = getDayAgo(infoPublishTime);
        long infoPublishForData = DateUtils.parseDate(infoPublishTime, DATE_SDF).getTime();

        String oneCode = "";
        boolean flagCodeOne = true;
        // 生成编号 1.0  '2.2、B & G完全匹配，C & D & E & F & H & I & J异同  AND （标题含有<标段>不参与去重 OR 招标方式为"4"<询价>不参与去重 OR 标题含有“政府采购意向”不参与去重）
        // 标题含有<标段>不参与去重
        for (String black : blacks) {
            if (infoTitle.toUpperCase().contains(black.toUpperCase())){
                flagCodeOne = false;
                break;
            }
        }
        //  招标方式为"4"<询价>不参与 OR 标题“政府采购意向”不参与
        if ("4".equals(biddingType) || infoTitle.contains("政府采购意向")){
            flagCodeOne = false;
        }
        if (flagCodeOne && StringUtils.isNotBlank(xmNumber)){
            oneCode = String.valueOf((xmNumber).hashCode());
        }

        String twoCode = "";
        boolean flagCodeTwo = true;
        // 标题“政府采购意向”不参与
        if (infoTitle.contains("政府采购意向")){
            flagCodeTwo = false;
        }
        if (flagCodeTwo && StringUtils.isNotBlank(infoTitle)){
            twoCode = String.valueOf((infoTitle).hashCode());
        }

        String threeCode = "";
        // D & F & G完全匹配，B & C & E & H & I & J异同
        if (StringUtils.isNotBlank(winnerAmount) && StringUtils.isNotBlank(zhongBiaoUnit)){
            threeCode = String.valueOf((winnerAmount + zhongBiaoUnit).hashCode());
        }

        String fourCode = "";
        // C & E & G完全匹配，B & D & F & H & I & J异同
        if (StringUtils.isNotBlank(budget) && StringUtils.isNotBlank(zhaoBiaoUnit)){
            fourCode = String.valueOf((budget + zhaoBiaoUnit).hashCode());
        }

        String fiveCode = "";
        // D & E & G完全匹配，B & C & F & H & I & J异同
        if (StringUtils.isNotBlank(winnerAmount) && StringUtils.isNotBlank(zhaoBiaoUnit)){
            fiveCode = String.valueOf((winnerAmount + zhaoBiaoUnit).hashCode());
        }

        String sixCode = "";
        // C & F & G完全匹配，B & D & E & H & I & J异同
        if (StringUtils.isNotBlank(budget) && StringUtils.isNotBlank(zhongBiaoUnit)){
            sixCode = String.valueOf((budget + zhongBiaoUnit).hashCode());
        }

        boolean flag = true;
        log.info("处理到了infoId：{}, 当前是第num：{} 条", infoId, atomicInteger.addAndGet(1));
        bdJdbcTemplate.update("update han_ali_offline_data set code = ?,infoPublishForData = ? where infoId = ?", infoId, infoPublishForData, infoId);
        String dnpId = "";

        // B & G完全匹配，C & D & E & F & H & I & J异同
        if (flag && StringUtils.isNotBlank(oneCode)){
            // 更新完成后，进行规则查询
            List<Map<String, Object>> results = bdJdbcTemplate.queryForList(SELECT_FOR_ONE, oneCode, dayAgo);
            if (results != null && results.size() > 0){
                Map<String, List<Map<String, Object>>> handleMap = handle(results);
                if (handleMap != null && ! handleMap.isEmpty()){
                    Set<Map.Entry<String, List<Map<String, Object>>>> entries = handleMap.entrySet();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
                        dnpId = entry.getKey();
                        List<Map<String, Object>> value = entry.getValue();
                        if (value != null && value.size() > 0){
                            for (Map<String, Object> result : value) {
                                String newAreaProvince = result.get("areaProvince") != null ? result.get("areaProvince").toString() : "";
                                String newAreaCity = result.get("areaCity") != null ? result.get("areaCity").toString() : "";
                                String newAreaCountry = result.get("areaCountry") != null ? result.get("areaCountry").toString() : "";
                                String newZhongBiaoUnit = result.get("zhongBiaoUnit") != null ? result.get("zhongBiaoUnit").toString() : "";
                                String newZhaoBiaoUnit = result.get("zhaoBiaoUnit") != null ? result.get("zhaoBiaoUnit").toString() : "";
                                String newBudget = result.get("budget") != null ? result.get("budget").toString() : "";
                                String newWinnerAmount = result.get("winnerAmount") != null ? result.get("winnerAmount").toString() : "";
                                if (! (simAndDid(budget, newBudget) && simAndDid(winnerAmount, newWinnerAmount) && simAndDid(zhaoBiaoUnit, newZhaoBiaoUnit) && simAndDid(zhongBiaoUnit, newZhongBiaoUnit)
                                        && simAndDid(areaProvince, newAreaProvince) && simAndDid(areaCity, newAreaCity) && simAndDid(areaCountry, newAreaCountry))){
                                    log.info("满足B & D & E & H & I & J异同");
                                    dnpId = null;
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(dnpId)){
                            bdJdbcTemplate.update("update han_ali_offline_data set code2 = ?, code = ? where infoId = ?", "2.1", dnpId, infoId);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }


        // A & G完全匹配，B & C & D & E & F & H & I & J & K异同
        if (flag && StringUtils.isNotBlank(twoCode)){
            // 更新完成后，进行规则查询
            List<Map<String, Object>> results = bdJdbcTemplate.queryForList(SELECT_FOR_TWO, twoCode, dayAgo);
            if (results != null && results.size() > 0){
                Map<String, List<Map<String, Object>>> handleMap = handle(results);
                if (handleMap != null && ! handleMap.isEmpty()){
                    Set<Map.Entry<String, List<Map<String, Object>>>> entries = handleMap.entrySet();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
                        dnpId = entry.getKey();
                        List<Map<String, Object>> value = entry.getValue();
                        if (value != null && value.size() > 0){
                            for (Map<String, Object> result : value) {
                                String newXmNumber = result.get("xmNumber") != null ? result.get("xmNumber").toString() : "";
                                String newExtractProjName = map.get("extractProjName") != null ? map.get("extractProjName").toString() : "";
                                String newAreaProvince = result.get("areaProvince") != null ? result.get("areaProvince").toString() : "";
                                String newAreaCity = result.get("areaCity") != null ? result.get("areaCity").toString() : "";
                                String newAreaCountry = result.get("areaCountry") != null ? result.get("areaCountry").toString() : "";
                                String newZhongBiaoUnit = result.get("zhongBiaoUnit") != null ? result.get("zhongBiaoUnit").toString() : "";
                                String newZhaoBiaoUnit = result.get("zhaoBiaoUnit") != null ? result.get("zhaoBiaoUnit").toString() : "";
                                String newBudget = result.get("budget") != null ? result.get("budget").toString() : "";
                                String newWinnerAmount = result.get("winnerAmount") != null ? result.get("winnerAmount").toString() : "";
                                if (! (simAndDid(newXmNumber, xmNumber) && simAndDid(winnerAmount, newWinnerAmount) && simAndDid(zhongBiaoUnit, newZhongBiaoUnit) && simAndDid(budget, newBudget)
                                        && simAndDid(zhaoBiaoUnit, newZhaoBiaoUnit) && simAndDid(areaProvince, newAreaProvince) && simAndDid(areaCity, newAreaCity) && simAndDid(areaCountry, newAreaCountry)
                                        && simAndDid(extractProjName, newExtractProjName))){
                                    log.info("满足B & D & E & H & I & J异同");
                                    dnpId = null;
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(dnpId)){
                            bdJdbcTemplate.update("update han_ali_offline_data set code2 = ?, code = ? where infoId = ?", "2.2", dnpId, infoId);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }


        // D & F & G完全匹配，B & C & E & H & I & J异同
        if (flag && StringUtils.isNotBlank(threeCode)){
            // 更新完成后，进行规则查询
            List<Map<String, Object>> results = bdJdbcTemplate.queryForList(SELECT_FOR_THREE, threeCode, dayAgo);
            if (results != null && results.size() > 0){
                Map<String, List<Map<String, Object>>> handleMap = handle(results);
                if (handleMap != null && ! handleMap.isEmpty()){
                    Set<Map.Entry<String, List<Map<String, Object>>>> entries = handleMap.entrySet();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
                        dnpId = entry.getKey();
                        List<Map<String, Object>> value = entry.getValue();
                        if (value != null && value.size() > 0){
                            for (Map<String, Object> result : value) {
                                String newXmNumber = result.get("xmNumber") != null ? result.get("xmNumber").toString() : "";
                                String newAreaProvince = result.get("areaProvince") != null ? result.get("areaProvince").toString() : "";
                                String newAreaCity = result.get("areaCity") != null ? result.get("areaCity").toString() : "";
                                String newAreaCountry = result.get("areaCountry") != null ? result.get("areaCountry").toString() : "";
                                String newZhaoBiaoUnit = result.get("zhaoBiaoUnit") != null ? result.get("zhaoBiaoUnit").toString() : "";
                                String newBudget = result.get("budget") != null ? result.get("budget").toString() : "";
                                if (! (simAndDid(newXmNumber, xmNumber) && simAndDid(budget, newBudget) && simAndDid(zhaoBiaoUnit, newZhaoBiaoUnit) &&
                                        simAndDid(areaProvince, newAreaProvince) && simAndDid(areaCity, newAreaCity) && simAndDid(areaCountry, newAreaCountry))){
                                    log.info("满足B & D & E & H & I & J异同");
                                    dnpId = null;
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(dnpId)){
                            bdJdbcTemplate.update("update han_ali_offline_data set code2 = ?, code = ? where infoId = ?", "2.3", dnpId, infoId);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }


        // 2。4、C & E & G完全匹配，B & D & F & H & I & J异同
        if (flag && StringUtils.isNotBlank(fourCode)){
            // 更新完成后，进行规则查询
            List<Map<String, Object>> results = bdJdbcTemplate.queryForList(SELECT_FOR_FOUR, fourCode, dayAgo);
            if (results != null && results.size() > 0){
                Map<String, List<Map<String, Object>>> handleMap = handle(results);
                if (handleMap != null && ! handleMap.isEmpty()){
                    Set<Map.Entry<String, List<Map<String, Object>>>> entries = handleMap.entrySet();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
                        dnpId = entry.getKey();
                        List<Map<String, Object>> value = entry.getValue();
                        if (value != null && value.size() > 0){
                            for (Map<String, Object> result : value) {
                                String newXmNumber = result.get("xmNumber") != null ? result.get("xmNumber").toString() : "";
                                String newAreaProvince = result.get("areaProvince") != null ? result.get("areaProvince").toString() : "";
                                String newAreaCity = result.get("areaCity") != null ? result.get("areaCity").toString() : "";
                                String newAreaCountry = result.get("areaCountry") != null ? result.get("areaCountry").toString() : "";
                                String newZhongBiaoUnit = result.get("zhongBiaoUnit") != null ? result.get("zhongBiaoUnit").toString() : "";
                                String newWinnerAmount = result.get("winnerAmount") != null ? result.get("winnerAmount").toString() : "";
                                if (! (simAndDid(newXmNumber, xmNumber) && simAndDid(winnerAmount, newWinnerAmount) && simAndDid(zhaoBiaoUnit, newZhongBiaoUnit) && simAndDid(areaProvince, newAreaProvince)
                                        && simAndDid(areaCity, newAreaCity) && simAndDid(areaCountry, newAreaCountry))){
                                    log.info("满足B & D & E & H & I & J异同");
                                    dnpId = null;
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(dnpId)){
                            bdJdbcTemplate.update("update han_ali_offline_data set code2 = ?, code = ? where infoId = ?", "2.4", dnpId, infoId);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }



        // 2。4、C & E & G完全匹配，B & D & F & H & I & J异同
        if (flag && StringUtils.isNotBlank(fiveCode)){
            // 更新完成后，进行规则查询
            List<Map<String, Object>> results = bdJdbcTemplate.queryForList(SELECT_FOR_FIVE, fiveCode, dayAgo);
            if (results != null && results.size() > 0){
                Map<String, List<Map<String, Object>>> handleMap = handle(results);
                if (handleMap != null && ! handleMap.isEmpty()){
                    Set<Map.Entry<String, List<Map<String, Object>>>> entries = handleMap.entrySet();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
                        dnpId = entry.getKey();
                        List<Map<String, Object>> value = entry.getValue();
                        if (value != null && value.size() > 0){
                            for (Map<String, Object> result : value) {
                                String newXmNumber = result.get("xmNumber") != null ? result.get("xmNumber").toString() : "";
                                String newAreaProvince = result.get("areaProvince") != null ? result.get("areaProvince").toString() : "";
                                String newAreaCity = result.get("areaCity") != null ? result.get("areaCity").toString() : "";
                                String newAreaCountry = result.get("areaCountry") != null ? result.get("areaCountry").toString() : "";
                                String newZhongBiaoUnit = result.get("zhongBiaoUnit") != null ? result.get("zhongBiaoUnit").toString() : "";
                                String newBudget = result.get("budget") != null ? result.get("budget").toString() : "";

                                if (! (simAndDid(newXmNumber, xmNumber) && simAndDid(newBudget, budget) && simAndDid(zhongBiaoUnit, newZhongBiaoUnit) && simAndDid(areaProvince, newAreaProvince)
                                        && simAndDid(areaCity, newAreaCity) && simAndDid(areaCountry, newAreaCountry))){
                                    log.info("满足B & D & E & H & I & J异同");
                                    dnpId = null;
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(dnpId)){
                            bdJdbcTemplate.update("update han_ali_offline_data set code2 = ?, code = ? where infoId = ?", "2.5", dnpId, infoId);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }


        // 2。4、C & E & G完全匹配，B & D & F & H & I & J异同
        if (flag && StringUtils.isNotBlank(sixCode)){
            // 更新完成后，进行规则查询
            List<Map<String, Object>> results = bdJdbcTemplate.queryForList(SELECT_FOR_SIX, sixCode, dayAgo);
            if (results != null && results.size() > 0){
                Map<String, List<Map<String, Object>>> handleMap = handle(results);
                if (handleMap != null && ! handleMap.isEmpty()){
                    Set<Map.Entry<String, List<Map<String, Object>>>> entries = handleMap.entrySet();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
                        dnpId = entry.getKey();
                        List<Map<String, Object>> value = entry.getValue();
                        if (value != null && value.size() > 0){
                            for (Map<String, Object> result : value) {
                                String newXmNumber = result.get("xmNumber") != null ? result.get("xmNumber").toString() : "";
                                String newAreaProvince = result.get("areaProvince") != null ? result.get("areaProvince").toString() : "";
                                String newAreaCity = result.get("areaCity") != null ? result.get("areaCity").toString() : "";
                                String newAreaCountry = result.get("areaCountry") != null ? result.get("areaCountry").toString() : "";
                                String newZhaoBiaoUnit = result.get("zhaoBiaoUnit") != null ? result.get("zhaoBiaoUnit").toString() : "";
                                String newWinnerAmount = result.get("winnerAmount") != null ? result.get("winnerAmount").toString() : "";

                                if (! (simAndDid(newXmNumber, xmNumber) && simAndDid(winnerAmount, newWinnerAmount) && simAndDid(zhaoBiaoUnit, newZhaoBiaoUnit) && simAndDid(areaProvince, newAreaProvince)
                                        && simAndDid(areaCity, newAreaCity) && simAndDid(areaCountry, newAreaCountry))){
                                    log.info("满足B & D & E & H & I & J异同");
                                    dnpId = null;
                                    break;
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(dnpId)){
                            bdJdbcTemplate.update("update han_ali_offline_data set code2 = ?, code = ? where infoId = ?", "2.6", dnpId, infoId);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }



        bdJdbcTemplate.update("update han_ali_offline_data set oneCode = ?, twoCode = ?, threeCode = ?, fourCode = ?, fiveCode = ?, sixCode = ? where infoId = ?", oneCode, twoCode, threeCode, fourCode, fiveCode, sixCode, infoId);
    }







    /**
     * 将一个code码查出来的标文，按照项目id进行分组     *
     * @param
     * @return
     */
    private Map<String, List<Map<String, Object>>> handle(List<Map<String, Object>> results) {
        Map<String, List<Map<String, Object>>> map=new HashMap<>();
        Set<String> set = new HashSet<>();
        for (Map<String, Object> result : results) {
            String code = result.get("code") != null ? result.get("code").toString() : "";
            set.add(code);
        }
        for (String str : set) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> result : results) {
                String code = result.get("code") != null ? result.get("code").toString() : "";
                if (str.equals(code)){
                    list.add(result);
                }
            }
            map.put(str,list);
        }
        return map;
    }


}
