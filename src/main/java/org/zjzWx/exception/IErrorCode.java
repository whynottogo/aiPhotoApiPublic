package org.zjzWx.exception;

/**
 * 错误码抽象
 *
 * @author xiaozhou.qin
 * @date 2022/10/09
 */
public interface IErrorCode {

    /**
     * 错误代码
     *
     * @return {@link String}
     */
    String getErrorCode();

    /**
     * 错误信息
     *
     * @return {@link String}
     */
    String getErrorMsg();
}
