package com.rober.bookshop.repository;

import com.rober.bookshop.enums.TokenType;
import com.rober.bookshop.model.entity.Token;
import com.rober.bookshop.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Token findByToken(String token);
//    @Query("select t from Token t where t.user = ?1 and t.type = ?2 and t.revoked = false and t.expiresAt > ?3")
    List<Token> findByUserAndTypeAndRevokedFalseAndExpiresAtAfter(User user, TokenType type, Instant now);
    List<Token> findAllByTypeAndExpiresAtBeforeAndUserActiveFalse(TokenType type, Instant time);
    void deleteAllByUser(User user);
    List<Token> findAllByTypeAndRevokedTrue(TokenType type);
}
