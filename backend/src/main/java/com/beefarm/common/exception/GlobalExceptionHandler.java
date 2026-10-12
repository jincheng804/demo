package com.beefarm.common.exception;

import com.beefarm.common.core.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> business(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常[{}] {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> accessDenied(AccessDeniedException e) {
        return R.fail(403, "没有访问权限，请联系管理员授权");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public R<Void> badCredentials(BadCredentialsException e) {
        return R.fail(400, "用户名或密码错误");
    }

    @ExceptionHandler(AuthenticationException.class)
    public R<Void> authentication(AuthenticationException e) {
        return R.fail(401, "认证失败或登录已过期，请重新登录");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> valid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return R.fail(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> bind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return R.fail(400, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> constraint(ConstraintViolationException e) {
        return R.fail(400, e.getMessage());
    }

    @ExceptionHandler({org.springframework.dao.DuplicateKeyException.class})
    public R<Void> duplicate(Exception e) { return R.fail(409, "记录已存在，请刷新后重试"); }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public R<Void> malformed(Exception e) { return R.fail(400, "请求参数格式不正确"); }

    @ExceptionHandler(Exception.class)
    public R<Void> exception(Exception e, HttpServletRequest request) {
        log.error("系统异常[{}]", request.getRequestURI(), e);
        return R.fail(500, "系统暂时无法处理请求，请稍后重试");
    }
}
