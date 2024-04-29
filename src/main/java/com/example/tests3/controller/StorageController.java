package com.example.tests3.controller;

import com.example.tests3.service.StorageService;
import com.example.tests3.service.StorageService.FileDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/file")
public class StorageController {

    @Autowired
    private StorageService service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam(value = "file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
            }

            // 파일 크기 제한 검증 (선택사항)
            // long maxFileSize = 10 * 1024 * 1024; // 10MB
            // if (file.getSize() > maxFileSize) {
            // return new ResponseEntity<>("File size exceeds the limit",
            // HttpStatus.BAD_REQUEST);
            // }

            // 파일 확장자 검증 (선택사항)
            // String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            // if (!allowedExtensions.contains(extension.toLowerCase())) {
            // return new ResponseEntity<>("Invalid file extension",
            // HttpStatus.BAD_REQUEST);
            // }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadedFileUrl = service.uploadFile(file, fileName);
            return new ResponseEntity<>("File uploaded successfully: " + uploadedFileUrl, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileName) {
        byte[] data = service.downloadFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        return new ResponseEntity<>(service.deleteFile(fileName), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileDetail>> listFiles() {
        List<FileDetail> fileList = service.listFiles();
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }
}
