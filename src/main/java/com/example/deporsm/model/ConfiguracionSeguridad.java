package com.example.deporsm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_seguridad")
public class ConfiguracionSeguridad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "two_factor_auth")
    private boolean twoFactorAuth;

    @Column(name = "password_expiration")
    private boolean passwordExpiration;

    @Column(name = "password_expiration_days")
    private int passwordExpirationDays;

    @Column(name = "min_password_length")
    private int minPasswordLength;

    @Column(name = "require_special_chars")
    private boolean requireSpecialChars;

    @Column(name = "require_numbers")
    private boolean requireNumbers;

    @Column(name = "require_uppercase")
    private boolean requireUppercase;

    @Column(name = "max_login_attempts")
    private int maxLoginAttempts;

    @Column(name = "lockout_duration")
    private int lockoutDuration;

    @Column(name = "session_timeout")
    private int sessionTimeout;

    @Column(name = "ip_restriction")
    private boolean ipRestriction;

    @Column(name = "allowed_ips")
    private String allowedIPs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters y setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public boolean isTwoFactorAuth() {
        return twoFactorAuth;
    }
    public void setTwoFactorAuth(boolean twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }
    public boolean isPasswordExpiration() {
        return passwordExpiration;
    }
    public void setPasswordExpiration(boolean passwordExpiration) {
        this.passwordExpiration = passwordExpiration;
    }
    public int getPasswordExpirationDays() {
        return passwordExpirationDays;
    }
    public void setPasswordExpirationDays(int passwordExpirationDays) {
        this.passwordExpirationDays = passwordExpirationDays;
    }
    public int getMinPasswordLength() {
        return minPasswordLength;
    }
    public void setMinPasswordLength(int minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }
    public boolean isRequireSpecialChars() {
        return requireSpecialChars;
    }
    public void setRequireSpecialChars(boolean requireSpecialChars) {
        this.requireSpecialChars = requireSpecialChars;
    }
    public boolean isRequireNumbers() {
        return requireNumbers;
    }
    public void setRequireNumbers(boolean requireNumbers) {
        this.requireNumbers = requireNumbers;
    }
    public boolean isRequireUppercase() {
        return requireUppercase;
    }
    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }
    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }
    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }
    public int getLockoutDuration() {
        return lockoutDuration;
    }
    public void setLockoutDuration(int lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
    }
    public int getSessionTimeout() {
        return sessionTimeout;
    }
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    public boolean isIpRestriction() {
        return ipRestriction;
    }
    public void setIpRestriction(boolean ipRestriction) {
        this.ipRestriction = ipRestriction;
    }
    public String getAllowedIPs() {
        return allowedIPs;
    }
    public void setAllowedIPs(String allowedIPs) {
        this.allowedIPs = allowedIPs;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
