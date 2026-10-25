package com.brother.crm.auth;

import com.brother.crm.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/health")
  public ApiResponse<Map<String, String>> health() {
    return ApiResponse.ok(Map.of("service", "crm-api", "status", "up"));
  }

  @PostMapping("/login")
  public ApiResponse<Map<String, Object>> login(@Valid @RequestBody AuthService.LoginRequest request) {
    return ApiResponse.ok(authService.login(request));
  }

  @GetMapping("/me")
  public ApiResponse<Map<String, Object>> me() {
    return ApiResponse.ok(authService.me());
  }
}
