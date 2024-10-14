package org.zjzWx.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.dao.WebSetDao;
import org.zjzWx.entity.WebSet;
import org.zjzWx.model.dto.CreatePhotoDto;
import org.zjzWx.model.dto.LayoutPhotoRequest;
import org.zjzWx.model.vo.PicVo;
import org.zjzWx.service.ApiService;
import org.zjzWx.service.PhotoService;
import org.zjzWx.util.Response;

@RestController
@RequestMapping("/api")
public class ApiController {


    @Autowired
    private ApiService apiService;
    @Autowired
    private WebSetDao webSetDao;
    @Autowired
    private PhotoService photoService;


    //获取全局配置信息
    @PostMapping("/getWeb")
    public WebSet getWeb(){
        return webSetDao.getWeb();
    }


    //上传图片保存且添加默认蓝色背景色
    @PostMapping("/createIdPhoto")
    public Response createIdPhoto(@RequestBody CreatePhotoDto createPhotoDto) {
        createPhotoDto.setUserId(Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString()));
        PicVo idPhoto = apiService.createIdPhoto(createPhotoDto);
        if(null!=idPhoto.getMsg()){
            return Response.no(idPhoto.getMsg());
        }
        return Response.ok(idPhoto);
    }

    //修改图片背景色
    @PostMapping("/updateIdPhoto")
    public Response updateIdPhoto(@RequestBody CreatePhotoDto createPhotoDto) {
        createPhotoDto.setUserId(Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString()));
        PicVo idPhoto = apiService.updateImageBackground(createPhotoDto);
        if(null!=idPhoto.getMsg()){
            return Response.no(idPhoto.getMsg());
        }
        return Response.ok(idPhoto);
    }



    //保存用户图片
    @PostMapping("/updateUserPhoto")
    public Response updateUserPhoto(@RequestBody CreatePhotoDto createPhotoDto){
        Integer userId = Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString());;
        PicVo picVo = apiService.updateUserPhoto(userId, createPhotoDto.getImage(), createPhotoDto.getPhotoId());
        if(null!=picVo.getMsg()){
            return Response.no(picVo.getMsg());
        }
        return Response.ok(picVo);
    }

    @PostMapping("/layoutPhotoByFile")
    public Response<String> layoutPhotoByFile(MultipartFile file) {
        Integer userId = Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString());
        String picUrl = apiService.layoutPhotoByFile(userId, file);
        return Response.ok(picUrl);
    }

    //根据url生成排版照
    @PostMapping("/layoutPhoto")
    public Response<String> layoutPhoto(@RequestBody LayoutPhotoRequest layoutPhotoRequest) {
        Integer userId = Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString());
        String picUrl = apiService.layoutPhoto(userId, layoutPhotoRequest.getImgUrl());
        return Response.ok(picUrl);
    }

    //生成人像抠图
    @PostMapping("/humanMatting")
    public Response<String> humanMatting(MultipartFile file) {
        Integer userId = Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString());
        String picUrl = apiService.humanMatting(userId, file);
        return Response.ok(picUrl);
    }

    //黑白照片生成彩色
    @PostMapping("/colorImage")
    public Response<String> colorImage(MultipartFile file) {
        Integer userId = Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString());
        String picUrl = apiService.colorImage(userId, file);
        return Response.ok(picUrl);
    }

}
