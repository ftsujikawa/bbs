package com.example.bbs.web;

import com.example.bbs.domain.UserAccount;
import com.example.bbs.repository.UserAccountRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserImageController {

    private final UserAccountRepository userAccountRepository;

    public UserImageController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/users/{username}/image")
    public ResponseEntity<byte[]> getUserImage(@PathVariable String username) {
        Optional<UserAccount> opt = userAccountRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserAccount user = opt.get();
        if (user.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        String contentType = user.getProfileImageContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.IMAGE_PNG_VALUE;
        }
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new ResponseEntity<>(user.getProfileImage(), headers, HttpStatus.OK);
    }
}
