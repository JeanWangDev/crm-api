package com.brother.crm.user;

import com.brother.crm.auth.AuthSupport;
import com.brother.crm.common.BizException;
import com.brother.crm.entity.Role;
import com.brother.crm.entity.UserAccount;
import com.brother.crm.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public List<UserView> list() {
    return userRepository.findAll().stream().map(UserService::view).toList();
  }

  @Transactional
  public UserView create(CreateRequest request) {
    requireAdmin();
    String account = request.getAccount().trim();
    if (userRepository.existsByAccount(account)) {
      throw new BizException("账号已存在");
    }
    String phone = blankToNull(request.getPhone());
    if (phone != null && userRepository.existsByPhone(phone)) {
      throw new BizException("手机号已被使用");
    }
    UserAccount user = new UserAccount();
    user.setAccount(account);
    user.setName(request.getName().trim());
    user.setPhone(phone);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole() == null ? Role.ADVISOR : request.getRole());
    user.setEnabled(true);
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    return view(userRepository.save(user));
  }

  @Transactional
  public void resetPassword(Long id, PasswordRequest request) {
    requireAdmin();
    UserAccount user = userRepository.findById(id).orElseThrow(() -> new BizException("账号不存在"));
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
  }

  @Transactional
  public UserView setEnabled(Long id, EnabledRequest request) {
    requireAdmin();
    UserAccount user = userRepository.findById(id).orElseThrow(() -> new BizException("账号不存在"));
    if (user.getId().equals(AuthSupport.currentUser().getId()) && !request.isEnabled()) {
      throw new BizException("不能停用自己的账号");
    }
    if (user.getRole() == Role.ADMIN && !request.isEnabled()) {
      long enabledAdmins = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
      if (enabledAdmins <= 1) {
        throw new BizException("至少保留一个管理员");
      }
    }
    user.setEnabled(request.isEnabled());
    user.setUpdatedAt(Instant.now());
    return view(userRepository.save(user));
  }

  private void requireAdmin() {
    if (!AuthSupport.currentUser().isAdmin()) {
      throw new BizException("只有管理员可以管理账号");
    }
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static UserView view(UserAccount user) {
    return new UserView(
        user.getId(),
        user.getAccount(),
        user.getName(),
        user.getPhone() == null ? "" : user.getPhone(),
        user.getRole().name(),
        Boolean.TRUE.equals(user.getEnabled()));
  }

  public record UserView(Long id, String account, String name, String phone, String role, boolean enabled) {}

  @Data
  public static class CreateRequest {
    @NotBlank(message = "请填写账号")
    @Size(max = 64, message = "账号过长")
    private String account;

    @NotBlank(message = "请填写姓名")
    private String name;

    private String phone;

    @NotBlank(message = "请填写密码")
    @Size(min = 6, max = 32, message = "密码为 6 到 32 位")
    private String password;

    private Role role;
  }

  @Data
  public static class PasswordRequest {
    @NotBlank(message = "请填写密码")
    @Size(min = 6, max = 32, message = "密码为 6 到 32 位")
    private String password;
  }

  @Data
  public static class EnabledRequest {
    private boolean enabled;
  }
}
