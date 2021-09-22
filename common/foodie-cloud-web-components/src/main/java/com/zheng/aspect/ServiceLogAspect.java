package com.zheng.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLogAspect {


    public static final Logger log = LoggerFactory.getLogger(ServiceLogAspect.class);
    /*
    * AOP通知
    * 1.前置通知：在方法调用之前执行
    * 2.后置通知：在方法正常调用之后执行
    * 3.环绕通知：在方法调用之前和之后，都分别可以执行的通知
    * 4.异常通知：如果在方法调用过程中发生异常，则通知
    * 5.最终通知：在方法调用之后通知
    * */


    /**
     * 切面表达式：
     * execution 代表所要执行的的表达式主体
     * 第一处 * 代表方法返回类型， * 代表任何类型
     * 第二处 包名代表aop监控的类所在的包
     * 第三处 .. 代表该包以及子包下的所有类方法
     * 第四处 * 代表类名，* 代表所有类
     * 最终 .*.*代表任何类，任何方法 (..)任意参数
     *
     * @param joinPoint 切入点
     * @return object
     * @throws Throwable throw
     */
    @Around("execution(* com.zheng..*.impl..*.*(..)))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("========开始执行{}.{}========",
                            joinPoint.getTarget().getClass(),
                            joinPoint.getSignature().getName());
        //记录开始时间
        long begin = System.currentTimeMillis();
        //执行目标service
        Object result = joinPoint.proceed();
        //记录结束时间
        Long end = System.currentTimeMillis();
        Long takeTime = end -begin;

        if(takeTime>3000){
            log.error("====执行结束，耗时：{}毫秒=====",takeTime);
        }else if(takeTime>2000){
            log.warn("====执行结束，耗时：{}毫秒=====",takeTime);
        }else {
            log.info("====执行结束，耗时：{}毫秒=====",takeTime);
        }


        return result;



    }

}
