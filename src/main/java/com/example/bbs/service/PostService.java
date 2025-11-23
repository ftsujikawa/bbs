package com.example.bbs.service;

import com.example.bbs.domain.Post;
import com.example.bbs.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Optional<Post> findById(@NonNull Long id) {
        return postRepository.findById(id);
    }

    public Post save(@NonNull Post post) {
        return postRepository.save(post);
    }

    public void deleteById(@NonNull Long id) {
        postRepository.deleteById(id);
    }
}
