package com.zheng.user.service;


import com.zheng.user.pojo.Users;
import com.zheng.user.pojo.bo.UserBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("foodie-user-service")
@RequestMapping("user-api")
public interface UserService {
   /**
    * 判断用户名是否存在
    * @param username
    * @return
    */
   @GetMapping("user/exists")
   public boolean queryUsernameIsExist(@RequestParam("username") String username);

   /**
    * 创建用户
    * @param userBO
    * @return
    */
   @PostMapping("user")
   public Users createUser(@RequestBody UserBO userBO);

   /**
    * 检索用户名和密码是否匹配，用于登录
    * @param username
    * @param password
    * @return
    */
   @GetMapping("verify")
   public Users queryUserForLogin(@RequestParam("username") String username, @RequestParam("password") String password);
}
