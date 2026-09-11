package com.thenuka.socialweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Abstract base class for all account types.
 *
 * OOP: Encapsulation - all fields are private, accessed only via getters/setters.
 * OOP: Inheritance - AdminUser and RegularUser extend this class and add their
 *      own specific behaviour.
 * OOP: Polymorphism - getRole() and getDashboardPath() are overridden differently
 *      by each subclass, so calling them on a User reference gives different
 *      results depending on the actual runtime type.
 *
 * Stored as a single database table (SINGLE_TABLE inheritance) with a
 * discriminator column "user_type" that records which subclass each row is.
 */
@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password; // stored as a BCrypt hash, never plain text

    @Column
    private String backupEmail; // optional

    @Column
    private String phoneNumber; // optional

    @Column
    private String gender; // "MALE" or "FEMALE", optional

    // --- Password reset support ---
    @Column
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    // --- Two-factor authentication support ---
    @Column
    private String twoFaCode;

    @Column
    private LocalDateTime twoFaCodeExpiry;

    // --- Profile picture ---
    @Column
    private String avatarFilename; // just the filename - actual file lives in /uploads/avatars/

    protected User() {
        // required by JPA
    }

    protected User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Polymorphic method - each subclass returns its own role name.
     * Used to grant the correct Spring Security authority at login.
     */
    public abstract String getRole();

    /**
     * Polymorphic method - each subclass decides where it lands after login.
     */
    public abstract String getDashboardPath();

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBackupEmail() {
        return backupEmail;
    }

    public void setBackupEmail(String backupEmail) {
        this.backupEmail = backupEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public String getTwoFaCode() {
        return twoFaCode;
    }

    public void setTwoFaCode(String twoFaCode) {
        this.twoFaCode = twoFaCode;
    }

    public LocalDateTime getTwoFaCodeExpiry() {
        return twoFaCodeExpiry;
    }

    public void setTwoFaCodeExpiry(LocalDateTime twoFaCodeExpiry) {
        this.twoFaCodeExpiry = twoFaCodeExpiry;
    }

    public String getAvatarFilename() {
        return avatarFilename;
    }

    public void setAvatarFilename(String avatarFilename) {
        this.avatarFilename = avatarFilename;
    }
}
