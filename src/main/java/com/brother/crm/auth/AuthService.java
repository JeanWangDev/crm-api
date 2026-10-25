package com.brother.crm.auth;

import com.brother.crm.common.BizException;
import com.brother.crm.entity.UserAccount;
import com.brother.crm.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public Map<String, Object> login(LoginRequest request) {
    String loginName = request.loginName();
    if (loginName.isBlank()) {
      throw new BizException("请填写账号");
    }
    UserAccount user =
        userRepository
            .findByAccount(loginName)
            .or(() -> userRepository.findByPhone(loginName))
            .orElseThrow(() -> new BizException("账号或密码错误"));
    if (!Boolean.TRUE.equals(user.getEnabled())
        || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BizException("账号或密码错误");
    }
    Map<String, Object> body = new HashMap<>();
    body.put("token", jwtService.createToken(user));
    body.put("user", profile(user));
    return body;
  }

  /** 直接用 JWT 里的登录态，避免 /me 再打一次远程库。 */
  public Map<String, Object> me() {
    LoginUser current = AuthSupport.currentUser();
    Map<String, Object> profile = new HashMap<>();
    profile.put("id", current.getId());
    profile.put("account", current.getAccount() == null ? "" : current.getAccount());
    profile.put("name", current.getName());
    profile.put("phone", current.getPhone() == null ? "" : current.getPhone());
    profile.put("role", current.getRole().name());
    return profile;
  }

  private Map<String, Object> profile(UserAccount user) {
    Map<String, Object> profile = new HashMap<>();
    profile.put("id", user.getId());
    profile.put("account", user.getAccount() == null ? "" : user.getAccount());
    profile.put("name", user.getName());
    profile.put("phone", user.getPhone() == null ? "" : user.getPhone());
    profile.put("role", user.getRole().name());
    return profile;
  }

  @Data
  public static class LoginRequest {
    private String account;
    private String phone;

    @NotBlank(message = "请填写密码")
    private String password;

    String loginName() {
      if (account != null && !account.isBlank()) {
        return account.trim();
      }
      return phone == null ? "" : phone.trim();
    }
  }
}
