package org.zjzWx.service;

import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.util.Response;

public interface UploadService {

    //图片鉴黄
    String checkNsfw(MultipartFile multipartFile);

    //将流转成base64
    Response uploadPhoto(MultipartFile file, String originalFilename);

}
