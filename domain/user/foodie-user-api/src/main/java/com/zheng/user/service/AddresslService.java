package com.zheng.user.service;

import com.zheng.user.pojo.UserAddress;
import com.zheng.user.pojo.bo.AddressBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("foodie-user-service")
@RequestMapping("address-api")
public interface AddresslService {

    /**
     * 根据用户的用户id查询地址列表
     * @param userId
     * @return
     */
    @GetMapping("addressList")
    public List<UserAddress> queryAll(@RequestParam("userId") String userId);

    /**
     * 用户新增地址
     * @param addressBO
     */
    @PostMapping("address")
    public void addNewUserAddress(@RequestBody AddressBO addressBO);

    /**
     * 用户修改地址
     * @param addressBO
     */
    @PutMapping("address")
    public void updateUserAddress(@RequestBody AddressBO addressBO);

    /**
     * 根据用户id和地址id删除用户对应的地址信息
     * @param userId
     * @param addressId
     */
    @DeleteMapping("address")
    public void deleteUserAddress(@RequestParam("userId")String userId,@RequestParam("addressId") String addressId);

    /**
     * 修改默认地址
     * @param userId
     * @param addressId
     */
    @PostMapping("setDefaultAddress")
    public void updateUserAddressToBeDefault(@RequestParam("userId")String userId,@RequestParam("addressId") String addressId);

    /**
     * 根据用户id和地址id，查询具体的用户地址对象信息
     * @param userId
     * @param addressId
     * @return
     */
    @GetMapping("queryAddress")
    public UserAddress queryUserAddress(@RequestParam("userId")String userId,@RequestParam(value = "addressId",required = false) String addressId);
}
