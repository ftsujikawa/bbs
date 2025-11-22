package com.example.bbs.web;

import com.example.bbs.domain.UserAccount;
import com.example.bbs.repository.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public AuthController(UserAccountRepository userAccountRepository,
                          PasswordEncoder passwordEncoder,
                          UserDetailsService userDetailsService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @InitBinder("user")
    public void initUserBinder(WebDataBinder binder) {
        binder.setDisallowedFields("profileImage", "profileImageContentType");
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserAccount());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") UserAccount form,
                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                           Model model) {
        if (userAccountRepository.findByUsername(form.getUsername()).isPresent()) {
            model.addAttribute("error", "そのユーザ名は既に使用されています。");
            return "auth/register";
        }
        form.setPassword(passwordEncoder.encode(form.getPassword()));
        if (form.getRole() == null || form.getRole().isBlank()) {
            form.setRole("USER");
        }
        // プロフィール画像をDBに保存
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                form.setProfileImage(profileImage.getBytes());
                form.setProfileImageContentType(profileImage.getContentType());
            } catch (Exception e) {
                // 失敗しても登録自体は続行
                e.printStackTrace();
            }
        }

        userAccountRepository.save(form);

        // 自動ログイン
        UserDetails userDetails = userDetailsService.loadUserByUsername(form.getUsername());
        Authentication authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/posts";
    }
}
