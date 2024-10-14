package org.zjzWx.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zjzWx.service.UploadService;
import org.zjzWx.util.PicUtil;
import org.zjzWx.util.Response;

import java.io.IOException;
import java.util.Base64;

@Service
public class UploadServiceImpl implements UploadService {


    @Value("${webset.safetyDomain}")
    private String safetyDomain;


    @Override
    public String checkNsfw(MultipartFile multipartFile) {


        try {
            RestTemplate restTemplate = new RestTemplate();

            // 构建 multipart 数据
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file",new PicUtil.MultipartInputStreamFileResource(multipartFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    safetyDomain+"checkImg",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);


            // 解析响应的 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String code = jsonNode.get("code").asText();
            if(code.equals("0")){
                return null;
            }else {
                return "图片色情，制作失败";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "系统繁忙，请稍后再试";
        }
    }





    @Override
    public Response<String> uploadPhoto(MultipartFile file, String originalFilename) {
        try {

            // 直接获取文件内容
            byte[] fileContent = file.getBytes();

            // 进行Base64编码
            String base64Image = Base64.getEncoder().encodeToString(fileContent);

            // 拼接完整的Base64图片URI
            String imagePrefix = "";
            if (originalFilename.toLowerCase().endsWith(".png")) {
                imagePrefix = "data:image/png;base64,";
            } else if (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg")) {
                imagePrefix = "data:image/jpeg;base64,";
            }

            //进行图片鉴黄
            return Response.ok(imagePrefix + base64Image);

        } catch (IOException e) {
            return Response.no("图片识别失败，请重试");
        }
    }




}
