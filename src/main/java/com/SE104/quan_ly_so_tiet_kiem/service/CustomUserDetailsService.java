package com.SE104.quan_ly_so_tiet_kiem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmailOrSdt) throws UsernameNotFoundException {
        Optional<NguoiDung> nguoiDungOpt = nguoiDungRepository.findByEmail(usernameOrEmailOrSdt);

        if (!nguoiDungOpt.isPresent()) {
            if (!usernameOrEmailOrSdt.contains("@")) { 
                nguoiDungOpt = nguoiDungRepository.findBySdt(usernameOrEmailOrSdt);
            }
        }

        NguoiDung nguoiDung = nguoiDungOpt.orElseThrow(() ->
            new UsernameNotFoundException("Không tìm thấy người dùng với thông tin đăng nhập: " + usernameOrEmailOrSdt)
        );

        return new CustomUserDetails(nguoiDung); 
    }
}