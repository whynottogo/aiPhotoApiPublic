package org.zjzWx.file;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.entity.PhotoRecord;
import org.zjzWx.entity.WebSet;
import org.zjzWx.service.PhotoRecordService;
import org.zjzWx.service.UploadService;
import org.zjzWx.service.WebSetService;
import org.zjzWx.util.Response;

import java.util.Date;

@RestController
public class ImageUpload {

    @Autowired
    private UploadService uploadService;
    @Autowired
    private WebSetService webSetService;
    @Autowired
    private PhotoRecordService photoRecordService;

    //图片检查以及上传图片
    @PostMapping("/upload")
    public Response<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Response.no("图片不能为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || (!originalFilename.toLowerCase().endsWith(".png")
                && !originalFilename.toLowerCase().endsWith(".jpg")) && !originalFilename.toLowerCase().endsWith(".jpeg")) {
            // 文件类型不合法
            return Response.no("图片类型不合法，仅支持jpg/png/jpeg的图片");
        }

        // 检查文件大小，因为现在的手机，一拍照就10多M
        if (file.getSize() > 15 * 1024 * 1024) {
            return Response.no("图片大小不能超过15M");
        }

        WebSet webSet = webSetService.getById(1);
        //如果开启鉴黄
        if(webSet.getSafetyApi() == 2){
            String s = uploadService.checkNsfw(file);
            if(s != null){
                return Response.no(s);
            }
        }

        PhotoRecord photoRecord = new PhotoRecord();
        photoRecord.setName("上传图片");
        photoRecord.setUserId(Integer.parseInt(StpUtil.getTokenInfo().getLoginId().toString()));
        photoRecord.setCreateTime(new Date());
        photoRecordService.save(photoRecord);


        return uploadService.uploadPhoto(file,originalFilename);
    }


}
