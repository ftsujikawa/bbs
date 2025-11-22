package com.example.bbs.repository;

import com.example.bbs.domain.Reply;
import com.example.bbs.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByPostOrderByCreatedAtAsc(Post post);
}
