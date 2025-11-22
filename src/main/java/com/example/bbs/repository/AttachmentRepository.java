package com.example.bbs.repository;

import com.example.bbs.domain.Attachment;
import com.example.bbs.domain.Post;
import com.example.bbs.domain.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByPost(Post post);
    List<Attachment> findByReply(Reply reply);
}
