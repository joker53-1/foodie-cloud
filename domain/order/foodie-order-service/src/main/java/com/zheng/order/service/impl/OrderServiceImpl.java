package com.zheng.order.service.impl;

import com.zheng.enums.OrderStatusEnum;
import com.zheng.enums.YesOrNo;
import com.zheng.item.pojo.Items;
import com.zheng.item.pojo.ItemsSpec;
import com.zheng.item.service.ItemService;
import com.zheng.order.mapper.OrderItemsMapper;
import com.zheng.order.mapper.OrderStatusMapper;
import com.zheng.order.mapper.OrdersMapper;
import com.zheng.order.pojo.OrderItems;
import com.zheng.order.pojo.OrderStatus;
import com.zheng.order.pojo.Orders;
import com.zheng.order.pojo.bo.PlaceOrderBO;
import com.zheng.pojo.ShopcartBO;
import com.zheng.order.pojo.bo.SubmitOrderBO;
import com.zheng.order.pojo.vo.MerchantOrdersVO;
import com.zheng.order.pojo.vo.OrderVO;
import com.zheng.order.service.OrderService;
import com.zheng.user.pojo.UserAddress;
import com.zheng.user.service.AddresslService;
import com.zheng.utils.DateUtil;
import com.zheng.utils.RedisOperator;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemsMapper orderItemsMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private AddresslService addresslService;
    @Autowired
    private ItemService itemService;

//    @Autowired
//    private LoadBalancerClient client;
//    @Autowired
//    private RestTemplate restTemplate;

    @Autowired
    private RedisOperator redisOperator;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrderBO placeOrderBO) {
        List<ShopcartBO> shopcartList = placeOrderBO.getItems();
        SubmitOrderBO submitOrderBO = placeOrderBO.getOrder();
        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        //?????????????????????0
        Integer postAmount = 0;

        String orderId = sid.nextShort();
        //FIXME ??????feign??????????????????
        UserAddress address = addresslService.queryUserAddress(userId,addressId);
//        ServiceInstance instance = client.choose("FOODIE-USER-SERVICE");
//        String url = String.format("http://%s:%s/address-api/queryAddress"+
//                "?userId=%s&addressId=%s",
//                instance.getHost(),
//                instance.getPort(),
//                userId,addressId);
//        UserAddress address = restTemplate.getForObject(url,UserAddress.class);
        //1.?????????????????????
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);
        newOrder.setReceiverName(address.getReceiver());
        newOrder.setReceiverMobile(address.getMobile());
        newOrder.setReceiverAddress(address.getProvince()+" "
                +address.getCity()+" "
                +address.getDistrict()+
                " "+address.getDetail());
        //newOrder.setTotalAmount();
//        newOrder.setRealPayAmount();
        newOrder.setPostAmount(postAmount);
        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);
        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());
        //2.????????????itemSpecIds???????????????????????????
        String itemSpecIdArr[] = itemSpecIds.split(",");
        Integer totalAmount = 0; //??????????????????
        Integer realPayAmount = 0;//????????????????????????????????????
        List<ShopcartBO> toBeRemoveShopcartList = new ArrayList<>();
        for (String itemSpecId :itemSpecIdArr){

            ShopcartBO cartItem = getBuyCountsFromShopcart(shopcartList,itemSpecId);
            // ??????redis????????????????????????????????????redis?????????????????????

            int buyCounts = cartItem.getBuyCounts();
            toBeRemoveShopcartList.add(cartItem);

            //2.1????????????id???????????????????????????????????????????????????
            //FIXME ??????feign??????????????????
            ItemsSpec itemsSpec = itemService.queryItemSpecById(itemSpecId);
//            ServiceInstance itemAPi = client.choose("FOODIE-ITEM-SERVICE");
//            url = String.format("http://%s:%s/item-api/singleItemSpec"+
//                            "?specId=%s",
//                    itemAPi.getHost(),
//                    itemAPi.getPort(),
//                    itemSpecId);
//            ItemsSpec itemsSpec = restTemplate.getForObject(url,ItemsSpec.class);
            totalAmount += itemsSpec.getPriceNormal()*buyCounts;
            realPayAmount += itemsSpec.getPriceDiscount()*buyCounts;

            //2.2????????????id???????????????????????????????????????
            String itemId = itemsSpec.getItemId();
            //FIXME ??????feign??????????????????
            Items items = itemService.queryItemById(itemId);
//            ServiceInstance itemAPi1 = client.choose("FOODIE-ITEM-SERVICE");
//            url = String.format("http://%s:%s/item-api/item"+
//                            "?itemId=%s",
//                    itemAPi1.getHost(),
//                    itemAPi1.getPort(),
//                    itemId);
//            Items items = restTemplate.getForObject(url,Items.class);
            //FIXME ??????feign??????????????????
            String imgUrl = itemService.queryItemMainImgById(itemId);
//            ServiceInstance itemAPi2 = client.choose("FOODIE-ITEM-SERVICE");
//            url = String.format("http://%s:%s/item-api/primaryImage"+
//                            "?itemId=%s",
//                    itemAPi1.getHost(),
//                    itemAPi1.getPort(),
//                    itemId);
//            String imgUrl = restTemplate.getForObject(url,String.class);

            //2.3???????????????????????????????????????
            String subOrderId = sid.nextShort();
            OrderItems subOrderItems = new OrderItems();
            subOrderItems.setId(subOrderId);
            subOrderItems.setOrderId(orderId);
            subOrderItems.setItemId(itemId);
            subOrderItems.setItemName(items.getItemName());
            subOrderItems.setItemImg(imgUrl);
            subOrderItems.setBuyCounts(buyCounts);
            subOrderItems.setItemSpecId(itemSpecId);
            subOrderItems.setItemSpecName(itemsSpec.getName());
            subOrderItems.setPrice(itemsSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItems);

            //2.4????????????????????????????????????????????????????????????
            //FIXME ??????feign??????????????????
            itemService.decreaseItemSpecStock(itemSpecId,buyCounts);
//            ServiceInstance itemAPi3 = client.choose("FOODIE-ITEM-SERVICE");
//            url = String.format("http://%s:%s/item-api/decreaseStock",
//                    itemAPi1.getHost(),
//                    itemAPi1.getPort());
//            restTemplate.postForLocation(url,itemSpecId,buyCounts);
        }
        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAmount);
        ordersMapper.insert(newOrder);
        //3.?????????????????????
        OrderStatus waitPayOrderStatus = new OrderStatus();
        waitPayOrderStatus.setOrderId(orderId);
        waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitPayOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitPayOrderStatus);

        //4.?????????????????????????????????????????????
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAmount+postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        //5.?????????????????????VO
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        orderVO.setToBeRemoveShopcartList(toBeRemoveShopcartList);

        return orderVO;
    }

    /**
     * ???redis??????????????????????????????????????????counts
     * @param shopcartList
     * @param itemSpecId
     * @return
     */
    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartList, String itemSpecId){
        for(ShopcartBO shopcartBO:shopcartList){
            if(shopcartBO.getSpecId().equals(itemSpecId)){
                return shopcartBO;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);

    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId){
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder(){
        //?????????????????????????????????????????????????????????1??????????????????????????????
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list=orderStatusMapper.select(queryOrder);
        for(OrderStatus os:list){
            //????????????????????????
            Date createdTime = os.getCreatedTime();
            //???????????????????????????
            int days = DateUtil.daysBetween(createdTime,new Date());
            if (days>=1){
                //???????????????????????????
                doCloseOrder(os.getOrderId());
            }

        }

    }
    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId){
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }
}
