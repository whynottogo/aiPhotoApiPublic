package org.zjzWx.service.impl;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.entity.*;
import org.zjzWx.model.dto.CreatePhotoDto;
import org.zjzWx.model.dto.HivisionDto;
import org.zjzWx.model.vo.PicVo;
import org.zjzWx.service.*;
import org.zjzWx.util.PicUtil;
import org.zjzWx.util.OuterApiRequestUtils;
import org.zjzWx.util.Response;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Slf4j
@SuppressWarnings("DataFlowIssue")
@Service
public class ApiServiceImpl implements ApiService {

    @Value("${webset.zjzDomain}")
    private String zjzDomain;

    @Autowired
    private CustomService customService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private PhotoService photoService;
    @Autowired
    private PhotoRecordService photoRecordService;
    @Resource
    private WebSetService webSetService;
    @Resource
    private UploadService uploadService;

    @Value("${webset.directory}")
    private String directory;

    @Value("${webset.picDomain}")
    private String picDomain;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PicVo createIdPhoto(CreatePhotoDto createPhotoDto) {
        PicVo picVo = new PicVo();
        Item item = null;
        if (createPhotoDto.getType() == 0) { //定制
            Custom custom = customService.getById(createPhotoDto.getItemId());
            if (Objects.isNull(custom)) {
                throw new BizException("500", "规格不存在");
            }
            if (!custom.getUserId().equals(createPhotoDto.getUserId())) {
                throw new BizException("500", "非法请求");
            }
            createPhotoDto.setHeight(custom.getHeightPx());
            createPhotoDto.setWidth(custom.getWidthPx());

        } else { //列表
            item = itemService.getById(createPhotoDto.getItemId());
            if (null == item) {
                throw new BizException("500", "规格信息不存在");
            }
            createPhotoDto.setHeight(item.getHeightPx());
            createPhotoDto.setWidth(item.getWidthPx());
        }

        MultipartFile multipartFile;
        if (Objects.nonNull(createPhotoDto.getFile())) {
            multipartFile = createPhotoDto.getFile();
            checkImage(multipartFile);
        } else {
            multipartFile = PicUtil.base64ToMultipartFile(createPhotoDto.getImage());
        }
        String sourceImagePath = PicUtil.saveImgToServer(multipartFile);

        HivisionDto createIdPhotoResult = OuterApiRequestUtils.createIdPhoto(multipartFile, createPhotoDto.getHeight(), createPhotoDto.getWidth());
        //因为小程序需要默认初始化一张蓝底。新版本HivisionIDPhotos无法通过修改py代码来实现，只能再java再调用一次换背景接口
        //可能会慢点，目前已经列入待优化名单
        HivisionDto updateBackgroundResult = OuterApiRequestUtils.requestAiUpdateImageBackground(createIdPhotoResult.getImageBase64Standard(), "#438edb");


        //保存生成记录
        Photo photo = new Photo();
        photo.setUserId(createPhotoDto.getUserId());
        if (null == item) {
            photo.setName("用户自定义尺寸");
        } else {
            photo.setName(item.getName());
        }
        photo.setOImg(sourceImagePath);
        photo.setSize(createPhotoDto.getWidth() + "x" + createPhotoDto.getHeight());
        photo.setCreateTime(new Date());
        photoService.save(photo);

        //保存用户行为记录
        PhotoRecord record = new PhotoRecord();
        record.setName("生成证件照");
        record.setUserId(createPhotoDto.getUserId());
        record.setCreateTime(new Date());
        photoRecordService.save(record);

        //封装前端参数
        picVo.setId2(photo.getId());
        picVo.setOImg(createIdPhotoResult.getImageBase64Hd());
        picVo.setKImg(createIdPhotoResult.getImageBase64Standard());
        picVo.setCImg(updateBackgroundResult.getImageBase64());
        return picVo;


    }

    @Override
    public PicVo updateImageBackground(CreatePhotoDto createPhotoDto) {
        PicVo picVo = new PicVo();
        HivisionDto hivisionDto = OuterApiRequestUtils.requestAiUpdateImageBackground(createPhotoDto.getImage(), createPhotoDto.getColors());
        if (!hivisionDto.isStatus()) {
            picVo.setMsg("未检测到人脸或多人脸");
            return picVo;
        }

        //保存用户行为记录
        PhotoRecord record = new PhotoRecord();
        record.setName("换背景");
        record.setUserId(createPhotoDto.getUserId());
        record.setCreateTime(new Date());
        photoRecordService.save(record);

        picVo.setCImg(hivisionDto.getImageBase64());
        return picVo;


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PicVo updateUserPhoto(Integer userid, String img, Integer photoId) {
        PicVo picVo = new PicVo();
        //防止被当图床
        Photo photo = photoService.getById(photoId);
        if (null == photo) {
            picVo.setMsg("非法请求");
            return picVo;
        }
        if (!photo.getUserId().equals(userid)) {
            picVo.setMsg("非法请求");
            return picVo;
        }


        //因为图片没刚开始存库，是为了防止性能浪费，所有由前端传入
        // 将图片转成MultipartFile，再次检查，防止数据伪造，如：被劫持数据包上传黄色，木马什么的
        MultipartFile file = PicUtil.base64ToMultipartFile(img);


        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || (!originalFilename.toLowerCase().endsWith(".png")
                && !originalFilename.toLowerCase().endsWith(".jpg")) && !originalFilename.toLowerCase().endsWith(".jpeg")) {
            picVo.setMsg("图片类型不合法，仅支持jpg/png/jpeg的图片");
            return picVo;
        }
        //检查通过，上传服务器，数据库保存url
        //之前试过保存base64，发现数据库加载很慢，性能很低
        // 创建文件存储目录
        String imagePath = PicUtil.saveImgToServer(file);
        photo.setId(photoId);
        photo.setNImg(imagePath);
        photoService.updateById(photo);


        //保存用户行为记录
        PhotoRecord record = new PhotoRecord();
        record.setName("下载证件照");
        record.setUserId(userid);
        record.setCreateTime(new Date());
        photoRecordService.save(record);
        picVo.setPicUrl(imagePath);
        return picVo;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String layoutPhoto(Integer userId, String imgUrl) {
        String localImgPath = directory + "/" + imgUrl.replace(picDomain, "");
        //生成排版照base64
        HivisionDto hivisionDto = OuterApiRequestUtils.requestGenerateLayoutPhotos(localImgPath);
        //base64转file
        MultipartFile multipartFile = PicUtil.base64ToMultipartFile(hivisionDto.getImageBase64());
        //保存图片
        String layoutImagePath = PicUtil.saveImgToServer(multipartFile);
        //保存图片到用户图片列表
        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setName("排版照");
        photo.setOImg(imgUrl);
        photo.setNImg(layoutImagePath);
        photo.setSize(295 + "x" + 413);
        photo.setCreateTime(new Date());
        photoService.save(photo);

        //记录用户行为
        PhotoRecord record = new PhotoRecord();
        record.setName("生成排版照");
        record.setUserId(userId);
        record.setCreateTime(new Date());
        photoRecordService.save(record);
        return layoutImagePath;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String layoutPhotoByFile(Integer userId, MultipartFile file) {
        checkImage(file);
        //保存到当前服务器
        String imagePath = PicUtil.saveImgToServer(file);
        return layoutPhoto(userId, imagePath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String humanMatting(Integer userId, MultipartFile file) {
        checkImage(file);
        //保存到当前服务器
        String imagePath = PicUtil.saveImgToServer(file);
        //获取服务器上图片的绝对路径
        String localImgPath = directory + "/" + imagePath.replace(picDomain, "");
        //生成人像抠图
        HivisionDto hivisionDto = OuterApiRequestUtils.requestHumanMattingPhotos(localImgPath);
        //base64转file
        MultipartFile multipartFile = PicUtil.base64ToMultipartFile(hivisionDto.getImageBase64());
        //保存图片
        String mattingImagePath = PicUtil.saveImgToServer(multipartFile);
        //保存图片到用户图片列表
        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setName("人像抠图");
        photo.setOImg(imagePath);
        photo.setNImg(mattingImagePath);
        photo.setSize("*****");
        photo.setCreateTime(new Date());
        photoService.save(photo);

        //记录用户行为
        PhotoRecord record = new PhotoRecord();
        record.setName("生成人像抠图");
        record.setUserId(userId);
        record.setCreateTime(new Date());
        photoRecordService.save(record);
        return mattingImagePath;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String colorImage(Integer userId, MultipartFile file) {
        checkImage(file);
        //保存到当前服务器
        String imagePath = PicUtil.saveImgToServer(file);
        //获取服务器上图片的绝对路径
        String localImgPath = directory + "/" + imagePath.replace(picDomain, "");
        //生成人像抠图
        HivisionDto hivisionDto = OuterApiRequestUtils.colorImage(localImgPath);
        //base64转file
        MultipartFile multipartFile = PicUtil.base64ToMultipartFile(hivisionDto.getImageBase64());
        //保存图片
        String mattingImagePath = PicUtil.saveImgToServer(multipartFile);
        //保存图片到用户图片列表
        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setName("黑白照片上色");
        photo.setOImg(imagePath);
        photo.setNImg(mattingImagePath);
        photo.setSize("*****");
        photo.setCreateTime(new Date());
        photoService.save(photo);

        //记录用户行为
        PhotoRecord record = new PhotoRecord();
        record.setName("生成黑白照片上色");
        record.setUserId(userId);
        record.setCreateTime(new Date());
        photoRecordService.save(record);
        return mattingImagePath;
    }


    private void checkImage(MultipartFile file) {
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || (!originalFilename.toLowerCase().endsWith(".png")
                && !originalFilename.toLowerCase().endsWith(".jpg")) && !originalFilename.toLowerCase().endsWith(".jpeg")) {
            // 文件类型不合法
            throw new BizException("500", "图片类型不合法，仅支持jpg/png/jpeg的图片");
        }

        // 检查文件大小，因为现在的手机，一拍照就10多M
        if (file.getSize() > 15 * 1024 * 1024) {
            throw new BizException("500", "图片大小不能超过15M");
        }

        WebSet webSet = webSetService.getById(1);
        //如果开启鉴黄
        if(webSet.getSafetyApi() == 2){
            String s = uploadService.checkNsfw(file);
            if(s != null){
                throw new BizException("500", s);
            }
        }
    }
}
