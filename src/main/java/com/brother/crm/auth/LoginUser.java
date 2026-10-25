package com.brother.crm.auth;

import com.brother.crm.entity.Role;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class LoginUser implements UserDetails {

  private final Long id;
  private final String account;
  private final String name;
  private final String phone;
  private final String passwordHash;
  private final Role role;
  private final boolean enabled;

  public LoginUser(
      Long id,
      String account,
      String name,
      String phone,
      String passwordHash,
      Role role,
      boolean enabled) {
    this.id = id;
    this.account = account == null ? "" : account;
    this.name = name;
    this.phone = phone;
    this.passwordHash = passwordHash;
    this.role = role;
    this.enabled = enabled;
  }

  public boolean isAdmin() {
    return role == Role.ADMIN;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return account != null && !account.isBlank() ? account : phone;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
