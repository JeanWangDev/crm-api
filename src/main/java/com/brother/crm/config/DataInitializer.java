package com.brother.crm.config;
import com.brother.crm.entity.Role;
import com.brother.crm.entity.UserAccount;
import com.brother.crm.repository.UserRepository;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    if (userRepository.findByAccount("admin").isPresent()) {
      return;
    }
    UserAccount admin = new UserAccount();
    admin.setAccount("admin");
    admin.setName("管理员");
    admin.setPhone(null);
    admin.setPasswordHash(passwordEncoder.encode("123456"));
    admin.setRole(Role.ADMIN);
    admin.setEnabled(true);
    admin.setCreatedAt(Instant.now());
    admin.setUpdatedAt(Instant.now());
    userRepository.save(admin);
  }
}
