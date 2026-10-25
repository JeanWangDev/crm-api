package com.brother.crm.user;

import com.brother.crm.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ApiResponse<List<UserService.UserView>> list() {
    return ApiResponse.ok(userService.list());
  }

  @PostMapping
  public ApiResponse<UserService.UserView> create(@Valid @RequestBody UserService.CreateRequest request) {
    return ApiResponse.ok(userService.create(request));
  }

  @PostMapping("/{id}/password")
  public ApiResponse<Void> resetPassword(
      @PathVariable Long id, @Valid @RequestBody UserService.PasswordRequest request) {
    userService.resetPassword(id, request);
    return ApiResponse.ok();
  }

  @PostMapping("/{id}/enabled")
  public ApiResponse<UserService.UserView> setEnabled(
      @PathVariable Long id, @RequestBody UserService.EnabledRequest request) {
    return ApiResponse.ok(userService.setEnabled(id, request));
  }
}
