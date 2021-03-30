package com.qianlima.offline.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig implements EnvironmentAware {

    // 配置文件
    Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("获取配置文件");
        this.environment = environment;
    }

    /**
     * 官网jdbc数据源n
     */
    @Bean(name = "gwDataSource")
    @Qualifier("gwDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.gw")
    public DataSource gwDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 官网jdbc数据源JdbcTemplet1
     */
    @Bean(name = "gwJdbcTemplate")
    public JdbcTemplate gwJdbcTemplate(@Qualifier("gwDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 13数据源
     */
    @Bean(name = "bdDataSource")
    @Qualifier("bdDataSource")
    @ConfigurationProperties(prefix="spring.datasource.bd")
    public DataSource bdPrimaryDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * jdbcs数据源JdbcTemplet1
     */
    @Bean(name = "bdJdbcTemplate")
    public JdbcTemplate bdPrimaryJdbcTemplate(@Qualifier("bdDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    /**
     * 数据源连接数据库
     */
    @Bean(name = "myDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.my")
    //@Primary // 这个注解是说明此是默认数据源，必须设定默认数据源。（即主数据源）
    public DataSource stockDataSource() {
        return new DruidDataSource();
    }

    /**
     * sqlSessionFactory工厂
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "mySqlSessionFactory")
    @Primary
    public SqlSessionFactory stockSqlSessionFactory(@Qualifier("myDataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //支持属性使用驼峰的命名,mapper配置不需要写字段与属性的配置，会自动映射。
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        //使用jdbc的getGeneratedKeys获取数据库自增主键值
        configuration.setUseGeneratedKeys(true);
        //使用列别名替换列名 select user as User
        configuration.setUseColumnLabel(true);
        //-自动使用驼峰命名属性映射字段   userId    user_id
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        // xml路径
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "myTransactionManager")
    @Primary
    public DataSourceTransactionManager stockTransactionManager(
            @Qualifier("myDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate stockSqlSessionTemplate(
            @Qualifier("mySqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
