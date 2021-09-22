package com.zheng.order.controller.center;

import com.zheng.controller.BaseController;
import com.zheng.order.pojo.vo.OrderStatusCountsVO;
import com.zheng.order.service.center.MyOrdersService;
import com.zheng.pojo.JsonResult;
import com.zheng.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Api(value = "用户中心我的订单",tags = {"用户中心我的订单相关接口"})
@RestController
@RequestMapping("myorders")
public class MyOrdersController extends BaseController {


    @Autowired
    private MyOrdersService myOrdersService;

    @ApiOperation(value = "查询订单列表",notes = "查询订单列表", httpMethod = "POST")
    @PostMapping("/query")
    public JsonResult query(
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderStatus",value = "订单状态",required = false)
            @RequestParam Integer orderStatus,
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
        PagedGridResult grid = myOrdersService.queryMyOrders(userId,orderStatus,page,pageSize);
        return JsonResult.ok(grid);
    }

    // 商家发货没有后端，所以这个接口仅仅只是用于模拟
    @ApiOperation(value="商家发货", notes="商家发货", httpMethod = "GET")
    @GetMapping("/deliver")
    public JsonResult deliver(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId) throws Exception {

        if (StringUtils.isBlank(orderId)) {
            return JsonResult.errorMsg("订单ID不能为空");
        }
        myOrdersService.updateDeliverOrderStatus(orderId);
        return JsonResult.ok();
    }

    @ApiOperation(value="用户确认收货", notes="用户确认收货", httpMethod = "POST")
    @PostMapping("/confirmReceive")
    public JsonResult confirmReceive(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId) throws Exception {

        JsonResult checkResult = myOrdersService.checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!= HttpStatus.OK.value()){
            return checkResult;
        }
        boolean res = myOrdersService.updateReceiveOrderStatus(orderId);
        if(!res){
            return JsonResult.errorMsg("订单确认收货失败！");
        }
        return JsonResult.ok();
    }

    @ApiOperation(value="用户删除收货", notes="用户删除收货", httpMethod = "POST")
    @PostMapping("/delete")
    public JsonResult delete(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId) throws Exception {

        JsonResult checkResult = myOrdersService.checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!= HttpStatus.OK.value()){
            return checkResult;
        }
        boolean res = myOrdersService.deleteOrder(userId,orderId);
        if(!res){
            return JsonResult.errorMsg("订单删除失败！");
        }
        return JsonResult.ok();
    }

    @ApiOperation(value="获得订单状态数概况", notes="获得订单状态数概况", httpMethod = "POST")
    @PostMapping("/statusCounts")
    public JsonResult statusCounts(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId) throws Exception {

        if(StringUtils.isBlank(userId)){
            return JsonResult.errorMsg(null);
        }
        OrderStatusCountsVO orderStatusCountsVO = myOrdersService.getOrderStatusCounts(userId);
        return JsonResult.ok(orderStatusCountsVO);
    }


    @ApiOperation(value = "查询订单动向",notes = "查询订单动向", httpMethod = "POST")
    @PostMapping("/trend")
    public JsonResult trend(
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
        PagedGridResult grid = myOrdersService.getOrdersTrend(userId,page,pageSize);
        return JsonResult.ok(grid);
    }
//    /**
//     * 用于验证用户和订单是否有关联关系，避免非法用户调用
//     * @param userId
//     * @param orderId
//     * @return
//     */
//    private JsonResult checkUserOrder(String userId,String orderId){
//        Orders orders = myOrdersService.queryMyOrder(userId,orderId);
//        if(orders==null){
//            return JsonResult.errorMsg("订单不存在");
//        }
//        return JsonResult.ok();
//    }

}