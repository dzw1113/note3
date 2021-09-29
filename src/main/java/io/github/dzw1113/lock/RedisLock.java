package io.github.dzw1113.lock;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import redis.clients.jedis.Jedis;

/**
 * @description: redis分布式锁
 * @author: dzw
 * @date: 2021/09/29 09:01
 **/
public class RedisLock {
    
    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
//        genCode("hd");
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        String key = "l";
        String value = "500";
        String status = jedis.set(key, value, "nx", "ex", 5);
        System.out.println("占锁成功" + status);
        AtomicInteger cnt = new AtomicInteger();
        //每三秒续一次锁，续三次
        Thread thread1 = new Thread(() -> {
            while (cnt.get() < 3) {
                String expTime = "5";
                try {
                    Thread.sleep(3 * 1000);
                    cnt.getAndIncrement();
                    Long obj = (Long) jedis.eval("if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('expire',KEYS[1], ARGV[2]) else return 0 end", Collections.singletonList(key), Arrays.asList(value,expTime));
                    System.out.println("续锁:"+obj);
                    if(obj == 0){
                        System.out.println("key已经移除退出续锁线程");
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().isInterrupted();
                }
            }
        });
        //每四秒续常识删一次，循环三次
        AtomicInteger delCnt = new AtomicInteger();
        Thread threadDel = new Thread(() -> {
            while (delCnt.get() < 3) {
                try {
                    Thread.sleep(3 * 1000);
                    delCnt.getAndIncrement();
                    Long obj = (Long) jedis.eval("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Collections.singletonList(key), Collections.singletonList(value));
                    System.out.println("删锁" + obj);
                    if(obj == 0){
                        System.out.println("key已经移除退出删锁线程");
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().isInterrupted();
                }
            }
        });
        
        //去抢占锁三次
        AtomicInteger tryGetLock = new AtomicInteger();
        String newValue = "22";
        
        Thread thread2 = new Thread(() -> {
            while (tryGetLock.get() < 20) {
                try {
                    Thread.sleep(1 * 1000);
                    tryGetLock.getAndIncrement();
                    String getLock = jedis.set(key, newValue, "nx", "ex", 5);
                    System.out.println("抢占锁:"+getLock);
                    if("OK".equals(getLock)){
                        System.out.println("抢占锁成功");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().isInterrupted();
                }
            }
        });

//        threadDel.start();
//        threadDel.join();
        
        thread1.start();
        thread1.join();
        
        thread2.start();
        thread2.join();
    }
}
