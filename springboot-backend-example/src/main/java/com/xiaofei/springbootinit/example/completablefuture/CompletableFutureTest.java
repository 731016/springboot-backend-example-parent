package com.xiaofei.springbootinit.example.completablefuture;

import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.exception.BusinessException;
import com.xiaofei.springbootinit.model.entity.User;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tuaofei
 * @description CompletableFuture
 * 并行处理
 * （1）单个处理返回
 * （2）一起返回处理
 * https://javaguide.cn/java/concurrent/completablefuture-intro.html#future-%E4%BB%8B%E7%BB%8D
 * @date 2024/12/23
 */
@Log4j2
public class CompletableFutureTest {

    private static ThreadPoolExecutor threadPoolExecutor = null;

    private static User user = new User();

    static {
        threadPoolExecutor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public static void main(String[] args) {
        //异步执行两个任务
//        task1();
//        task2();

        //等待三个任务都完成，才执行
//        queryData();

        //task2等待task1执行完成后，再执行
        task3();


        log.info("所有任务执行完成! {}", user);
    }

    private static void task3() {
        try {
            // 创建第一个任务，直接返回 User 对象
            CompletableFuture<User> future = CompletableFuture.supplyAsync(() ->
                            {
                                int i = 1 / 0;
                                return getUser("汪浩天");
                            }
                            , threadPoolExecutor)
                    .handle((user, throwable) -> {
                        if (throwable != null) {
                            log.error("task1 执行异常", throwable);
                            // 可以返回默认值或者抛出自定义异常
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR, throwable.getMessage());
                        }
                        log.info("task1 执行结果 {}", user.getUserName());
                        return user;
                    });

            // 等待第一个任务完成后执行第二个任务
            future.thenCompose(firstResult ->
                    CompletableFuture.supplyAsync(() ->
                                            getUser("涂鏊飞")
                                    , threadPoolExecutor)
                            .handle((user, throwable) -> {
                                if (throwable != null) {
                                    log.error("task2 执行异常", throwable);
                                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, throwable.getMessage());
                                }
                                log.info("task2 执行结果 {}", user.getUserName());
                                return user;
                            })
            ).join(); // 等待整个任务链完成
        } catch (Exception e) {
            log.error(e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    private static void queryData() {
        CompletableFuture<Void> taskUserAccountFuture = CompletableFuture.runAsync(() -> {
            fillUserAccount();
        }, threadPoolExecutor);

        CompletableFuture<Void> taskMpOpenIdFuture = CompletableFuture.runAsync(() -> {
            int i = 1 / 0;
            fillMpOpenId();
        }, threadPoolExecutor);

        CompletableFuture<Void> taskUnionIdFuture = CompletableFuture.runAsync(() -> {
            fillUnionId();
        }, threadPoolExecutor);

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(taskUserAccountFuture, taskMpOpenIdFuture, taskUnionIdFuture);

        allFuture.join();

        log.info("{queryData} 任务执行完成!");

    }

    private static void fillUserAccount() {
        user.setUserAccount("xiaofei");
        log.info("fill user account!");
    }

    private static void fillMpOpenId() {
        user.setMpOpenId(UUID.randomUUID().toString());
        log.info("fill mp openid!");
    }

    private static void fillUnionId() {
        user.setUnionId(UUID.randomUUID().toString());
        log.info("fill union id!");
    }

    private static void task1() {
        try {
            Thread.sleep(2000);
            CompletableFuture<User> supplyAsync = CompletableFuture.supplyAsync(() -> {
                return getUser("汪浩天");
            }, threadPoolExecutor);
            supplyAsync.handle((user, throwable) -> {
                if (throwable != null) {
                    log.error("执行异常", throwable);
                    return null;
                }
                log.info("执行结果 {}", user.getUserName());
                return user;
            });
        } catch (Exception e) {
            log.error(e);
        }
    }

    private static void task2() {
        CompletableFuture<User> supplyAsync = CompletableFuture.supplyAsync(() -> {
            return getUser("涂鏊飞");
        }, threadPoolExecutor);
        supplyAsync.handle((user, throwable) -> {
            if (throwable != null) {
                log.error("执行异常", throwable);
                return null;
            }
            log.info("执行结果 {}", user.getUserName());
            return user;
        });
    }


    public static User getUser(String userName) {
        try {
            Thread.sleep(1000);
            User user = new User();
            user.setUserName(userName);
            log.info(userName);
            return user;
        } catch (Exception e) {
            log.error(e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
