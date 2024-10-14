package org.zjzWx.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cola.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zjzWx.util.BizStackUtil;
import org.zjzWx.util.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Shynin
 * @version 1.0
 * @date 2024/10/8 23:42
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandle {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Response methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error(StrUtil.format("【{} {}】\t--MethodArgumentNotValidException", httpServletRequest.getMethod(), httpServletRequest.getRequestURI()), e);
        BizStackUtil.stack(log, e, httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        return this.buildFailure(500, Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
    }
    @ExceptionHandler(value = NotLoginException.class)
    public Response notLoginExceptionHandler(NotLoginException e) {
        log.error(StrUtil.format("【{} {}】\t--BizException", httpServletRequest.getMethod(), httpServletRequest.getRequestURI()), e);
        BizStackUtil.stack(log, e, httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        return this.buildFailure(401, e.getMessage());
    }

    @ExceptionHandler(value = {BizException.class})
    public Response badRequest(BizException e) {
        log.error(StrUtil.format("【{} {}】\t--BizException", httpServletRequest.getMethod(), httpServletRequest.getRequestURI()), e);
        BizStackUtil.stack(log, e, httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        return this.buildFailure(Integer.parseInt(e.getErrCode()), e.getMessage());
    }

    @ExceptionHandler
    public Response common(Exception e) {
        log.error(StrUtil.format("【{} {}】\t--commonException", httpServletRequest.getMethod(), httpServletRequest.getRequestURI()), e);
        BizStackUtil.stack(log, e, httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        return this.buildFailure(500, "系统出错啦，请在客服联系管理员");
    }

    private Response buildFailure(Integer errorCode, String errorMsg) {
        return Response.no(errorCode, errorMsg);
    }
}
