package com.qianlima.offline.service.han.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.qianlima.offline.service.han.TestMongoService;
import com.qianlima.offline.util.MongoDBUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestMongoServiceImpl implements TestMongoService {

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
}