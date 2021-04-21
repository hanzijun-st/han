package com.qianlima.offline.bean;

public class Constant {

    public static final String SELECT_EXISTS_RESULT = "select content_id from data_producer where content_id =? and task_id=?";
    public static final String INSERT_RESULT = "insert into data_producer (content_id,task_id) values (?,?)";
    //公告标签修复队列--key
    public static final String NOTICE_MONGO_KEY = "ka_mongo";
    //交换机
    public static final String EXCHANGE_NAME = "ka_exchange";

    //交换机name
    public static final String NOW_EXCHANGE_NAME = "cusdata_current";
    //队列name
    public static final String NOW_QUEUE_KEY = "cusdata_current_key";

    //历史交换机name
    public static final String HSITORY_EXCHANGE_NAME = "cusdata_history";
    //历史队列name
    public static final String HSITORY_QUEUE_KEY = "cusdata_history_key";
}
