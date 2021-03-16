package com.qianlima.offline.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;


public class MongoDBUtil {

    //@Value("${spring.data.mongodb.uri.mongodb}")
    //private String mongoUrl;

    //mongodb 连接数据库工具类
    //不通过认证获取连接数据库对象
    public static MongoDatabase getConnect(){
        //连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("172.18.30.12", 27017);

        //连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("move");

        //返回连接数据库对象
        return mongoDatabase;
    }

  /*  //需要密码认证方式连接
    public static MongoDatabase getConnect2(){
        List<ServerAddress> adds = new ArrayList<>();
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress = new ServerAddress("localhost", 27017);
        adds.add(serverAddress);

        List<MongoCredential> credentials = new ArrayList<>();
        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential("username", "databaseName", "password".toCharArray());
        credentials.add(mongoCredential);

        //通过连接认证获取MongoDB连接
        MongoClient mongoClient = new MongoClient(adds, credentials);

        //连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("test");

        //返回连接数据库对象
        return mongoDatabase;
    }*/
}