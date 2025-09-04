package com.rober.bookshop.controller;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                
                health.put("status", "UP");
                health.put("poolName", hikariDataSource.getPoolName());
                health.put("activeConnections", poolMXBean.getActiveConnections());
                health.put("idleConnections", poolMXBean.getIdleConnections());
                health.put("totalConnections", poolMXBean.getTotalConnections());
                health.put("maxPoolSize", hikariDataSource.getMaximumPoolSize());
                health.put("minIdle", hikariDataSource.getMinimumIdle());
                health.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
                
                // Test connection
                try (var connection = dataSource.getConnection()) {
                    health.put("connectionTest", "SUCCESS");
                    health.put("connectionValid", connection.isValid(5));
                }
            } else {
                health.put("status", "UP");
                health.put("dataSourceType", dataSource.getClass().getSimpleName());
            }
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/app")
    public ResponseEntity<Map<String, String>> getAppHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", java.time.Instant.now().toString());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
}
