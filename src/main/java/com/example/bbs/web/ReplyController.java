package com.example.bbs.web;

import com.example.bbs.domain.Post;
import com.example.bbs.domain.Reply;
import com.example.bbs.repository.UserAccountRepository;
import com.example.bbs.service.AttachmentService;
import com.example.bbs.service.PostService;
import com.example.bbs.service.ReplyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/posts/{postId}/replies")
public class ReplyController {

    private final PostService postService;
    private final ReplyService replyService;
    private final AttachmentService attachmentService;
    private final UserAccountRepository userAccountRepository;

    public ReplyController(PostService postService,
                           ReplyService replyService,
                           AttachmentService attachmentService,
                           UserAccountRepository userAccountRepository) {
        this.postService = postService;
        this.replyService = replyService;
        this.attachmentService = attachmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping
    public String create(@PathVariable Long postId,
                         @ModelAttribute Reply reply,
                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                         Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Post post = postService.findById(postId).orElseThrow();
        reply.setPost(post);
        reply.setAuthor(principal.getName());
        Reply saved = replyService.save(reply);
        attachmentService.saveForReply(saved, files);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{replyId}/delete")
    public String delete(@PathVariable Long postId, @PathVariable Long replyId, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Reply reply = replyService.findById(replyId).orElseThrow();
        String username = principal.getName();
        boolean owner = username.equals(reply.getAuthor());
        boolean admin = userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        if (!owner && !admin) {
            return "redirect:/posts/" + postId;
        }
        replyService.deleteById(replyId);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{replyId}/attachments/{attachmentId}/delete")
    public String deleteReplyAttachment(@PathVariable Long postId,
                                        @PathVariable Long replyId,
                                        @PathVariable Long attachmentId,
                                        Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Reply reply = replyService.findById(replyId).orElseThrow();
        String username = principal.getName();
        boolean owner = username.equals(reply.getAuthor());
        boolean admin = userAccountRepository.findByUsername(username)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        if (!owner && !admin) {
            return "redirect:/posts/" + postId;
        }
        attachmentService.deleteById(attachmentId);
        return "redirect:/posts/" + postId;
    }
}
