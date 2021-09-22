package com.zheng.order.controller;

import com.zheng.controller.BaseController;
import com.zheng.enums.OrderStatusEnum;
import com.zheng.enums.PayMethod;
import com.zheng.order.pojo.OrderStatus;
import com.zheng.order.pojo.bo.PlaceOrderBO;
import com.zheng.order.pojo.bo.SubmitOrderBO;
import com.zheng.order.pojo.vo.MerchantOrdersVO;
import com.zheng.order.pojo.vo.OrderVO;
import com.zheng.order.service.OrderService;
import com.zheng.pojo.JsonResult;
import com.zheng.pojo.ShopcartBO;
import com.zheng.utils.CookieUtils;
import com.zheng.utils.JsonUtils;
import com.zheng.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


//@Controller
@Api(value = "订单相关", tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisOperator redisOperator;
    @ApiOperation(value = "用户下单",notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public JsonResult create(
            @RequestBody SubmitOrderBO submitOrderBO,
            HttpServletRequest request,
            HttpServletResponse response){

        if(submitOrderBO.getPayMethod()!= PayMethod.WEIXIN.type&&submitOrderBO.getPayMethod()!= PayMethod.ALIPAY.type){
            return JsonResult.errorMsg("支付方式不支持！");
        }
//        System.out.println(submitOrderBO);

        String shopcartJson = redisOperator.get(FOODIE_SHOPCART+":"+submitOrderBO.getUserId());
        if(StringUtils.isBlank(shopcartJson)) {
            return JsonResult.errorMsg("购物数据不正确");
        }
        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

        //1.创建订单
        PlaceOrderBO placeOrderBO = new PlaceOrderBO(submitOrderBO,shopcartList);
        OrderVO orderVO = orderService.createOrder(placeOrderBO);
        String orderId = orderVO.getOrderId();

        //2.创建订单以后，移除购物车中已结算（已提交）的商品
        //清理覆盖现有的redis中的购物数据
        shopcartList.removeAll(orderVO.getToBeRemoveShopcartList());
        redisOperator.set(FOODIE_SHOPCART+":"+submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));
        //整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie
        CookieUtils.setCookie(request,response,FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartList),true);

        //3.向支付中心发送当前订单，用于保存支付中心的订单
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);

        //为了方便测试购买，所以所有的支付金额统一改为一分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("imoocUserId","imooc");
        headers.add("password","imooc");

//        headers.add("imoocUserId","zheng");
//        headers.add("password","zheng");
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO,headers);
        ResponseEntity<JsonResult> responseEntity = restTemplate.postForEntity(paymentUrl,entity,JsonResult.class);
        JsonResult paymentResult = responseEntity.getBody();
        if(paymentResult.getStatus()!=200){
            return JsonResult.errorMsg("支付中心订单创建失败，请联系管理员！");
        }
        return JsonResult.ok(orderId);
    }

    @PostMapping("notifyMerchatOrderPaid")
    public Integer notifyMerchatOrderPaid(String merchatOrderId){
        orderService.updateOrderStatus(merchatOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }
    @PostMapping("/getPaidOrderInfo")
    public JsonResult getPaidOrderInfo(String orderId) {
        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return JsonResult.ok(orderStatus);
    }

}
