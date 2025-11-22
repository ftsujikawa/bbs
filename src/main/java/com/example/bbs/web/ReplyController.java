package com.example.bbs.web;

import com.example.bbs.domain.Post;
import com.example.bbs.domain.Reply;
import com.example.bbs.service.PostService;
import com.example.bbs.service.ReplyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/posts/{postId}/replies")
public class ReplyController {

    private final PostService postService;
    private final ReplyService replyService;

    public ReplyController(PostService postService, ReplyService replyService) {
        this.postService = postService;
        this.replyService = replyService;
    }

    @PostMapping
    public String create(@PathVariable Long postId, @ModelAttribute Reply reply, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Post post = postService.findById(postId).orElseThrow();
        reply.setPost(post);
        reply.setAuthor(principal.getName());
        replyService.save(reply);
        return "redirect:/posts/" + postId;
    }
}
