package com.example.bbs.service;

import com.example.bbs.domain.Attachment;
import com.example.bbs.domain.Post;
import com.example.bbs.domain.Reply;
import com.example.bbs.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttachmentService {

    public static final long MAX_FILE_SIZE = 2L * 1024 * 1024; // 2MB

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<String> saveForPost(Post post, MultipartFile[] files) {
        List<String> errors = new ArrayList<>();
        if (files == null) {
            return errors;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                errors.add(file.getOriginalFilename() + " は2MBを超えています。");
                continue;
            }
            try {
                Attachment attachment = new Attachment();
                attachment.setPost(post);
                attachment.setOriginalFilename(file.getOriginalFilename());
                attachment.setContentType(file.getContentType());
                attachment.setSize(file.getSize());
                attachment.setData(file.getBytes());
                attachmentRepository.save(attachment);
            } catch (IOException e) {
                errors.add(file.getOriginalFilename() + " の保存に失敗しました。");
            }
        }
        return errors;
    }

    public List<String> saveForReply(Reply reply, MultipartFile[] files) {
        List<String> errors = new ArrayList<>();
        if (files == null) {
            return errors;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                errors.add(file.getOriginalFilename() + " は2MBを超えています。");
                continue;
            }
            try {
                Attachment attachment = new Attachment();
                attachment.setReply(reply);
                attachment.setOriginalFilename(file.getOriginalFilename());
                attachment.setContentType(file.getContentType());
                attachment.setSize(file.getSize());
                attachment.setData(file.getBytes());
                attachmentRepository.save(attachment);
            } catch (IOException e) {
                errors.add(file.getOriginalFilename() + " の保存に失敗しました。");
            }
        }
        return errors;
    }

    public List<Attachment> findByPost(Post post) {
        return attachmentRepository.findByPost(post);
    }

    public List<Attachment> findByReply(Reply reply) {
        return attachmentRepository.findByReply(reply);
    }

    public Attachment findById(Long id) {
        return attachmentRepository.findById(id).orElseThrow();
    }

    public void deleteById(Long id) {
        attachmentRepository.deleteById(id);
    }
}
