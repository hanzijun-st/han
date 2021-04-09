package com.qianlima.offline.service.han.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.qianlima.offline.bean.*;
import com.qianlima.offline.entity.Enterprise;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestMongoService;
import com.qianlima.offline.util.MongoDBUtil;
import com.qianlima.offline.util.ReadFileUtil;
import com.qianlima.offline.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class TestMongoServiceImpl implements TestMongoService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Resource
    @Qualifier("testMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Resource
    @Qualifier("qlyMongoTemplate")
    private MongoTemplate qlyMongoTemplate;

    @Autowired
    private CurrencyService currencyService;


    public static final String UPDATE_ZT_RESULT_TYPE = "UPDATE han_unit SET actualCapital=?,estiblishTime=?,regLocation=?,industry=?,base=?,top01=?,top02=? WHERE unit_name =?";


    @Override
    public void getMongo(String str) {
        MongoCollection<Document> project = MongoDBUtil.getConnect().getCollection(str);

        //指定查询过滤器
        Bson filter = Filters.eq("project_id", "22_77a136c6774e46848eaf4225fc977fd4");
        //指定查询过滤器查询
        FindIterable findIterable = project.find(filter);
        MongoCursor cursor = findIterable.iterator();
        while (cursor.hasNext()) {
            log.info("过滤查询结果->{}",cursor.next());
        }
        //查找集合中的所有文档
        FindIterable findIterable2 = project.find();
        //取出查询到的第一个文档
        Document document = (Document) findIterable2.first();

    }

    @Override
    public void getMongoTest() {
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT unit_name FROM han_unit");
        if (mapList !=null && mapList.size()>0){
            for (Map<String, Object> map : mapList) {
                String name = map.get("unit_name").toString();
                Query query = new Query();
                query.addCriteria(Criteria.where("name").is(name));
                Enterprise one = mongoTemplate.findOne(query, Enterprise.class);
                if (one != null){
                    //saveIntoMysqlTenxun(one,UPDATE_ZT_RESULT_TYPE);
                }else{
                    log.info("该中标单位为空：{}",map.get("unit_name"));
                }
            }
        }
    }

    @Override
    public void getTest() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT id, unit_name FROM han_unit");
        if (maps != null && maps.size() > 0){
            for (Map<String, Object> map : maps) {
                futureList1.add(executorService1.submit(() -> {
                    Integer id = (Integer) map.get("id");
                    String company = map.get("unit_name").toString();
                    Enterprise enterprise = queryForName(company);
                    if (enterprise != null){
                        // 注册资金  成立日期  注册地址  行业分类  省编码
                        //String aa = enterprise.getLegalPersonName();
                        String bbb = enterprise.getPhoneNumber();
                        saveIntoMysqlTenxun(enterprise,UPDATE_ZT_RESULT_TYPE, bbb, null);


                        //bdJdbcTemplate.update("update han_unit set top01 = ? , top02 = ? where id = ?", aa, bbb, id);
                    }
                }));
            }

            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            log.info("=======================该方法直接结束===================");

        }
    }

    @Override
    public void getTestMo() {
        String idParam ="";
        List<String> listAll = new ArrayList<>();
        while (true){
            List<NewareaToOldarea> list = getList(idParam);
            if (list !=null && list.size()>0){
                for (NewareaToOldarea newareaToOldarea : list) {
                    idParam = newareaToOldarea.get_id();
                    //处理对应的业务逻辑
                    listAll.add(newareaToOldarea.getNewName());
                }
            }else{
                break;
            }
        }
        //本地
        currencyService.readFileByNameBd("aaa",listAll);
    }

    private List<NewareaToOldarea> getList(String idParam){
        Query query = new Query();
        if (StrUtil.isNotEmpty(idParam)){
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(idParam)));
        }
        query.with(new Sort(Sort.Direction.ASC,"_id")).limit(1000);
        List<NewareaToOldarea> newareaToOldareas = mongoTemplate.find(query, NewareaToOldarea.class);
        return newareaToOldareas;
    }
    private Enterprise queryForName(String zhongbiaounit) {
        if (StringUtils.isBlank(zhongbiaounit)){
            return null;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(zhongbiaounit));
        return mongoTemplate.findOne(query, Enterprise.class);
    }

    public void saveIntoMysqlTenxun(Enterprise one ,String table,String aa,String bbb){
        Long estiblishTime = one.getEstiblishTime();
        Date date=null;
        if (estiblishTime !=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = sdf.format(new Date(estiblishTime));
            try {
                date = DateUtils.parseDate(format, "yyyy-MM-dd HH:mm:ss");
            } catch (ParseException e) {

            }
        }
        bdJdbcTemplate.update(table,one.getActualCapital(),date,one.getRegLocation(),one.getIndustry(),one.getBase(),aa,bbb,one.getName());
        log.info("存mysql数据库进度--->{}",one.getName());
    }
}