package com.rober.bookshop.service.impl;

import com.rober.bookshop.enums.TokenType;
import com.rober.bookshop.model.entity.Token;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.repository.TokenRepository;
import com.rober.bookshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    // Every 2 minutes (for testing)
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void cleanupUnverifiedUsersAndOldTokens() {
        // Grace period: 3 minutes (for testing)
        Instant grace = Instant.now().minus(3, ChronoUnit.MINUTES);

        // Delete unverified users whose verify tokens expired before grace time
        List<Token> expiredVerifyTokens = tokenRepository
                .findAllByTypeAndExpiresAtBeforeAndUserActiveFalse(TokenType.VERIFY, grace);

        for (Token token : expiredVerifyTokens) {
            User u = token.getUser();
            if (u != null && !u.isActive()) {
                log.info("Cleaning up unverified user {}", u.getEmail());
                tokenRepository.deleteAllByUser(u);
                userRepository.delete(u);
            }
        }

        // Remove revoked tokens (VERIFY, RESET_PASSWORD)
        tokenRepository.findAllByTypeAndRevokedTrue(TokenType.VERIFY).forEach(tokenRepository::delete);
        tokenRepository.findAllByTypeAndRevokedTrue(TokenType.RESET_PASSWORD).forEach(tokenRepository::delete);
    }
}


