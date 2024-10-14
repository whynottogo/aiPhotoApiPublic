package org.zjzWx.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("web_set")
public class WebSet {
/**
     * 应用设置表
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
/**
     * 小程序appid
     */
    private String appId;
/**
     * 小程序AppSecret
     */
    private String appSecret;
/**
     * 1免费下载，2视频下载
     */
    private Integer downloadOne;
/**
     * 1免费下载，2视频下载
     */
    private Integer downloadTwo;
/**
     * 是否开启鉴黄：1关闭，2开启
     */
    private Integer safetyApi;
/**
     * 广告位id
     */
    private String videoUnitId;

    /**
     * 首页左上角的显示信息
     */
    private String homeLeftInfo;

    /**
     * 首页左上角的跳转地址
     */
    private String homeLeftUrl;


}
