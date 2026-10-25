package com.brother.crm.auth;
import com.brother.crm.common.BizException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthSupport {
  private AuthSupport() {}

  public static LoginUser currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof LoginUser user)) {
      throw new BizException("未登录");
    }
    return user;
  }
}
