package org.zjzWx.service;

import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.model.dto.CreatePhotoDto;
import org.zjzWx.model.vo.PicVo;

public interface ApiService {


 //生成证件照，初始化，返回原图（用于下载高清），蓝图（用于初始化页面），透明图（用于切换颜色）
 PicVo createIdPhoto(CreatePhotoDto createPhotoDto);

 //换背景色
 PicVo updateImageBackground(CreatePhotoDto createPhotoDto);


 //更新用户保存记录
 PicVo updateUserPhoto(Integer userid, String img, Integer photoId);


 /**
  * 生成排版照
  * @param userId 用户id
  * @param imgUrl 图片url
  * @return 排版照地址
  */
 String layoutPhoto(Integer userId, String imgUrl);

 /**
  * 生成人像抠图
  * @param userId 用户id
  * @param file 图片img
  * @return 人像抠图地址
  */
 String humanMatting(Integer userId, MultipartFile file);

 /**
  * 黑白照片上色
  * @param userId 用户id
  * @param file 图片文件
  * @return 上色后的图片
  */
 String colorImage(Integer userId, MultipartFile file);

 /**
  * 根据文件生成排版照
  * @param userId 用户id
  * @param file 图片文件
  * @return 排版照地址
  */
 String layoutPhotoByFile(Integer userId, MultipartFile file);
}
