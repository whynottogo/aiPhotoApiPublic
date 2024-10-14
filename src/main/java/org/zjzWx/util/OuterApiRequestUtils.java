package org.zjzWx.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.model.dto.HivisionDto;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shynin
 * @version 1.0
 * @date 2024/10/9 09:18
 */
@Component
public class OuterApiRequestUtils {

    private static String zjzDomain;
    private static String colorDomain;

    @Value("${webset.colorDomain}")
    public void setColorDomain(String colorDomain) {
        OuterApiRequestUtils.colorDomain = colorDomain;
    }
    @Value("${webset.zjzDomain}")
    public void setZjzDomain(String zjzDomain) {
        OuterApiRequestUtils.zjzDomain = zjzDomain;
    }

    /**
     * 给抠图后的图像，添加背景色
     * @param img 图像的base64信息
     * @param colors 颜色信息，如：#438edb
     * @return
     */
    @SneakyThrows
    public static HivisionDto requestAiUpdateImageBackground(String img, String colors) {
        RestTemplate restTemplate = new RestTemplate();

        // 构建 multipart 数据
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        MultipartFile multipartFile = PicUtil.base64ToMultipartFile(img);
        body.add("input_image", new PicUtil.MultipartInputStreamFileResource(multipartFile));
        body.add("color", colors);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                zjzDomain + "/add_background",
                HttpMethod.POST,
                requestEntity,
                String.class);

        return JSON.parseObject(response.getBody(), HivisionDto.class);
    }


    /**
     * 生成人像抠图，并且裁剪为证件照
     * @param file 原始图片文件
     * @param height 抠图后的图片高度
     * @param width 抠图后的图片长度
     * @return 返回结果
     */
    @SneakyThrows
    public static HivisionDto createIdPhoto(MultipartFile file, Integer height, Integer width) {
        RestTemplate restTemplate = new RestTemplate();

        // 构建 multipart 数据
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("input_image", new PicUtil.MultipartInputStreamFileResource(file));
        body.add("height", height);
        body.add("width", width);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        //请求divisionAPI，生成证件照(底透明)
        ResponseEntity<String> response = restTemplate.exchange(
                zjzDomain + "/idphoto",
                HttpMethod.POST,
                requestEntity,
                String.class);
        //返回请求结果
        HivisionDto hivisionDto = JSON.parseObject(response.getBody(), HivisionDto.class);
        return hivisionDto;
    }

    /**
     * 生成六寸排版照  /generate_layout_photos 接口
     * @param inputImageDir 文件地址
     * @return 状态以及图片的base64
     */
    @SneakyThrows
    public static HivisionDto requestGenerateLayoutPhotos(String inputImageDir) {
        String url = zjzDomain + "generate_layout_photos";
        // 创建文件对象
        File inputFile = new File(inputImageDir);
        Map<String, Object> paramMap=new HashMap<>();
        paramMap.put("input_image", inputFile);
        paramMap.put("height","413");
        paramMap.put("width","295");
        paramMap.put("kb","500");
        //response为一个json格式字典，包含status和image_base64

        HivisionDto hivisionDto = JSON.parseObject(HttpUtil.post(url, paramMap), HivisionDto.class);
        return hivisionDto;
    }

    /**
     * 生成人像抠图照  /human_matting 接口
     * @param inputImageDir 文件地址
     * @return
     */
    @SneakyThrows
    public static HivisionDto requestHumanMattingPhotos(String inputImageDir) {
        String url = zjzDomain + "human_matting";
        // 创建文件对象
        File inputFile = new File(inputImageDir);
        Map<String, Object> paramMap=new HashMap<>();
        paramMap.put("input_image",inputFile);
        //包含status、image_base64
        HivisionDto hivisionDto = JSON.parseObject(HttpUtil.post(url, paramMap), HivisionDto.class);
        return hivisionDto;
    }

    @SneakyThrows
    public static HivisionDto colorImage(String inputImageDir) {
        String url = colorDomain + "uploadImage";
        // 创建文件对象
        File inputFile = new File(inputImageDir);
        Map<String, Object> paramMap=new HashMap<>();
        paramMap.put("filename", inputFile);
        //包含status、image_base64
        HivisionDto hivisionDto = JSON.parseObject(HttpUtil.post(url, paramMap), HivisionDto.class);
        return hivisionDto;
    }
}
