package com.thenuka.socialweb.config;

import com.thenuka.socialweb.model.User;
import com.thenuka.socialweb.repository.UserRepository;
import com.thenuka.socialweb.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Runs immediately after Spring Security confirms the username/password are correct.
 * Instead of letting the user straight in, this:
 *   1. Generates a 6-digit code and emails it
 *   2. Wipes the session Spring Security just created (so they're NOT actually logged in yet)
 *   3. Redirects to /verify-2fa, where TwoFactorController finishes the job once the
 *      correct code is entered.
 */
@Component
public class TwoFactorAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public TwoFactorAuthenticationSuccessHandler(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        String code = String.valueOf(100000 + random.nextInt(900000)); // 6 digits
        user.setTwoFaCode(code);
        user.setTwoFaCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendTwoFactorCode(user.getEmail(), code);

        // Remember whether "Remember me" was checked - we need this AFTER 2FA succeeds,
        // but we're about to wipe this session, so save it into the new pending session.
        boolean rememberMeRequested = request.getParameter("remember-me") != null;

        // Undo the full login Spring Security just granted - they only get real
        // access after TwoFactorController confirms the code.
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("PENDING_2FA_USER", username);
        newSession.setAttribute("REMEMBER_ME_REQUESTED", rememberMeRequested);

        // Expire any remember-me cookie so it can't silently skip 2FA on the next visit
        Cookie rememberMeCookie = new Cookie("remember-me", null);
        rememberMeCookie.setPath("/");
        rememberMeCookie.setMaxAge(0);
        response.addCookie(rememberMeCookie);

        response.sendRedirect("/verify-2fa");
    }
}
