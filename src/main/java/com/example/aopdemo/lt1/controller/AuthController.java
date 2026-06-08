package com.example.aopdemo.lt1.controller;

import com.example.aopdemo.lt1.dto.AuthRequest;
import com.example.aopdemo.lt1.dto.AuthResponse;
import com.example.aopdemo.lt1.dto.RefreshRequest;
import com.example.aopdemo.lt1.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok("Đăng xuất thành công, toàn bộ token đã bị vô hiệu hóa.");
    }
}
