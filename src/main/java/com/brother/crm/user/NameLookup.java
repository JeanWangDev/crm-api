package com.brother.crm.user;
import com.brother.crm.entity.UserAccount;
import com.brother.crm.repository.UserRepository;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NameLookup {

  private final UserRepository userRepository;

  public Map<Long, String> names() {
    return userRepository.findAll().stream()
        .collect(Collectors.toMap(UserAccount::getId, UserAccount::getName, (a, b) -> a));
  }
}
