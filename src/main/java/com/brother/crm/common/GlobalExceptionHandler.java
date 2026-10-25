package com.brother.crm.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BizException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleBiz(BizException ex) {
    return ApiResponse.fail(ex.getMessage());
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleValid(Exception ex) {
    String message = "参数不正确";
    if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
      message = manv.getBindingResult().getFieldError().getDefaultMessage();
    } else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
      message = be.getBindingResult().getFieldError().getDefaultMessage();
    }
    return ApiResponse.fail(message);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleOther(Exception ex) {
    log.error("未处理异常", ex);
    return ApiResponse.fail(ex.getMessage() == null ? "服务器错误" : ex.getMessage());
  }
}
