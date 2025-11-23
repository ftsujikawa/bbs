package com.example.bbs.web;

import com.example.bbs.domain.Attachment;
import com.example.bbs.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<byte[]> download(@PathVariable @NonNull Long id) {
        Attachment attachment = attachmentService.findById(id);
        if (attachment.getData() == null) {
            return ResponseEntity.notFound().build();
        }
        String filename = attachment.getOriginalFilename();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        String contentType = attachment.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(attachment.getSize());
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

        return new ResponseEntity<>(attachment.getData(), headers, HttpStatus.OK);
    }
}
