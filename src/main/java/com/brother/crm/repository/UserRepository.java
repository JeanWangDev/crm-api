package com.brother.crm.repository;
import com.brother.crm.entity.Role;
import com.brother.crm.entity.UserAccount;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByAccount(String account);

  Optional<UserAccount> findByPhone(String phone);

  boolean existsByAccount(String account);

  boolean existsByPhone(String phone);

  long countByRoleAndEnabled(Role role, Boolean enabled);
}
