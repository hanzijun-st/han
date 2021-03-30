package com.qianlima.offline.configuration;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * @Title: MongoConfig
 */
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.test}")
    private String testUri;

    @Value("${spring.data.mongodb.qly}")
    private String qlyUri;

    @Primary
    @Bean(name = "testMongoDbFactory")
    public MongoDbFactory cusdataMongoDbFactory(){
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        MongoClientURI mongoClientURI=new MongoClientURI(testUri,builder);
        SimpleMongoDbFactory simpleMongoDbFactory=new SimpleMongoDbFactory(mongoClientURI);
        return  simpleMongoDbFactory;
    }

    @Bean(name = "qlyMongoDbFactory")
    public MongoDbFactory qianliyanMongoDbFactory(){
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        MongoClientURI mongoClientURI=new MongoClientURI(qlyUri,builder);
        SimpleMongoDbFactory simpleMongoDbFactory=new SimpleMongoDbFactory(mongoClientURI);
        return  simpleMongoDbFactory;
    }


    @Primary
    @Bean(name = "testMongoTemplate") //创建方法 交给  spring 创建的名称 为 mainMongo
    public MongoTemplate cusdataMongoTemplate(@Qualifier("testMongoDbFactory") MongoDbFactory mongoDbFactory) throws Exception {
        return new MongoTemplate(mongoDbFactory);
    }


    @Bean(name = "qlyMongoTemplate") //创建方法 交给  spring 创建的名称 为 mainMongo
    public MongoTemplate qianliyanMongoTemplate(@Qualifier("qlyMongoDbFactory") MongoDbFactory mongoDbFactory) throws Exception {
        return new MongoTemplate(mongoDbFactory);
    }

}