package com.zheng.order.controller.center;

import com.zheng.controller.BaseController;
import com.zheng.enums.YesOrNo;
import com.zheng.item.service.ItemCommentsService;
import com.zheng.order.pojo.OrderItems;
import com.zheng.order.pojo.Orders;
import com.zheng.order.pojo.bo.center.OrderItemsCommentBO;
import com.zheng.order.service.center.MyCommentsService;
import com.zheng.order.service.center.MyOrdersService;
import com.zheng.pojo.JsonResult;
import com.zheng.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Api(value = "用户中心评价模块",tags = {"用户中心评价模块相关接口"})
@RestController
@RequestMapping("mycomments")
public class MyCommentsController extends BaseController {
    @Autowired
    private MyCommentsService myCommentsService;

    @Autowired
    private ItemCommentsService itemCommentsService;
    @Autowired
    private MyOrdersService myOrdersService;


    @ApiOperation(value = "查询订单列表",notes = "查询订单列表", httpMethod = "POST")
    @PostMapping("/pending")
    public JsonResult pending(
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId",value = "订单id",required = true)
            @RequestParam String orderId)
            {
        //判断用户和订单是否关联
        JsonResult checkResult = myOrdersService.checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!= HttpStatus.OK.value()){
                return checkResult;
        }
        //判断该笔订单是否已经评价过，评价过滤就不再继续
        Orders myOrders = (Orders)checkResult.getData();
        if (myOrders.getIsComment()== YesOrNo.YES.type){
            return JsonResult.errorMsg("该笔订单已经评价");
        }

        List<OrderItems> list = myCommentsService.queryPendingComment(orderId);
        return JsonResult.ok(list);
    }

    @ApiOperation(value = "保存评论列表",notes = "保存评论列表", httpMethod = "POST")
    @PostMapping("/saveList")
    public JsonResult saveList(
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId",value = "订单id",required = true)
            @RequestParam String orderId,
            @RequestBody List<OrderItemsCommentBO> commentList)
    {
        System.out.println(commentList);
        //判断用户和订单是否关联
        JsonResult checkResult = myOrdersService.checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!= HttpStatus.OK.value()){
            return checkResult;
        }
        //判断评论内容list不能为空
        if(commentList==null||commentList.isEmpty()||commentList.size()==0){
            return JsonResult.errorMsg("评论内容不能为空");
        }
        myCommentsService.saveComments(orderId,userId,commentList);
        return JsonResult.ok();
    }

    @ApiOperation(value = "查询我的评价",notes = "查询我的评价", httpMethod = "POST")
    @PostMapping("/query")
    public JsonResult query(
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "page",value = "查询下一页的第几页",required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "分页的每一页显示的条数",required = false)
            @RequestParam Integer pageSize){
        if(StringUtils.isBlank(userId)){
            return JsonResult.errorMsg(null);
        }
        if(page==null){
            page = 1;
        }
        if(pageSize==null){
            pageSize=COMMON_PAGE_SIZE;
        }
        PagedGridResult grid = itemCommentsService.queryMyComments(userId,page,pageSize);
        //TODO 学完Feign再来改造
//        ServiceInstance instance = client.choose("FOODIE-ITEM-SERVICE");
//        String url = String.format("http://%s:%s/item-comments-api/myComments"+
//                "?userId=%s&page=%s&pageSize=%s",
//                instance.getHost(),
//                instance.getPort(),
//                userId,page,pageSize);
//        PagedGridResult grid=restTemplate.getForObject(url,PagedGridResult.class);
        return JsonResult.ok(grid);
    }
}
