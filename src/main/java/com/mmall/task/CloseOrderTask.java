package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissionManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时关闭订单的定时器
 * Created by hasee on 2018/1/11.
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private RedissionManager redissionManager;

    //@Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV1(){
        log.info("定时关单任务开启");
        int hour = Integer.valueOf(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        log.info("定时关单任务结束");
    }

    /**
     * 2.0的定时关单可能由于tomcat集群原因,导致tomcat1上锁后没能进行设置有效期就直接挂掉,导致锁无法释放,其他tomcat也就永远无法获取到锁,定时关单功能直接废掉
     */
    //@Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV2(){
        log.info("定时关单任务开启");
        Long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1){
            //获取锁成功
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            log.info("没有获取到分布式锁{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("定时关单任务结束");
    }

    /**
     * 3.0版本是原生实现的redis分布式锁,可以在tomcat集群环境下让关单操作不会发生多次，每到一分钟只有一个进程可以获取到锁，执行关单操作
     * 同时避免了2.0的死锁情况
     */
    //@Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV3(){
        log.info("定时关单任务开启");
        Long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1){
            //获取锁成功
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            //没有获取到锁继续判断，看看原锁是否过期
            String lockValue = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValue != null && System.currentTimeMillis() > Long.valueOf(lockValue)){
                String getsetResult = RedisShardedPoolUtil.getset(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                if (getsetResult == null || (getsetResult!=null && StringUtils.equals(getsetResult,lockValue))){
                    //如果获取的旧值不存在或者和lockValue相等的话,代表旧值已经被删除或者set过程中没有被其他线程改变过值,即代表获取锁成功
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else {
                    log.info("没有获取到分布式锁{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                log.info("没有获取到分布式锁{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("定时关单任务结束");
    }

    /**
     * 使用Redission定时关闭订单
     */
    //@Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV4(){
        RLock lock = redissionManager.getRedission().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getlock = false;
        try {
            if (getlock = lock.tryLock(0,50, TimeUnit.SECONDS)){
                log.info("获取到分布式锁:{},threadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
                //iOrderService.closeOrder(hour);
            }else {
                log.info("未获取到分布式锁:{},threadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.info("获取分布式锁异常",e);
        } finally {
            if (!getlock){
                return;
            }
            lock.unlock();
            log.info("Redission释放分布式锁");
        }
    }

    /**
     * 关闭订单(私有方法)
     * @param lockName
     */
    private void closeOrder(String lockName){
        //给锁设置有效期,防止死锁
        RedisShardedPoolUtil.expire(lockName,5);
        //属性文件获取要取消多久没付款的订单
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        //关闭订单后需要手动释放锁
        RedisShardedPoolUtil.del(lockName);
        log.info("释放锁{} ,threadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("-----------------------");
    }
}
