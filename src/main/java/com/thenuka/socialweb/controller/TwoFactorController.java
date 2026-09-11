package com.thenuka.socialweb.controller;

import com.thenuka.socialweb.model.User;
import com.thenuka.socialweb.repository.UserRepository;
import com.thenuka.socialweb.service.EmailService;
import com.thenuka.socialweb.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Controller
public class TwoFactorController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserDetailsServiceImpl userDetailsService;
    private final RememberMeServices rememberMeServices;
    private final SecureRandom random = new SecureRandom();

    public TwoFactorController(UserRepository userRepository, EmailService emailService,
                                UserDetailsServiceImpl userDetailsService, RememberMeServices rememberMeServices) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.userDetailsService = userDetailsService;
        this.rememberMeServices = rememberMeServices;
    }

    @GetMapping("/verify-2fa")
    public String showVerifyForm(HttpSession session) {
        if (session.getAttribute("PENDING_2FA_USER") == null) {
            return "redirect:/login";
        }
        return "verify-2fa";
    }

    @PostMapping("/verify-2fa")
    public String verifyCode(@RequestParam String code,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session,
                              Model model) {

        String pendingUsername = (String) session.getAttribute("PENDING_2FA_USER");
        if (pendingUsername == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(pendingUsername).orElseThrow();

        boolean codeValid = user.getTwoFaCode() != null
                && user.getTwoFaCode().equals(code)
                && user.getTwoFaCodeExpiry() != null
                && user.getTwoFaCodeExpiry().isAfter(LocalDateTime.now());

        if (!codeValid) {
            model.addAttribute("error", "That code is incorrect or has expired.");
            return "verify-2fa";
        }

        // Code is correct - clear it so it can't be reused, then actually log the user in
        user.setTwoFaCode(null);
        user.setTwoFaCodeExpiry(null);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        // Persist the now-real login into the session
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        // Now that they're genuinely logged in, honour "Remember me" if it was checked
        Boolean rememberMeRequested = (Boolean) session.getAttribute("REMEMBER_ME_REQUESTED");
        if (Boolean.TRUE.equals(rememberMeRequested)) {
            rememberMeServices.loginSuccess(request, response, authToken);
        }

        session.removeAttribute("PENDING_2FA_USER");
        session.removeAttribute("REMEMBER_ME_REQUESTED");

        return "redirect:" + user.getDashboardPath();
    }

    @PostMapping("/resend-2fa-code")
    public String resendCode(HttpSession session, RedirectAttributes redirectAttributes) {
        String pendingUsername = (String) session.getAttribute("PENDING_2FA_USER");
        if (pendingUsername == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(pendingUsername).orElseThrow();

        String code = String.valueOf(100000 + random.nextInt(900000));
        user.setTwoFaCode(code);
        user.setTwoFaCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendTwoFactorCode(user.getEmail(), code);

        redirectAttributes.addFlashAttribute("resent", true);
        return "redirect:/verify-2fa";
    }
}
