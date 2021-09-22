package com.zheng;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class RedisLimiterConfiguration {

    @Bean
    @Primary
    public KeyResolver remoteAddrKeyResolver(){
        return exchange -> Mono.just(
                exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress()
        );
    }

    @Bean("redisRateLimiterUser")
    @Primary
    public RedisRateLimiter redisRateLimiterUser(){
        return new RedisRateLimiter(10,20);
    }

    @Bean("redisRateLimiterItem")
    public RedisRateLimiter redisRateLimiterItem(){
        return new RedisRateLimiter(20,50);
    }

}
