package com.example.bbs.service;

import com.example.bbs.domain.Post;
import com.example.bbs.domain.Reply;
import com.example.bbs.repository.ReplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;

    public ReplyService(ReplyRepository replyRepository) {
        this.replyRepository = replyRepository;
    }

    public List<Reply> findByPost(Post post) {
        return replyRepository.findByPostOrderByCreatedAtAsc(post);
    }

    public Reply save(@NonNull Reply reply) {
        return replyRepository.save(reply);
    }

    public Optional<Reply> findById(@NonNull Long id) {
        return replyRepository.findById(id);
    }

    public void deleteById(@NonNull Long id) {
        replyRepository.deleteById(id);
    }
}
