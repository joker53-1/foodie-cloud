package com.zheng;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfiguration {

    @Autowired
    private KeyResolver hostNameResolver;

    @Autowired
    @Qualifier("redisRateLimiterUser")
    private RedisRateLimiter rateLimiterUser;

    @Bean
    public RouteLocator route(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/address/list",
                        "/address/add",
                        "/address/update",
                        "/address/setDefalut",
                        "/address/delete",
                        "/userInfo/**", "/center/**")
                        //TODO
//                        .filters(f -> f.filter(authFilter))
                        .uri("lb://FOODIE-USER-SERVICE")
                )
                .route(r -> r.path("/auth-service/refresh")
                        .uri("lb://FOODIE-AUTH-SERVICE")
                )
//                .route(r -> r.path("/search/**", "/index/**", "/items/search", "/items/catItems")
//                        .uri("lb://FOODIE-SEARCH-SERVICE")
//                )
                .route(r -> r.path("/address/**", "/passport/**", "/userInfo/**", "/center/**")
                                .filters(f -> f.requestRateLimiter(c -> {
                                    c.setKeyResolver(hostNameResolver);
                                    c.setRateLimiter(rateLimiterUser);
//                            c.setStatusCode(HttpStatus.BAD_GATEWAY);
                                }))
                                .uri("lb://FOODIE-USER-SERVICE")
                )
                .route(r -> r.path("/items/**")
                        .uri("lb://FOODIE-ITEM-SERVICE")
                )
                .route(r -> r.path("/shopcart/**")
                        .uri("lb://FOODIE-CART-SERVICE")
                )
                .route(r -> r.path("/orders/**", "/myorders/**", "/mycomments/**")
                        .uri("lb://FOODIE-ORDER-SERVICE")
                )
                .build();
    }
}
