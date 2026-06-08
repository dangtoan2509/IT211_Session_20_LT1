package com.example.aopdemo.lt1.service;

import com.example.aopdemo.lt1.dto.AuthRequest;
import com.example.aopdemo.lt1.dto.AuthResponse;
import com.example.aopdemo.lt1.dto.RefreshRequest;
import com.example.aopdemo.lt1.entity.Employee;
import com.example.aopdemo.lt1.entity.Token;
import com.example.aopdemo.lt1.entity.TokenType;
import com.example.aopdemo.lt1.repository.EmployeeRepository;
import com.example.aopdemo.lt1.repository.TokenRepository;
import com.example.aopdemo.lt1.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final EmployeeRepository employeeRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        Employee employee = employeeRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        String accessToken = jwtService.generateAccessToken(employee);
        String refreshToken = jwtService.generateRefreshToken(employee);

        saveEmployeeToken(employee, accessToken, TokenType.ACCESS);
        saveEmployeeToken(employee, refreshToken, TokenType.REFRESH);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        Token storedToken = tokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token không hợp lệ"));

        if (storedToken.isExpired() || storedToken.isRevoked() || storedToken.getTokenType() != TokenType.REFRESH) {
            throw new RuntimeException("Refresh Token đã hết hạn hoặc bị vô hiệu hóa");
        }

        String username = jwtService.extractUsername(refreshToken);
        Employee employee = employeeRepository.findByUsername(username).orElseThrow();

        if (jwtService.isTokenValid(refreshToken, employee.getUsername())) {
            String newAccessToken = jwtService.generateAccessToken(employee);
            saveEmployeeToken(employee, newAccessToken, TokenType.ACCESS);
            return new AuthResponse(newAccessToken, refreshToken);
        }

        throw new RuntimeException("Token không hợp lệ");
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String jwt = authHeader.substring(7);
        Token storedToken = tokenRepository.findByTokenValue(jwt).orElse(null);

        if (storedToken != null) {
            Employee employee = storedToken.getEmployee();
            List<Token> validTokens = tokenRepository.findAllValidTokensByEmployee(employee.getId());

            validTokens.stream().forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validTokens);
        }
        SecurityContextHolder.clearContext();
    }

    private void saveEmployeeToken(Employee employee, String jwtToken, TokenType tokenType) {
        Token token = Token.builder()
                .employee(employee)
                .tokenValue(jwtToken)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
