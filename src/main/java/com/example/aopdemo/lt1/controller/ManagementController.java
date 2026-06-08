package com.example.aopdemo.lt1.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management")
public class ManagementController {

    @GetMapping("/employees")
    public ResponseEntity<String> getEmployeeData() {
        return ResponseEntity.ok("Dữ liệu nhân sự bảo mật cao: Truy cập API thành công.");
    }
}
