package org.zjzWx.controller;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zjzWx.entity.PicInfo;
import org.zjzWx.model.vo.PicInfoVo;
import org.zjzWx.service.PicInfoService;
import org.zjzWx.util.Response;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Shynin
 * @version 1.0
 * @date 2024/10/11 15:09
 */
@RestController
@RequestMapping("/info")
public class PicInfoController {

    @Resource
    private PicInfoService picInfoService;


    @GetMapping("/list")
    public Response<List<PicInfoVo>> list() {
        List<PicInfo> infoList = picInfoService.lambdaQuery().eq(PicInfo::getDeleted, 0).list();
        List<PicInfoVo> picInfoVos = BeanUtil.copyToList(infoList, PicInfoVo.class);
        return Response.ok(picInfoVos);
    }
}
