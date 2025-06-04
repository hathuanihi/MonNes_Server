package com.SE104.quan_ly_so_tiet_kiem.security;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetails.class);

    private final NguoiDung nguoiDung;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(NguoiDung nd) {
        this.nguoiDung = nd;
        String role = nd.getVaiTro() == 0 ? "ROLE_ADMIN" : "ROLE_USER";
        logger.debug("Creating CustomUserDetails for user: {}, vaiTro: {}, assigned role: {}", 
            nd.getEmail(), nd.getVaiTro(), role);
        this.authorities = List.of(new SimpleGrantedAuthority(role));
        logger.debug("Authorities set: {}", this.authorities);
    }
    
    public Integer getMaND() {
        return nguoiDung.getMaND();
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    @Override
    public String getUsername() {
        return nguoiDung.getEmail();
    }

    @Override
    public String getPassword() {
        return nguoiDung.getMatKhau();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return true;
    }
}
