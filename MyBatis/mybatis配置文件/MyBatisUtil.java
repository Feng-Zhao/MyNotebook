package util;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyBatisUtil {
    private static String resource;
    private volatile static SqlSessionFactory sqlSessionFactory;

    // 双锁 + volatile 单例 此处 volatile 阻止指令重排序,保证写内存之前不会有读内存操作
    public static SqlSessionFactory getSqlSessionFactory() throws IOException {
        if(sqlSessionFactory == null){
            synchronized (MyBatisUtil.class) {
                if(sqlSessionFactory == null) {
                    Properties properties = new Properties();
                    InputStream propIns = Resources.getResourceAsStream("mybatis.properties");
//                    InputStream propIns = MyBatisUtil.class.getResourceAsStream("/mybatis.properties");

//                    ClassPathResource classPathResource = new ClassPathResource("mybatis.properties");
//                    InputStream propIns = classPathResource.getInputStream();
                    properties.load(propIns);
                    resource = properties.getProperty("configLocation");
                    InputStream ins = Resources.getResourceAsStream(resource);
                    sqlSessionFactory = new SqlSessionFactoryBuilder().build(ins);
                    propIns.close();
                    // SqlSessionFactoryBuilder 关闭了流
//                    ins.close();
                }
            }
        }
        return sqlSessionFactory;
    }

    private MyBatisUtil(){

    }

    public static SqlSession getSqlSession() throws IOException {
        return getSqlSessionFactory().openSession();
    }
}

/*
// 静态内部类加载 实现单例, 静态内部类只有在 调用 getInstance() 才初始化
// 即保证线程安全,又实现懒加载
public class SingletonDemo {
    // 私有静态内部类 保证 单例+线程安全+懒加载
    private static class SingletonHolder{
        private static final SingletonDemo instance=new SingletonDemo();
    }
    // 私有构造器,保证不被实例化
    private SingletonDemo(){
        System.out.println("Singleton has loaded");
    }

    public static SingletonDemo getInstance(){
        return SingletonHolder.instance;
    }
}
 */

/*
// Effective Java 作者 Josh Bloch 推荐
// 绝对无法实例化,单例,线程安全,懒加载
enum SingletonDemo{
    INSTANCE;
    public void otherMethods(){
        System.out.println("Something");
    }
}
// 调用时
SingletonDemo.INSTANCE.otherMethods();
 */
