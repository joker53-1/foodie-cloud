package com.zheng.user.service.center;


import com.zheng.user.pojo.Users;
import com.zheng.user.pojo.bo.center.CenterUserBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

@FeignClient("foodie-user-service")
@RequestMapping("cneter-user-api")
public interface CenterUserService {
   /**
    * 根据用户id查询用户信息
    * @param userId
    * @return
    */
   @GetMapping("profile")
   public Users queryUserInfo(@RequestParam("userId") String userId);

   /**
    * 修改用户信息
    * @param userId
    * @param centerUserBO
    */
   @PutMapping("profile/{userId}")
   public Users updateUserInfo(@PathParam("userId") String userId, @RequestBody CenterUserBO centerUserBO);

   /**
    * 用户头像更新
    * @param userId
    * @param faceUrl
    * @return
    */
   @PostMapping("updatePhoto")
   public Users updateUserFace(@RequestParam("userId") String userId, @RequestParam("faceUrl") String faceUrl);
}
