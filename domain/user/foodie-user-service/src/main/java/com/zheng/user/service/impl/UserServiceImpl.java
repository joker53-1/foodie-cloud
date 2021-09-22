package com.zheng.user.service.impl;

import com.zheng.enums.Sex;
import com.zheng.user.mapper.UsersMapper;
import com.zheng.user.pojo.Users;
import com.zheng.user.pojo.bo.UserBO;
import com.zheng.user.service.UserService;
import com.zheng.utils.DateUtil;
import com.zheng.utils.Md5Util;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@RestController
public class UserServiceImpl implements UserService {
    @Autowired
    public UsersMapper usersMapper;
    @Autowired
    public Sid sid;

    private static final String USER_FACE="https://s2.ax1x.com/2019/11/13/MYYti9.png";
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {

        Example userExample = new Example(Users.class);
        Example.Criteria userCriteria = userExample.createCriteria();

        userCriteria.andEqualTo("username", username);

        Users result = usersMapper.selectOneByExample(userExample);

        return result==null?false:true;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users createUser(UserBO userBO) {
        String userId = sid.nextShort();
        Users users = new Users();
        users.setId(userId);
        users.setUsername(userBO.getUsername());
        try {
            users.setPassword(Md5Util.getMD5Str(userBO.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //默认用户昵称同用户名
        users.setNickname(userBO.getUsername());
        //默认头像
        users.setFace(USER_FACE);
        //默认生日
        users.setBirthday(DateUtil.stringToDate("1900-01-01"));
        //默认性别为保密
        users.setSex(Sex.secret.type);
        users.setCreateTime(new Date());
        users.setUpdateTime(new Date());

        usersMapper.insert(users);
        return users;
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {

        Example userExample = new Example(Users.class);
        Example.Criteria userCriteria = userExample.createCriteria();

        userCriteria.andEqualTo("username", username);
        userCriteria.andEqualTo("password", password);

        Users result = usersMapper.selectOneByExample(userExample);
        return result;
    }
}
