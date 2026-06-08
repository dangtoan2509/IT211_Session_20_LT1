package com.example.aopdemo.lt1.repository;

import com.example.aopdemo.lt1.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenValue(String tokenValue);

    @Query("""
        select t from Token t inner join Employee e on t.employee.id = e.id
        where e.id = :employeeId and (t.expired = false or t.revoked = false)
    """)
    List<Token> findAllValidTokensByEmployee(Long employeeId);
}
