package com.zheng.user.controller.center;

import com.zheng.controller.BaseController;
import com.zheng.pojo.JsonResult;
import com.zheng.user.pojo.Users;
import com.zheng.user.pojo.bo.center.CenterUserBO;
import com.zheng.user.pojo.vo.UsersVO;
import com.zheng.user.resource.FileUpload;
import com.zheng.user.service.center.CenterUserService;
import com.zheng.utils.CookieUtils;
import com.zheng.utils.DateUtil;
import com.zheng.utils.JsonUtils;
import com.zheng.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Api(value = "用户信息接口",tags = {"用户信息的相关接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {
    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "用户头像修改",notes = "用户头像修改",httpMethod = "POST")
    @PostMapping("uploadFace")
    public JsonResult uploadFace(
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "file",value = "用户头像",required = true)
            MultipartFile file,
            HttpServletRequest request,
            HttpServletResponse response){

        // 定义头像保存的地址
//        String fileSpace = IMAGE_USER_FACE_LOCATION;
        String fileSpace = fileUpload.getImageUserFaceLocation();
        //在路径下为每一个用户增加一个userId，用于区分不同用户上传
        String uploadPathPrefix = File.separator+userId;

        //开始文件上传
        if(file!=null){
            FileOutputStream fileOutputStream =null;
            try {
                //获得文件山那个床的文件名称
                String fileName = file.getOriginalFilename();
                if(StringUtils.isNotBlank(fileName)){
                    //文件重命名
                    String fileNameArr[] =fileName.split("\\.");

                    //获取文件的后缀名
                    String suffix = fileNameArr[fileNameArr.length-1];

                    if(suffix.equalsIgnoreCase("png")&&suffix.equalsIgnoreCase("jpg")&&suffix.equalsIgnoreCase("jpeg")){
                        return JsonResult.errorMsg("图片格式不正确!");
                    }
                    //face-{useid}.png
                    //文件名称重组  覆盖式上传，增量式：额外拼接当前时间
                    String newFileName = "face-"+userId+"."+suffix;

                    //上传的头像最终保存的位置
                    String finalFacePath = fileSpace+uploadPathPrefix+ File.separator+newFileName;

                    //用于提供给web服务访问的地址
                    uploadPathPrefix+=("/"+newFileName);
                    File outFile = new File(finalFacePath);
                    if(outFile.getParentFile()!=null){
                        //创建文件夹
                        outFile.getParentFile().mkdirs();
                    }

                    //文件输出保存到目录
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream,fileOutputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(fileOutputStream!=null){
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else
            return JsonResult.errorMsg("文件不能为空!");

        String imageServerUrl = fileUpload.getImageServerUrl();

        //由于浏览器可能存在缓存的情况，所以在这里，我们需要加上时间戳来保证更新后的图片可以及时刷新
        String finalUserFaceUrl = imageServerUrl+uploadPathPrefix
                +"?t="+ DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);
        //更新用户头像到数据库
        Users userResult = centerUserService.updateUserFace(userId,finalUserFaceUrl);
//        userResult = setNullProperty(userResult);

        //增加令牌token，会整合进redis，分布式会话
        UsersVO usersVO = conventUsersVO(userResult);
        CookieUtils.setCookie(request,response,"user",
                JsonUtils.objectToJson(usersVO),true);
        return JsonResult.ok();
    }


    @ApiOperation(value = "修改用户信息",notes = "修改用户信息",httpMethod = "POST")
    @PostMapping("update")
    public JsonResult userInfo(
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId,
            @RequestBody CenterUserBO centerUserBO ,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response){
        // 判断BindingResult是否保存错误的验证信息，如果有，则直接return
        if (result.hasErrors()) {
            Map<String, String> errorMap = getErrors(result);
            return JsonResult.errorMap(errorMap);
        }

        Users userResult = centerUserService.updateUserInfo(userId,centerUserBO);

        //增加令牌token，会整合进redis，分布式会话
        UsersVO usersVO = conventUsersVO(userResult);

        CookieUtils.setCookie(request,response,"user",
                JsonUtils.objectToJson(usersVO),true);


        return JsonResult.ok();
    }

    private Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError error : errorList) {
            // 发生验证错误所对应的某一个属性
            String errorField = error.getField();
            // 验证错误的信息
            String errorMsg = error.getDefaultMessage();

            map.put(errorField, errorMsg);
        }
        return map;
    }
    private Users setNullProperty(Users userResult){
        userResult.setPassword(null);
//        userResult.setMobile(null);
//        userResult.setEmail(null);
        userResult.setCreateTime(null);
        userResult.setUpdateTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    public UsersVO conventUsersVO(Users userResult){
        //实现用户的redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USER_TOKEN+":"+userResult.getId(),
                uniqueToken);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);

        return usersVO;
    }
}
