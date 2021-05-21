package com.qianlima.offline.service.aquchong;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RemoveDupServiceForThree {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    private String SELECT_SQL = "SELECT content_id, title,xmNumber,baiLian_budget,baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit," +
            "progid, province, city, country, keyword_term, bidding_type, updateTime, update_time,infoTypeSegment FROM han_qc_result_poc_table order by content_id asc";

    private String[] blacks = { "0次","1次","2次","3次","4次","5次","6次","7次","8次","9次","一次","二次","三次","四次","五次","六次","七次","八次","九次","十次","0回","1回","2回","3回","4回","5回","6回","7回","8回","9回","一回","二回","三回","四回","五回","六回","七回","八回","九回","十回","0标","1标","2标","3标","4标","5标","6标","7标","8标","9标","一标","二标","三标","四标","五标","六标","七标","八标","九标","十标","0包","1包","2包","3包","4包","5包","6包","7包","8包","9包","一包","二包","三包","四包","五包","六包","七包","八包","九包","十包","a包","b包","c包","d包","e包","f包","g包","h包","i包","j包","k包","l包","m包","n包","o包","p包","q包","r包","s包","t包","o包","v包","w包","x包","y包","z包","A包","B包","C包","D包","E包","F包","G包","H包","I包","J包","K包","L包","M包","N包","O包","P包","Q包","R包","S包","T包","O包","V包","W包","X包","Y包","Z包","0期","1期","2期","3期","4期","5期","6期","7期","8期","9期","一期","二期","三期","四期","五期","六期","七期","八期","九期","十期","包0","包1","包2","包3","包4","包5","包6","包7","包8","包9","包一","包二","包三","包四","包五","包六","包七","包八","包九","包十","包a","包b","包c","包d","包e","包f","包g","包h","包i","包j","包k","包l","包m","包n","包o","包p","包q","包r","包s","包t","包o","包v","包w","包x","包y","包z","包A","包B","包C","包D","包E","包F","包G","包H","包I","包J","包K","包L","包M","包N","包O","包P","包Q","包R","包S","包T","包O","包V","包W","包X","包Y","包Z","段0","段1","段2","段3","段4","段5","段6","段7","段8","段9","段一","段二","段三","段四","段五","段六","段七","段八","段九","段十","段a","段b","段c","段d","段e","段f","段g","段h","段i","段j","段k","段l","段m","段n","段o","段p","段q","段r","段s","段t","段o","段v","段w","段x","段y","段z","段A","段B","段C","段D","段E","段F","段G","段H","段I","段J","段K","段L","段M","段N","段O","段P","段Q","段R","段S","段T","段O","段V","段W","段X","段Y","段Z","批次1","批次2","批次3","批次4","批次5","批次6","批次7","批次8","批次9","批次一","批次二","批次三","批次四","批次五","批次六","批次七","批次八","批次九","批次十","1批次","2批次","3批次","4批次","5批次","6批次","7批次","8批次","9批次","一批次","二批次","三批次","四批次","五批次","六批次","七批次","八批次","九批次","十批次","标的1","标的2","标的3","标的4","标的5","标的6","标的7","标的8","标的9","标的0","标的一","标的二","标的三","标的四","标的五","标的六","标的七","标的八","标的九","标的十","标项1","标项2","标项3","标项4","标项5","标项6","标项7","标项8","标项9","标项0","标项一","标项二","标项三","标项四","标项五","标项六","标项七","标项八","标项九","标项十","品目一","品目二","品目三","品目四","品目五","品目六","品目七","品目八","品目九","品目十" };

    AtomicInteger atomicInteger = new AtomicInteger(0);

    private static final String DATE_SDF = "yyyy-MM-dd hh:mm:ss";


    public Long getThreeDayAgo(String updateTime) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date parseDate = DateUtils.parseDate(updateTime, DATE_SDF);
            calendar.setTime(parseDate);
            calendar.add(Calendar.DATE, -3);
        } catch (Exception e) {
            log.info("日期格式不正确, 日期转换异常");
        }
        return calendar.getTime().getTime();
    }

    public void handle(){
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
        if (maps != null && maps.size() > 0){
            for (Map<String, Object> map : maps) {

                String contentId = map.get("content_id") != null ? map.get("content_id").toString() : ""; //
                String title = map.get("title") != null ? map.get("title").toString() : ""; //A
                String xmNumber = map.get("xmNumber") != null ? map.get("xmNumber").toString() : ""; //B
                String budget = map.get("baiLian_budget") != null ? map.get("baiLian_budget").toString() : ""; //C
                String amount = map.get("baiLian_amount_unit") != null ? map.get("baiLian_amount_unit").toString() : ""; //D
                String zhaoUnit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : ""; //E
                String zhongUnit = map.get("zhong_biao_unit") != null ? map.get("zhong_biao_unit").toString() : ""; //F
                String progid = map.get("progid") != null ? map.get("progid").toString() : ""; //G
                String province = map.get("province") != null ? map.get("province").toString() : ""; //G
                String city = map.get("city") != null ? map.get("city").toString() : ""; //G
                String country = map.get("country") != null ? map.get("country").toString() : ""; //G
                String keywordTerm = map.get("keyword_term") != null ? map.get("keyword_term").toString() : ""; // k
                String biddingType = map.get("bidding_type") != null ? map.get("bidding_type").toString() : ""; // k
                String update_time = map.get("update_time") != null ? map.get("update_time").toString() : ""; // k
                String infoTypeSegment = map.get("infoTypeSegment") != null ? map.get("infoTypeSegment").toString() : ""; //G

                // 优先判断细分信息类型，如果没有，再判断信息类型（0、1、2、3、5）
                if (StringUtils.isNotBlank(infoTypeSegment)){
                    progid = infoTypeSegment;
                }


                // 获取三天前的时间
                Long threeDayAgo = getThreeDayAgo(update_time);

                String allTwo = "";
                boolean flagCodeForTwo = true;
                // 标题含有<标段>不参与去重
                for (String black : blacks) {
                    if (title.toUpperCase().contains(black.toUpperCase())){
                        flagCodeForTwo = false;
                        break;
                    }
                }



                //  招标方式为"4"<询价>不参与去重
                if ("4".equals(biddingType)){
                    flagCodeForTwo = false;
                }
                if (title.contains("政府采购意向")){
                    flagCodeForTwo = false;
                }
                // 标题含有“标段” 不生成 code 码  同时 B & G完全匹配，C & D & F相同
                if (flagCodeForTwo && StringUtils.isNotBlank(xmNumber) && StringUtils.isNotBlank(progid)){
                    allTwo = xmNumber + progid;
                }

                // A & G完全匹配，B & C & D & E & F & H & I & J & K异同  AND  标题含有“政府采购意向”不参与去重
                String allThree = "";
                boolean flagCodeForThree = true;
                if (title.contains("政府采购意向")){
                    flagCodeForThree = false;
                }



                if (flagCodeForThree && StringUtils.isNotBlank(title) && StringUtils.isNotBlank(progid)){
                    allThree = title + progid;
                }

                // D & F & G完全匹配，C & E & H & I & J异同
                String allFour = "";
                if (StringUtils.isNotBlank(amount) && StringUtils.isNotBlank(zhongUnit) && StringUtils.isNotBlank(progid)){
                    allFour = amount + zhongUnit + progid;
                }

                // C & E & G完全匹配，D & F & H & I & J异同
                String allFive = "";
                if (StringUtils.isNotBlank(budget) && StringUtils.isNotBlank(zhaoUnit) && StringUtils.isNotBlank(progid)){
                    allFive = budget + zhaoUnit + progid;
                }
                // 2.6、D & E & G完全匹配，B & C & F & H & I & J异同
                String allSix = "";
                if (StringUtils.isNotBlank(amount) && StringUtils.isNotBlank(zhaoUnit) && StringUtils.isNotBlank(progid)){
                    allSix = amount + zhaoUnit + progid;
                }
                // 2.7、C & F & G完全匹配，B & D & E & H & I & J异同
                String allSeven = "";
                if (StringUtils.isNotBlank(budget) && StringUtils.isNotBlank(zhongUnit) && StringUtils.isNotBlank(progid)){
                    allSeven = budget + zhongUnit + progid;
                }

                // 获取code码
                String allForTwoCode = String.valueOf(allTwo.toUpperCase().hashCode());
                String allForThreeCode = String.valueOf(allThree.toUpperCase().hashCode());
                String allForFourCode = String.valueOf(allFour.toUpperCase().hashCode());
                String allForFiveCode = String.valueOf(allFive.toUpperCase().hashCode());
                String allForSixCode = String.valueOf(allSix.toUpperCase().hashCode());
                String allForSevenCode = String.valueOf(allSeven.toUpperCase().hashCode());

                boolean flag = true;
                // 更新对应的编码
                log.info("处理到了infoId：{}, 当前是第num：{} 条", contentId, atomicInteger.addAndGet(1));
                bdJdbcTemplate.update("update han_qc_result_poc_table set code = ? where content_id = ?", contentId, contentId);

                String dnpId = "";
                /**
                 * 第二层 B & G完全匹配，C & D & E & F & H & I & J异同
                 */
                if (flag && StringUtils.isNotBlank(allTwo)){
                    // 更新完成后，进行规则查询 (因为自身已经更新进入，所以查询数据条数需要大于1条)
                    List<Map<String, Object>> allTwoForMaps = bdJdbcTemplate.queryForList("SELECT content_id, code, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit,progid,province,city, country from han_qc_result_poc_table where allForTwoCode = ? AND updateTime >= ? order by content_id asc", allForTwoCode, threeDayAgo);
                    if (allTwoForMaps != null && allTwoForMaps.size() > 0){
                        int num = 0;
                        for (Map<String, Object> objectMap : allTwoForMaps) {
                            String newbudget = objectMap.get("baiLian_budget") != null ? objectMap.get("baiLian_budget").toString() : ""; //C
                            String newamount = objectMap.get("baiLian_amount_unit") != null ? objectMap.get("baiLian_amount_unit").toString() : ""; //D
                            String newzhaoUnit = objectMap.get("zhao_biao_unit") != null ? objectMap.get("zhao_biao_unit").toString() : ""; //E
                            String newzhongUnit = objectMap.get("zhong_biao_unit") != null ? objectMap.get("zhong_biao_unit").toString() : ""; //F
                            String newprovince = objectMap.get("province") != null ? objectMap.get("province").toString() : ""; //G
                            String newcity = objectMap.get("city") != null ? objectMap.get("city").toString() : ""; //G
                            String newcountry = objectMap.get("country") != null ? objectMap.get("country").toString() : ""; //G

                            // 判断异同  字段C（招标预算） 字段D（中标金额） 字段E（招标单位） 字段F（中标单位） 字段H（省） 字段I（市） 字段J（县）
                            if (simAndDid(budget, newbudget) && simAndDid(amount, newamount) && simAndDid(zhaoUnit, newzhaoUnit) &&
                                    simAndDid(zhongUnit, newzhongUnit) && simAndDid(province, newprovince) && simAndDid(city, newcity) && simAndDid(country, newcountry)){
                                log.info("满足C & D & E & F & H & I & J异同");
                                num ++;
                            }
                        }
                        if (num == allTwoForMaps.size()){
                            dnpId = allTwoForMaps.get(0).get("code") != null ? allTwoForMaps.get(0).get("code").toString() : ""; //A
                            bdJdbcTemplate.update("update han_qc_result_poc_table set code2 = ?, code = ? where content_id = ?", "2.2", dnpId, contentId);
                            // 刷新标志位操作
                            flag = false;
                        }
                    }
                }

                /**
                 * 第三层  A & G完全匹配，B & C & D & E & F & H & I & J & K异同   AND  标题含有“政府采购意向”不参与去重
                 */
                if (flag && StringUtils.isNotBlank(allThree)){
                    List<Map<String, Object>> allThreeForMaps = bdJdbcTemplate.queryForList("SELECT content_id, code, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit,progid,province,city, country, keyword_term from han_qc_result_poc_table where allForThreeCode = ? AND updateTime >= ? order by content_id asc", allForThreeCode, threeDayAgo);
                    if (allThreeForMaps != null && allThreeForMaps.size() > 0){
                        int num = 0;
                        for (Map<String, Object> objectMap : allThreeForMaps) {
                            String newContentId = objectMap.get("content_id") != null ? objectMap.get("content_id").toString() : ""; //B
                            String newXmNumber = objectMap.get("xmNumber") != null ? objectMap.get("xmNumber").toString() : ""; //B
                            String newamount = objectMap.get("baiLian_amount_unit") != null ? objectMap.get("baiLian_amount_unit").toString() : ""; //D
                            String newzhongUnit = objectMap.get("zhong_biao_unit") != null ? objectMap.get("zhong_biao_unit").toString() : ""; //F
                            String newbudget = objectMap.get("baiLian_budget") != null ? objectMap.get("baiLian_budget").toString() : ""; //C
                            String newzhaoUnit = objectMap.get("zhao_biao_unit") != null ? objectMap.get("zhao_biao_unit").toString() : ""; //E
                            String newprovince = objectMap.get("province") != null ? objectMap.get("province").toString() : ""; //H
                            String newcity = objectMap.get("city") != null ? objectMap.get("city").toString() : ""; //G
                            String newcountry = objectMap.get("country") != null ? objectMap.get("country").toString() : ""; //G
                            String newKeywordTerm = objectMap.get("keyword_term") != null ? objectMap.get("keyword_term").toString() : ""; // k
                            // 由于异同是指完全匹配的对立面, 字段C（招标预算） 字段D（中标金额） 字段E（招标单位） 字段F（中标单位） 字段H（省） 字段I（市） 字段J（县）
                            if (simAndDid(newXmNumber, xmNumber) && simAndDid(newamount, amount) && simAndDid(newzhongUnit, zhongUnit) && simAndDid(budget, newbudget) && simAndDid(budget, newbudget) && simAndDid(zhaoUnit, newzhaoUnit) && simAndDid(province, newprovince)
                                    && simAndDid(city, newcity) && simAndDid(country, newcountry) && simAndDid(newKeywordTerm, keywordTerm)){
                                log.info("满足B & C & D & E & F & H & I & J & K异同");
                                log.info("原始：{} 后面的：{}", contentId, newContentId);
                                num ++;
                            }
                        }
                        if (num == allThreeForMaps.size()){
                            dnpId = allThreeForMaps.get(0).get("code") != null ? allThreeForMaps.get(0).get("code").toString() : ""; //A
                            bdJdbcTemplate.update("update han_qc_result_poc_table set code2 = ?, code = ? where content_id = ?", "2.3", dnpId, contentId);
                            flag = false;
                        }
                    }
                }

                /**
                 * 第四层  D & F & G完全匹配，B & C & E & H & I & J异同
                 */
                if (flag && StringUtils.isNotBlank(allFour)){
                    List<Map<String, Object>> allFourForMaps = bdJdbcTemplate.queryForList("SELECT content_id, code, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit,progid,province,city, country from han_qc_result_poc_table where allForFourCode = ? AND updateTime >= ? order by content_id asc", allForFourCode, threeDayAgo);
                    if (allFourForMaps != null && allFourForMaps.size() > 0){
                        int num = 0;
                        for (Map<String, Object> objectMap : allFourForMaps) {
                            String newXmNumber = objectMap.get("xmNumber") != null ? objectMap.get("xmNumber").toString() : ""; //B
                            String newbudget = objectMap.get("baiLian_budget") != null ? objectMap.get("baiLian_budget").toString() : ""; //C
                            String newzhaoUnit = objectMap.get("zhao_biao_unit") != null ? objectMap.get("zhao_biao_unit").toString() : ""; //E
                            String newprovince = objectMap.get("province") != null ? objectMap.get("province").toString() : ""; //H
                            String newcity = objectMap.get("city") != null ? objectMap.get("city").toString() : ""; //G
                            String newcountry = objectMap.get("country") != null ? objectMap.get("country").toString() : ""; //G
                            // 由于异同是指完全匹配的对立面, 故无法通过code码进行判断, 需要通过代码进行逻辑判断
                            if (simAndDid(newXmNumber, xmNumber) && simAndDid(budget, newbudget) && simAndDid(budget, newbudget) && simAndDid(zhaoUnit, newzhaoUnit) && simAndDid(province, newprovince)
                                    && simAndDid(city, newcity) && simAndDid(country, newcountry)){
                                log.info("满足B & C & E & H & I & J异同");
                                num ++;
                            }
                        }
                        if (num == allFourForMaps.size()){
                            dnpId = allFourForMaps.get(0).get("code") != null ? allFourForMaps.get(0).get("code").toString() : ""; //A
                            bdJdbcTemplate.update("update han_qc_result_poc_table set code2 = ?, code = ? where content_id = ?", "2.4", dnpId, contentId);
                            flag = false;
                        }
                    }
                }

                /**
                 * 第五层  C & E & G完全匹配，B & D & F & H & I & J异同
                 */
                if (flag && StringUtils.isNotBlank(allFive)){
                    // 更新完成后，进行规则查询
                    List<Map<String, Object>> allFiveForMaps = bdJdbcTemplate.queryForList("SELECT content_id, code, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit,progid,province,city, country from han_qc_result_poc_table where allForFiveCode = ? AND updateTime >= ?  order by content_id asc", allForFiveCode, threeDayAgo);
                    if (allFiveForMaps != null && allFiveForMaps.size() > 0){
                        int num = 0;
                        for (Map<String, Object> objectMap : allFiveForMaps) {
                            String newXmNumber = objectMap.get("xmNumber") != null ? objectMap.get("xmNumber").toString() : ""; //B
                            String newamount = objectMap.get("baiLian_amount_unit") != null ? objectMap.get("baiLian_amount_unit").toString() : ""; //D
                            String newzhongUnit = objectMap.get("zhong_biao_unit") != null ? objectMap.get("zhong_biao_unit").toString() : ""; //F
                            String newprovince = objectMap.get("province") != null ? objectMap.get("province").toString() : ""; //G
                            String newcity = objectMap.get("city") != null ? objectMap.get("city").toString() : ""; //G
                            String newcountry = objectMap.get("country") != null ? objectMap.get("country").toString() : ""; //G
                            // 由于异同是指完全匹配的对立面,
                            if (simAndDid(newXmNumber, xmNumber) && simAndDid(amount, newamount) && simAndDid(zhongUnit, newzhongUnit) && simAndDid(province, newprovince)
                                    && simAndDid(city, newcity) && simAndDid(country, newcountry)){
                                log.info("满足C & D & E & F & H & I & J异同");
                                num ++;
                            }
                        }
                        if (num == allFiveForMaps.size()){
                            dnpId = allFiveForMaps.get(0).get("code") != null ? allFiveForMaps.get(0).get("code").toString() : ""; //A
                            bdJdbcTemplate.update("update han_qc_result_poc_table set code2 = ?, code = ? where content_id = ?", "2.5", dnpId, contentId);
                            flag = false;
                        }
                    }
                }

                /**
                 * 第六层  D & E & G完全匹配，B & C & F & H & I & J异同
                 */
                if (flag && StringUtils.isNotBlank(allSix)){
                    // 更新完成后，进行规则查询
                    List<Map<String, Object>> allFiveForMaps = bdJdbcTemplate.queryForList("SELECT content_id, code, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit,progid,province,city, country from han_qc_result_poc_table where allForSixCode = ? AND updateTime >= ?  order by content_id asc", allForSixCode, threeDayAgo);
                    if (allFiveForMaps != null && allFiveForMaps.size() > 0){
                        int num = 0;
                        for (Map<String, Object> objectMap : allFiveForMaps) {
                            String newXmNumber = objectMap.get("xmNumber") != null ? objectMap.get("xmNumber").toString() : ""; //B
                            String newbudget = objectMap.get("baiLian_budget") != null ? objectMap.get("baiLian_budget").toString() : ""; //C
                            String newzhongUnit = objectMap.get("zhong_biao_unit") != null ? objectMap.get("zhong_biao_unit").toString() : ""; //F
                            String newprovince = objectMap.get("province") != null ? objectMap.get("province").toString() : ""; //G
                            String newcity = objectMap.get("city") != null ? objectMap.get("city").toString() : ""; //G
                            String newcountry = objectMap.get("country") != null ? objectMap.get("country").toString() : ""; //G
                            // 由于异同是指完全匹配的对立面,
                            if (simAndDid(newXmNumber, xmNumber) && simAndDid(newbudget, budget) && simAndDid(zhongUnit, newzhongUnit) && simAndDid(province, newprovince)
                                    && simAndDid(city, newcity) && simAndDid(country, newcountry)){
                                log.info("满足C & D & E & F & H & I & J异同");
                                num ++;
                            }
                        }
                        if (num == allFiveForMaps.size()){
                            dnpId = allFiveForMaps.get(0).get("code") != null ? allFiveForMaps.get(0).get("code").toString() : ""; //A
                            bdJdbcTemplate.update("update han_qc_result_poc_table set code2 = ?, code = ? where content_id = ?", "2.6", dnpId, contentId);
                            flag = false;
                        }
                    }
                }

                /**
                 * 第七层  B & D & E & H & I & J异同
                 */
                if (flag && StringUtils.isNotBlank(allSeven)){
                    // 更新完成后，进行规则查询
                    List<Map<String, Object>> allFiveForMaps = bdJdbcTemplate.queryForList("SELECT content_id, code, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit,progid,province,city, country from han_qc_result_poc_table where allForSevenCode = ? AND updateTime >= ?  order by content_id asc", allForSevenCode, threeDayAgo);
                    if (allFiveForMaps != null && allFiveForMaps.size() > 0){
                        int num = 0;
                        for (Map<String, Object> objectMap : allFiveForMaps) {
                            String newXmNumber = objectMap.get("xmNumber") != null ? objectMap.get("xmNumber").toString() : ""; //B
                            String newamount = objectMap.get("baiLian_amount_unit") != null ? objectMap.get("baiLian_amount_unit").toString() : ""; //D
                            String newzhaoUnit = objectMap.get("zhao_biao_unit") != null ? objectMap.get("zhao_biao_unit").toString() : ""; //E
                            String newprovince = objectMap.get("province") != null ? objectMap.get("province").toString() : ""; //G
                            String newcity = objectMap.get("city") != null ? objectMap.get("city").toString() : ""; //G
                            String newcountry = objectMap.get("country") != null ? objectMap.get("country").toString() : ""; //G
                            // 由于异同是指完全匹配的对立面,
                            if (simAndDid(newXmNumber, xmNumber) && simAndDid(newamount, amount) && simAndDid(newzhaoUnit, zhaoUnit) && simAndDid(province, newprovince)
                                    && simAndDid(city, newcity) && simAndDid(country, newcountry)){
                                log.info("满足B & D & E & H & I & J异同");
                                num ++;
                            }
                        }
                        if (num == allFiveForMaps.size()){
                            dnpId = allFiveForMaps.get(0).get("code") != null ? allFiveForMaps.get(0).get("code").toString() : ""; //A
                            bdJdbcTemplate.update("update han_qc_result_poc_table set code2 = ?, code = ? where content_id = ?", "2.7", dnpId, contentId);
                            flag = false;
                        }
                    }
                }


                List<Map<String, Object>> maps2 = bdJdbcTemplate.queryForList("SELECT content_id from han_qc_result_poc_table where code = ?", dnpId);
                if (maps2 != null && maps.size() > 0){
                    if (maps2.size() >= 4){
                        bdJdbcTemplate.update("update han_qc_result_poc_table set keywords = ? where code = ?", "1", dnpId);
                    }
                }

                bdJdbcTemplate.update("update han_qc_result_poc_table set allForTwoCode = ?, allForThreeCode = ?, allForFourCode = ?, allForFiveCode = ?, allForSixCode = ?, allForSevenCode = ? where content_id = ?", allForTwoCode, allForThreeCode, allForFourCode, allForFiveCode, allForSixCode, allForSevenCode, contentId);
            }
        }
    }


//    public static void main(String[] args) {
//        boolean b = simAndDid("900.00", "120.1");
//        System.out.println(b);
//    }

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

}
