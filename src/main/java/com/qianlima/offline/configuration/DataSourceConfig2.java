/*
package com.qianlima.offline.configuration;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

@Slf4j
@Configuration
@MapperScan(basePackages = "com.qianlima.offline.mapper",sqlSessionTemplateRef="dbSqlSessionTemplate")
public class DataSourceConfig2 {

    //数据源
    @Primary
    @Bean(name = "dbDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.bd")
    public DataSource dataSouce(){
        log.info("开始配置数据源信息--------");
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "dbSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dbDataSource")DataSource dataSource){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        ResourcePatternResolver reslover = new PathMatchingResourcePatternResolver();
        try{
            bean.setMapperLocations(reslover.getResource("classpath*:/mapper*/
/*.xml"));
            return bean.getObject();
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Bean(name = "dbSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("SqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory);
        return template;
    }
}
*/
