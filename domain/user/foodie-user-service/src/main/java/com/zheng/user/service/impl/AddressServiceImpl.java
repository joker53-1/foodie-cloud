package com.zheng.user.service.impl;

import com.zheng.enums.YesOrNo;
import com.zheng.user.mapper.UserAddressMapper;
import com.zheng.user.pojo.UserAddress;
import com.zheng.user.pojo.bo.AddressBO;
import com.zheng.user.service.AddresslService;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
public class AddressServiceImpl implements AddresslService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<UserAddress> queryAll(String userId) {

        UserAddress ua = new UserAddress();
        ua.setUserId(userId);
        return userAddressMapper.select(ua);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addNewUserAddress(AddressBO addressBO) {
        //1.判断当前用户是否存在地址，如果没有，则新增为‘默认地址’
        Integer isDefault = 0;
        List<UserAddress> addressList = this.queryAll(addressBO.getUserId());
        if(addressList==null||addressList.isEmpty()||addressList.size()==0){
            isDefault = 1;
        }

        String addressId = sid.nextShort();
        //2.保存地址到数据库
        UserAddress newAdress = new UserAddress();
        BeanUtils.copyProperties(addressBO,newAdress);
        newAdress.setId(addressId);
        newAdress.setIsDefault(isDefault);
        newAdress.setCreatedTime(new Date());
        newAdress.setUpdatedTime(new Date());

        userAddressMapper.insert(newAdress);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserAddress(AddressBO addressBO) {
        String addressId = addressBO.getAddressId();
        UserAddress pendingAdress = new UserAddress();
        BeanUtils.copyProperties(addressBO,pendingAdress);

        pendingAdress.setId(addressId);
        pendingAdress.setUpdatedTime(new Date());

        userAddressMapper.updateByPrimaryKeySelective(pendingAdress);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserAddress(String userId, String addressId) {

        UserAddress adress = new UserAddress();
        adress.setId(addressId);
        adress.setUserId(userId);

        userAddressMapper.delete(adress);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserAddressToBeDefault(String userId, String addressId) {

        //1.查找默认地址，设置为不默认
        UserAddress queryAddress = new UserAddress();
        queryAddress.setUserId(userId);
        queryAddress.setIsDefault(YesOrNo.YES.type);
        List<UserAddress> list = userAddressMapper.select(queryAddress);
        for(UserAddress ua:list){
            ua.setIsDefault(YesOrNo.NO.type);
            userAddressMapper.updateByPrimaryKeySelective(ua);
        }
        //2.根据地址id修改为默认的地址
        UserAddress defaultAddress = new UserAddress();
        defaultAddress.setId(addressId);
        defaultAddress.setUserId(userId);
        defaultAddress.setIsDefault(YesOrNo.YES.type);
        userAddressMapper.updateByPrimaryKeySelective(defaultAddress);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public UserAddress queryUserAddress(String userId, String addressId) {

        UserAddress singleAddress = new UserAddress();
        singleAddress.setId(addressId);
        singleAddress.setUserId(userId);

        return userAddressMapper.selectOne(singleAddress);
    }
}
