package com.SE104.quan_ly_so_tiet_kiem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Authentication", description = "API cho xác thực, đăng ký và quản lý mật khẩu người dùng")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Operation(summary = "Đăng nhập vào hệ thống")
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody LoginRequest request) {
        logger.info("Request: Sign in for user: {}", request.getUsername());
        LoginResponse response = authService.signIn(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Đăng ký tài khoản người dùng mới")
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody RegisterRequest request) {
        logger.info("Request: Sign up for email: {}", request.getEmail());
        UserResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Yêu cầu gửi passcode về email để đặt lại mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Request: Forgot password for email: {}", request.getEmail());
        authService.sendPasscode(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Mã xác minh đã được gửi đến email của bạn."));
    }

    @Operation(summary = "Người dùng nhập passcode nhận được qua email để xác minh")
    @PostMapping("/verify-passcode")
    public ResponseEntity<MessageResponse> verifyPasscode(@Valid @RequestBody VerifyPasscodeRequest request) {
        logger.info("Request: Verify passcode for email: {}", request.getEmail());
        authService.verifyPasscode(request.getEmail(), request.getPasscode());
        return ResponseEntity.ok(new MessageResponse("Xác minh mã passcode thành công. Bạn có thể đặt lại mật khẩu mới."));
    }

    @Operation(summary = "Sau khi xác minh passcode, người dùng nhập mật khẩu mới và gửi lên để đặt lại mật khẩu")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("Request: Reset password for email: {}", request.getEmail());
        authService.resetPassword(request.getEmail(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(new MessageResponse("Đặt lại mật khẩu thành công."));
    }

    @Operation(summary = "Gửi passcode về email để bắt đầu đăng ký tài khoản")
    @PostMapping("/signup/request-passcode")
    public ResponseEntity<MessageResponse> requestSignupPasscode(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Request: Signup passcode for email: {}", request.getEmail());
        authService.sendSignupPasscode(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Mã xác minh đã được gửi đến email của bạn."));
    }

    @Operation(summary = "Xác thực passcode đăng ký tài khoản")
    @PostMapping("/signup/verify-passcode")
    public ResponseEntity<MessageResponse> verifySignupPasscode(@Valid @RequestBody VerifyPasscodeRequest request) {
        logger.info("Request: Verify signup passcode for email: {}", request.getEmail());
        authService.verifySignupPasscode(request.getEmail(), request.getPasscode());
        return ResponseEntity.ok(new MessageResponse("Xác minh mã passcode thành công. Bạn có thể tiếp tục đăng ký tài khoản."));
    }

    @Operation(summary = "Hoàn tất đăng ký tài khoản sau khi xác thực passcode")
    @PostMapping("/signup/complete")
    public ResponseEntity<?> completeSignup(@Valid @RequestBody RegisterRequest request) {
        logger.info("Request: Complete signup for email: {}", request.getEmail());
        UserResponse response = authService.completeSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}