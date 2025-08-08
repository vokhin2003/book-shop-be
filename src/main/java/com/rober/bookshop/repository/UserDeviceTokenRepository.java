package com.rober.bookshop.repository;

import com.rober.bookshop.model.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long>, JpaSpecificationExecutor<UserDeviceToken> {

    boolean existsByUserIdAndDeviceTokenAndDeviceType(Long userId, String deviceToken, String deviceType);
    void deleteByUserIdAndDeviceTokenAndDeviceType(Long userId, String deviceToken, String deviceType);
    List<UserDeviceToken> findByUserIdAndDeviceTypeAndIsActiveTrue(Long userId, String deviceType);
    Optional<UserDeviceToken> findFirstByUserIdAndDeviceTypeOrderByUpdatedAtDesc(Long userId, String deviceType);
    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);
    Optional<UserDeviceToken> findByUserIdAndDeviceTokenAndDeviceType(Long userId, String deviceToken, String deviceType);
    List<UserDeviceToken> findByUserIdAndIsActiveTrue(Long userId);

}
