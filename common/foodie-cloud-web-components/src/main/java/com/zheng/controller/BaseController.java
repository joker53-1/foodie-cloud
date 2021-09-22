package com.zheng.controller;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.UUID;

@Controller
public class BaseController {


    public static final String FOODIE_SHOPCART = "shopcart";
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    //支付中心的调用地址
    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";
//    String paymentUrl = "http://localhost:8089/payment/createMerchantOrder";

    //微信支付成功 ->支付中心->天天吃货平台
    //                      |-回调通知的url
//    String payReturnUrl = "http://qgcn7h.natappfree.cc/orders/notifyMerchatOrderPaid";
     public String payReturnUrl = "http://47.111.143.81:8088/foodie-dev-api/orders/notifyMerchatOrderPaid";
    //用户上传头像的位置
//    public static final String IMAGE_USER_FACE_LOCATION = "D:\\zsj\\foodie-dev\\faces";
    public static final String IMAGE_USER_FACE_LOCATION = "D:"+ File.separator+"zsj"+ File.separator+
            "foodie-dev"+ File.separator+"faces";

//
//    public UsersVO conventUsersVO(Users userResult){
//        //实现用户的redis会话
//        String uniqueToken = UUID.randomUUID().toString().trim();
//        redisOperator.set(REDIS_USER_TOKEN+":"+userResult.getId(),
//                uniqueToken);
//
//        UsersVO usersVO = new UsersVO();
//        BeanUtils.copyProperties(userResult,usersVO);
//        usersVO.setUserUniqueToken(uniqueToken);
//
//        return usersVO;
//    }


    //FIXME 下面的逻辑移植到订单中心
//    @Autowired
//    public MyOrdersService myOrdersService;
//    /**
//     * 用于验证用户和订单是否有关联关系，避免非法用户调用
//     * @param userId
//     * @param orderId
//     * @return
//     */
//    public JsonResult checkUserOrder(String userId, String orderId){
//        Orders orders = myOrdersService.queryMyOrder(userId,orderId);
//        if(orders==null){
//            return JsonResult.errorMsg("订单不存在");
//        }
//        return JsonResult.ok(orders);
//    }
}
