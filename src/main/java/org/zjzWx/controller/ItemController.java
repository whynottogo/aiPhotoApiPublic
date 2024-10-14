package org.zjzWx.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.web.bind.annotation.*;
import org.zjzWx.entity.Custom;
import org.zjzWx.entity.Photo;
import org.zjzWx.service.CustomService;
import org.zjzWx.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zjzWx.service.PhotoService;
import org.zjzWx.util.Response;

import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/item")
public class ItemController {


    @Autowired
    private ItemService itemService;
    @Autowired
    private CustomService customService;
    @Autowired
    private PhotoService photoService;


    //保存用户自定义
    @PostMapping("/saveCustom")
    public Response saveCustom(@RequestBody Custom custom){
        custom.setUserId(Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString()));
        //拿mm尺寸zx
        String[] split = custom.getSize().split("\\*");
        String widthStr = split[0].trim();
        String heightStr = split[1].replace(" mm", "").trim();
        Random random = new Random();
        custom.setIcon(random.nextInt(6) + 1);
        custom.setWidthMm(Integer.parseInt(widthStr));
        custom.setHeightMm(Integer.parseInt(heightStr));
        custom.setCreateTime(new Date());
        customService.save(custom);
        return Response.ok(null);
    }

    //证件列表
    @GetMapping("/itemList")
   public Response itemList(int pageNum, int pageSize, int type, String name){
        String userId = "0";
        if(type==4){
            userId = StpUtil.getTokenInfo().getLoginId().toString();
        }
        List<Object> list = itemService.itemList(pageNum, pageSize, type,userId,name);
        if(list==null || list.size()==0){
            return Response.no();
        }
        return Response.ok(list);
    }


    //用户作品列表
    @GetMapping("/photoList")
    public Response photoList(int pageNum, int pageSize){
        List<Photo> photos = photoService.photoList(pageNum, pageSize, StpUtil.getTokenInfo().getLoginId().toString());
        if(photos==null || photos.size()==0){
            return Response.no();
        }
        return Response.ok(photos);
    }

    //删除作品
    @GetMapping("/deletePhotoId")
    public Response deletePhotoId(int id){
        UpdateWrapper<Photo> qw = new UpdateWrapper<>();
        qw.eq("id",id);
        qw.eq("user_id",StpUtil.getTokenInfo().getLoginId());
        qw.set("deleted", 1);
        photoService.update(qw);
        return Response.ok(null);
    }

}
