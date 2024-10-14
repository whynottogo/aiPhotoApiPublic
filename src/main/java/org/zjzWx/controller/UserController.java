package org.zjzWx.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.entity.User;
import org.zjzWx.model.vo.WxLoginVo;
import org.zjzWx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zjzWx.util.Response;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    //登录
    @GetMapping("/login")
    public Response<WxLoginVo> login(String code){
        if(code==null){
            return Response.no(null);
        }
       return Response.ok(userService.wxlogin(code));
    }

    @GetMapping("/logout")
    public Response<Void> unLogin() {
        Integer loginId = (Integer)StpUtil.getTokenInfo().getLoginId();
        StpUtil.logout(loginId);
        return Response.ok(null);
    }

    //获取用户信息
    @GetMapping("/userInfo")
    public Response<User> userInfo(){
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.select("nickname","avatar_url","create_time");
        qw.eq("id", StpUtil.getTokenInfo().getLoginId());
        User user = userService.getOne(qw);
        if(null==user){
            return Response.no();
        }
        return Response.ok(user);
    }

    //保存用户信息
    @PostMapping("/updateUserInfo")
    public Response<String> updateUserInfo(@RequestParam(name = "file", required = false) MultipartFile file,
                                   @RequestParam(name = "nickname", required = false) String nickname){
        if(null==file){
            if(null!=nickname && nickname.length()>20){
                return Response.no("名字太长啦~");
            }
        }
        String msg = userService.updateUserInfo(file,nickname,Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString()));
        if(null!=msg){
            return Response.no(msg);
        }
        return Response.ok(null);
    }











}
