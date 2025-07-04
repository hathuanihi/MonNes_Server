package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.entity.DangNhap;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.repository.DangNhapRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private DangNhapRepository dangNhapRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;


    private final Map<String, PasscodeEntry> passcodeStore = new HashMap<>();
    private final Map<String, PasscodeEntry> signupPasscodeStore = new HashMap<>();
    private static final long PASSCODE_EXPIRY_MS = 5 * 60 * 1000; 

    private static class PasscodeEntry {
        String passcode;
        long createdAt;
        boolean verified;

        PasscodeEntry(String passcode) {
            this.passcode = passcode;
            this.createdAt = System.currentTimeMillis();
            this.verified = false;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > PASSCODE_EXPIRY_MS;
        }
    }
    

    @Transactional
    public LoginResponse signIn(LoginRequest request) {
        Authentication authentication;
        String identifier = request.getUsername(); 
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng.");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String authenticatedUserEmail = authentication.getName(); 
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(authenticatedUserEmail) 
            .orElseThrow(() -> new EntityNotFoundException("Lỗi không mong muốn: Người dùng đã xác thực nhưng không tìm thấy trong DB với email: " + authenticatedUserEmail ));

        DangNhap dangNhap = new DangNhap();
        dangNhap.setNguoiDung(nguoiDung);
        dangNhap.setLoginTime(new Date());
        dangNhapRepository.save(dangNhap);

        String token = jwtService.generateToken(nguoiDung.getEmail()); 
        UserResponse userResponse = userService.mapToUserResponse(nguoiDung);

        return new LoginResponse(userResponse, token, "Đăng nhập thành công.");
    }

    @Transactional
    public UserResponse signUp(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        if (nguoiDungRepository.existsBySdt(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng.");
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setEmail(request.getEmail());
        nguoiDung.setSdt(request.getPhoneNumber());
        nguoiDung.setMatKhau(passwordEncoder.encode(request.getPassword()));
        // Giả sử vai trò là ENUM hoặc Integer. Cần kiểm tra lại entity NguoiDung
        // nguoiDung.setVaiTro(VaiTro.USER); // Nếu là enum
        nguoiDung.setVaiTro(1); // Nếu là Integer (1 = USER)
        NguoiDung savedNguoiDung = nguoiDungRepository.save(nguoiDung);
        return userService.mapToUserResponse(savedNguoiDung);
    }

    public void sendPasscode(String email) {
        nguoiDungRepository.findByEmail(email) 
                .orElseThrow(() -> new EntityNotFoundException("Email không tồn tại trong hệ thống."));

        String passcode = String.format("%06d", new Random().nextInt(1000000)); 
        passcodeStore.put(email, new PasscodeEntry(passcode));
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh đặt lại mật khẩu - Monnes");
        message.setText("Mã xác minh của bạn là: " + passcode + "\nMã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác minh. Vui lòng thử lại sau.");
        }
    }

    public void verifyPasscode(String email, String passcode) {
        PasscodeEntry entry = passcodeStore.get(email);
        if (entry == null) {
            throw new IllegalArgumentException("Yêu cầu mã xác minh không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.");
        }
        if (entry.isExpired()) {
            passcodeStore.remove(email);
            throw new IllegalArgumentException("Mã xác minh đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (!entry.passcode.equals(passcode)) {
            throw new IllegalArgumentException("Mã xác minh không đúng.");
        }
        entry.verified = true;
    }

    @Transactional
    public void resetPassword(String email, String newPassword, String confirmPassword) {
        PasscodeEntry entry = passcodeStore.get(email);
        if (entry == null || !entry.verified || entry.isExpired()) {
            if(entry != null && entry.isExpired()) passcodeStore.remove(email); 
            throw new IllegalArgumentException("Yêu cầu đặt lại mật khẩu không hợp lệ hoặc mã xác minh đã hết hạn. Vui lòng thực hiện lại quy trình xác minh.");
        }

        if (newPassword == null || newPassword.length() < 6) { 
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới và mật khẩu xác nhận không khớp.");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Email không tìm thấy để đặt lại mật khẩu.")); 

        nguoiDung.setMatKhau(passwordEncoder.encode(newPassword));
        nguoiDungRepository.save(nguoiDung);
        passcodeStore.remove(email); 
    }

    public void sendSignupPasscode(String email) {
        if (nguoiDungRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        String passcode = String.format("%06d", new Random().nextInt(1000000));
        signupPasscodeStore.put(email, new PasscodeEntry(passcode));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh đăng ký tài khoản - Monnes");
        message.setText("Mã xác minh đăng ký của bạn là: " + passcode + "\nMã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác minh. Vui lòng thử lại sau.");
        }
    }

    public void verifySignupPasscode(String email, String passcode) {
        PasscodeEntry entry = signupPasscodeStore.get(email);
        if (entry == null) {
            throw new IllegalArgumentException("Yêu cầu mã xác minh không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.");
        }
        if (entry.isExpired()) {
            signupPasscodeStore.remove(email);
            throw new IllegalArgumentException("Mã xác minh đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (!entry.passcode.equals(passcode)) {
            throw new IllegalArgumentException("Mã xác minh không đúng.");
        }
        entry.verified = true;
    }

    @Transactional
    public UserResponse completeSignup(RegisterRequest request) {
        PasscodeEntry entry = signupPasscodeStore.get(request.getEmail());
        if (entry == null || !entry.verified || entry.isExpired()) {
            if(entry != null && entry.isExpired()) signupPasscodeStore.remove(request.getEmail());
            throw new IllegalArgumentException("Bạn chưa xác thực email hoặc mã xác minh đã hết hạn. Vui lòng xác thực lại email.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        if (nguoiDungRepository.existsBySdt(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng.");
        }
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setEmail(request.getEmail());
        nguoiDung.setSdt(request.getPhoneNumber());
        nguoiDung.setMatKhau(passwordEncoder.encode(request.getPassword()));
        nguoiDung.setVaiTro(1); // USER
        NguoiDung savedNguoiDung = nguoiDungRepository.save(nguoiDung);
        signupPasscodeStore.remove(request.getEmail());
        return userService.mapToUserResponse(savedNguoiDung);
    }
    
    // =========================================================================
    // ✅ MỚI: Thêm phương thức này để lấy thông tin người dùng từ token
    // =========================================================================
    public UserResponse getUserProfile(String username) {
        // Username ở đây chính là email được lấy từ JWT token
        NguoiDung user = nguoiDungRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại với email: " + username));

        // Tái sử dụng phương thức mapping đã có trong UserService để đảm bảo tính nhất quán
        return userService.mapToUserResponse(user);
    }
}