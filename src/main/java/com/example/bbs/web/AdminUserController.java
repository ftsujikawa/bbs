package com.example.bbs.web;

import com.example.bbs.domain.UserAccount;
import com.example.bbs.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserAccountRepository userAccountRepository,
                               PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(Model model) {
        List<UserAccount> users = userAccountRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        UserAccount user = userAccountRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/users/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String username,
                         @RequestParam String role,
                         @RequestParam(required = false) String password) {
        UserAccount user = userAccountRepository.findById(id).orElseThrow();
        user.setUsername(username);
        user.setRole(role);
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userAccountRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userAccountRepository.deleteById(id);
        return "redirect:/admin/users";
    }
}
