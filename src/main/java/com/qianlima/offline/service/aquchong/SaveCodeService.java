package com.qianlima.offline.service.aquchong;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SaveCodeService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private String SELECT_SQL = "SELECT content_id, title,xmNumber,baiLian_budget, baiLian_amount_unit,zhao_biao_unit,zhong_biao_unit," +
            "progid,province,city, country  FROM han_qc_result_poc_table order by content_id asc";

    private String[] blacks = { "0次","1次","2次","3次","4次","5次","6次","7次","8次","9次","一次","二次","三次","四次","五次","六次","七次","八次","九次","十次","0回","1回","2回","3回","4回","5回","6回","7回","8回","9回","一回","二回","三回","四回","五回","六回","七回","八回","九回","十回","0标","1标","2标","3标","4标","5标","6标","7标","8标","9标","一标","二标","三标","四标","五标","六标","七标","八标","九标","十标","0包","1包","2包","3包","4包","5包","6包","7包","8包","9包","一包","二包","三包","四包","五包","六包","七包","八包","九包","十包","a包","b包","c包","d包","e包","f包","g包","h包","i包","j包","k包","l包","m包","n包","o包","p包","q包","r包","s包","t包","o包","v包","w包","x包","y包","z包","A包","B包","C包","D包","E包","F包","G包","H包","I包","J包","K包","L包","M包","N包","O包","P包","Q包","R包","S包","T包","O包","V包","W包","X包","Y包","Z包","0期","1期","2期","3期","4期","5期","6期","7期","8期","9期","一期","二期","三期","四期","五期","六期","七期","八期","九期","十期","包0","包1","包2","包3","包4","包5","包6","包7","包8","包9","包一","包二","包三","包四","包五","包六","包七","包八","包九","包十","包a","包b","包c","包d","包e","包f","包g","包h","包i","包j","包k","包l","包m","包n","包o","包p","包q","包r","包s","包t","包o","包v","包w","包x","包y","包z","包A","包B","包C","包D","包E","包F","包G","包H","包I","包J","包K","包L","包M","包N","包O","包P","包Q","包R","包S","包T","包O","包V","包W","包X","包Y","包Z","段0","段1","段2","段3","段4","段5","段6","段7","段8","段9","段一","段二","段三","段四","段五","段六","段七","段八","段九","段十","段a","段b","段c","段d","段e","段f","段g","段h","段i","段j","段k","段l","段m","段n","段o","段p","段q","段r","段s","段t","段o","段v","段w","段x","段y","段z","段A","段B","段C","段D","段E","段F","段G","段H","段I","段J","段K","段L","段M","段N","段O","段P","段Q","段R","段S","段T","段O","段V","段W","段X","段Y","段Z","批次1","批次2","批次3","批次4","批次5","批次6","批次7","批次8","批次9","批次一","批次二","批次三","批次四","批次五","批次六","批次七","批次八","批次九","批次十","1批次","2批次","3批次","4批次","5批次","6批次","7批次","8批次","9批次","一批次","二批次","三批次","四批次","五批次","六批次","七批次","八批次","九批次","十批次" };

    AtomicInteger atomicInteger = new AtomicInteger(0);

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

                // A & B & C & D & E & F & G & H & I & J相同 (相同”指字段“无值”的情况下相等或者“有值”的情况下相等)
                String allOne = title + xmNumber + budget + amount + zhaoUnit + zhongUnit + progid + province + city + country;

                String allTwo = "";
                boolean flagCode = true;
                for (String black : blacks) {
                    if (title.toUpperCase().contains(black.toUpperCase())){
                        flagCode = false;
                        break;
                    }
                }
                // 标题含有“标段” 不生成 code 码  同时 B & G完全匹配，C & D & F相同
                if (flagCode && StringUtils.isNotBlank(xmNumber) && StringUtils.isNotBlank(progid)){
                    allTwo = xmNumber + progid;
                }

                // A & G完全匹配，C & D & E & F & H & I & J异同
                String allThree = "";
                if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(progid)){
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

                // 获取code码
                String allCode = String.valueOf(allOne.toUpperCase().hashCode());
                String allForTwoCode = String.valueOf(allTwo.toUpperCase().hashCode());
                String allForThreeCode = String.valueOf(allThree.toUpperCase().hashCode());
                String allForFourCode = String.valueOf(allFour.toUpperCase().hashCode());
                String allForFiveCode = String.valueOf(allFive.toUpperCase().hashCode());


                log.info("处理到了infoId：{}, 当前是第num：{} 条", contentId, atomicInteger.addAndGet(1));
                bdJdbcTemplate.update("update han_qc_result_poc_table set code = ?, allCode = ?, allForTwoCode = ?, allForThreeCode = ?, allForFourCode = ?, allForFiveCode = ? where content_id = ?", contentId, allCode, allForTwoCode, allForThreeCode, allForFourCode, allForFiveCode, contentId);
            }
        }
    }











}
