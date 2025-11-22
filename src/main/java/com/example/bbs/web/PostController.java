package com.example.bbs.web;

import com.example.bbs.domain.Attachment;
import com.example.bbs.domain.Post;
import com.example.bbs.repository.UserAccountRepository;
import com.example.bbs.service.AttachmentService;
import com.example.bbs.service.PostService;
import com.example.bbs.service.ReplyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final ReplyService replyService;
    private final AttachmentService attachmentService;
    private final UserAccountRepository userAccountRepository;

    public PostController(PostService postService,
                          ReplyService replyService,
                          AttachmentService attachmentService,
                          UserAccountRepository userAccountRepository) {
        this.postService = postService;
        this.replyService = replyService;
        this.attachmentService = attachmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);

        Map<String, String> authorImages = new HashMap<>();
        posts.stream()
                .map(Post::getAuthor)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(username -> userAccountRepository.findByUsername(username)
                        .ifPresent(user -> {
                            if (user.getProfileImage() != null) {
                                authorImages.put(username, "/users/" + username + "/image");
                            }
                        }));
        model.addAttribute("authorImages", authorImages);

        return "posts/list";
    }

    @GetMapping("/new")
    public String form(Model model, Principal principal) {
        if (!model.containsAttribute("post")) {
            model.addAttribute("post", new Post());
        }
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);
            userAccountRepository.findByUsername(username).ifPresent(user -> {
                if (user.getProfileImage() != null) {
                    model.addAttribute("profileImagePath", "/users/" + username + "/image");
                }
            });
        }
        return "posts/form";
    }

    @PostMapping
    public String create(@ModelAttribute Post post,
                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        if (principal != null) {
            post.setAuthor(principal.getName());
        }
        // 先に添付ファイルのサイズチェックを行い、2MB超過があれば投稿を保存せずにフォームに戻す
        if (files != null) {
            java.util.List<String> errors = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty() && file.getSize() > AttachmentService.MAX_FILE_SIZE) {
                    errors.add(file.getOriginalFilename() + " は2MBを超えています。");
                }
            }
            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("attachmentErrors", errors);
                redirectAttributes.addFlashAttribute("post", post);
                return "redirect:/posts/new";
            }
        }

        Post saved = postService.save(post);
        attachmentService.saveForPost(saved, files);
        return "redirect:/posts/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        model.addAttribute("post", post);
        String username = principal != null ? principal.getName() : null;
        boolean owner = username != null && username.equals(post.getAuthor());
        boolean admin = username != null && userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        model.addAttribute("owner", owner);
        model.addAttribute("admin", admin);
        model.addAttribute("currentUsername", username);
        model.addAttribute("loggedIn", principal != null);
        userAccountRepository.findByUsername(post.getAuthor()).ifPresent(user -> {
            if (user.getProfileImage() != null) {
                model.addAttribute("authorProfileImagePath", "/users/" + post.getAuthor() + "/image");
            }
        });
        List<Attachment> postAttachments = attachmentService.findByPost(post);
        model.addAttribute("postAttachments", postAttachments);
        List<com.example.bbs.domain.Reply> replies = replyService.findByPost(post);
        model.addAttribute("replies", replies);

        Map<String, String> replyAuthorImages = new HashMap<>();
        Map<Long, List<Attachment>> replyAttachments = new HashMap<>();
        replies.stream()
                .map(com.example.bbs.domain.Reply::getAuthor)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(replyAuthor -> userAccountRepository.findByUsername(replyAuthor)
                        .ifPresent(user -> {
                            if (user.getProfileImage() != null) {
                                replyAuthorImages.put(replyAuthor, "/users/" + replyAuthor + "/image");
                            }
                        }));
        replies.forEach(reply -> {
            List<Attachment> attachments = attachmentService.findByReply(reply);
            if (!attachments.isEmpty()) {
                replyAttachments.put(reply.getId(), attachments);
            }
        });
        model.addAttribute("replyAuthorImages", replyAuthorImages);
        model.addAttribute("replyAttachments", replyAttachments);
        return "posts/detail";
    }

    @PostMapping("/{postId}/attachments/{attachmentId}/delete")
    public String deletePostAttachment(@PathVariable Long postId,
                                       @PathVariable Long attachmentId,
                                       Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Post post = postService.findById(postId).orElseThrow();
        String username = principal.getName();
        boolean owner = username.equals(post.getAuthor());
        boolean admin = userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        if (!owner && !admin) {
            return "redirect:/posts/" + postId;
        }
        attachmentService.deleteById(attachmentId);
        return "redirect:/posts/" + postId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        String username = principal != null ? principal.getName() : null;
        boolean owner = username != null && username.equals(post.getAuthor());
        boolean admin = username != null && userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        if (!owner && !admin) {
            return "redirect:/posts/" + id;
        }
        model.addAttribute("post", post);
        return "posts/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Post form, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        String username = principal != null ? principal.getName() : null;
        boolean owner = username != null && username.equals(post.getAuthor());
        boolean admin = username != null && userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        if (!owner && !admin) {
            return "redirect:/posts/" + id;
        }
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        postService.save(post);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        String username = principal != null ? principal.getName() : null;
        boolean owner = username != null && username.equals(post.getAuthor());
        boolean admin = username != null && userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        if (!owner && !admin) {
            return "redirect:/posts/" + id;
        }
        // まず、この投稿に紐づく返信とその添付ファイルを削除
        List<com.example.bbs.domain.Reply> replies = replyService.findByPost(post);
        for (com.example.bbs.domain.Reply reply : replies) {
            List<Attachment> replyAtts = attachmentService.findByReply(reply);
            for (Attachment att : replyAtts) {
                attachmentService.deleteById(att.getId());
            }
            replyService.deleteById(reply.getId());
        }

        // 次に、この投稿に直接紐づく添付ファイルを削除
        List<Attachment> postAtts = attachmentService.findByPost(post);
        for (Attachment att : postAtts) {
            attachmentService.deleteById(att.getId());
        }

        postService.deleteById(id);
        return "redirect:/posts";
    }
}
