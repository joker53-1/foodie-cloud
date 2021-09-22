package com.zheng.user.controller;

import com.zheng.pojo.JsonResult;
import com.zheng.user.pojo.UserAddress;
import com.zheng.user.pojo.bo.AddressBO;
import com.zheng.user.service.AddresslService;
import com.zheng.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//@Controller
@Api(value = "地址相关", tags = {"地址相关的api接口"})
@RequestMapping("address")
@RestController
public class AddressController {

    /*
    * 用户在确认订单页面，可以针对收货地址做如下操作：
    * 1.查新用户的所有收货地址列表
    * 2.新增收货地址
    * 3.删除收货地址
    * 4.修改收货地址
    * 5.设置默认地址
    * */

    @Autowired
    private AddresslService addresslService;

    @ApiOperation(value = "根据用户id查询收货地址列表",notes = "根据用户id查询收货地址列表", httpMethod = "POST")
    @PostMapping("/list")
    public JsonResult list(
            @RequestParam String userId){

        if(StringUtils.isBlank(userId)){
            return JsonResult.errorMsg("");
        }

        List<UserAddress> list =addresslService.queryAll(userId);
        return JsonResult.ok(list);
    }

    @ApiOperation(value = "用户新增地址",notes = "用户新增地址", httpMethod = "POST")
    @PostMapping("/add")
    public JsonResult add(
            @RequestBody AddressBO addressBO){

        JsonResult checkRes = checkAddress(addressBO);
        if(checkRes.getStatus()!=200){
            return checkRes;
        }
        addresslService.addNewUserAddress(addressBO);
        return JsonResult.ok();
    }
    private JsonResult checkAddress(AddressBO addressBO){
        String receiver =addressBO.getReceiver();
        if(StringUtils.isBlank(receiver)){
            return JsonResult.errorMsg("收货人不能为空");
        }
        if(receiver.length()>12){
            return JsonResult.errorMsg("收货人姓名不能太长");
        }
        String mobile = addressBO.getMobile();
        if(StringUtils.isBlank(mobile)){
            return JsonResult.errorMsg("收货人手机不能为空");
        }
        if(mobile.length()!=11){
            return JsonResult.errorMsg("收货人手机号长度不正确");
        }
        boolean isMobileOk = MobileEmailUtils.checkMobileIsOk(mobile);
        if(!isMobileOk){
            return JsonResult.errorMsg("收货人手机号格式不正确");
        }
        String province = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();
        if(StringUtils.isBlank(province)||
                StringUtils.isBlank(city)||
                StringUtils.isBlank(district)||
                StringUtils.isBlank(detail)){
            return JsonResult.errorMsg("收货地址信息不能为空");
        }
        return JsonResult.ok();
    }
    @ApiOperation(value = "用户修改地址",notes = "用户修改地址", httpMethod = "POST")
    @PostMapping("/update")
    public JsonResult update(
            @RequestBody AddressBO addressBO){

        if(StringUtils.isBlank(addressBO.getAddressId())){
            return JsonResult.errorMsg("修改地址错误：addressId不能为空");
        }
        JsonResult checkRes = checkAddress(addressBO);
        if(checkRes.getStatus()!=200){
            return checkRes;
        }
        addresslService.updateUserAddress(addressBO);
        return JsonResult.ok();
    }

    @ApiOperation(value = "用户删除地址",notes = "用户删除地址", httpMethod = "POST")
    @PostMapping("/delete")
    public JsonResult delete(
            @RequestParam String userId,
            @RequestParam String addressId){

        if(StringUtils.isBlank(userId)||StringUtils.isBlank(addressId)){
            return JsonResult.errorMsg("");
        }

        addresslService.deleteUserAddress(userId,addressId);
        return JsonResult.ok();
    }
    @ApiOperation(value = "用户设置默认地址",notes = "用户设置默认地址", httpMethod = "POST")
    @PostMapping("/setDefalut")
    public JsonResult setDefalut(
            @RequestParam String userId,
            @RequestParam String addressId){

        if(StringUtils.isBlank(userId)||StringUtils.isBlank(addressId)){
            return JsonResult.errorMsg("");
        }

        addresslService.updateUserAddressToBeDefault(userId,addressId);
        return JsonResult.ok();
    }
}
