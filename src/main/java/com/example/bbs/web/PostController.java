package com.example.bbs.web;

import com.example.bbs.domain.Post;
import com.example.bbs.service.PostService;
import com.example.bbs.service.ReplyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final ReplyService replyService;

    public PostController(PostService postService, ReplyService replyService) {
        this.postService = postService;
        this.replyService = replyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "posts/list";
    }

    @GetMapping("/new")
    public String form(Model model, Principal principal) {
        model.addAttribute("post", new Post());
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "posts/form";
    }

    @PostMapping
    public String create(@ModelAttribute Post post, Principal principal) {
        if (principal != null) {
            post.setAuthor(principal.getName());
        }
        postService.save(post);
        return "redirect:/posts";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        model.addAttribute("post", post);
        boolean owner = principal != null && principal.getName().equals(post.getAuthor());
        model.addAttribute("owner", owner);
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("replies", replyService.findByPost(post));
        return "posts/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        if (principal == null || !principal.getName().equals(post.getAuthor())) {
            return "redirect:/posts/" + id;
        }
        model.addAttribute("post", post);
        return "posts/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Post form, Principal principal) {
        Post post = postService.findById(id).orElseThrow();
        if (principal == null || !principal.getName().equals(post.getAuthor())) {
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
        if (principal == null || !principal.getName().equals(post.getAuthor())) {
            return "redirect:/posts/" + id;
        }
        postService.deleteById(id);
        return "redirect:/posts";
    }
}
