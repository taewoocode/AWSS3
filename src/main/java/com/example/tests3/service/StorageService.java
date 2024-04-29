package com.example.tests3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file, String fileName) {
        try {
            File fileObj = convertMultiPartFileToFile(file);
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
            fileObj.delete();
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("Error downloading file", e);
            return null;
        }
    }

    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed ...";
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

    public List<FileDetail> listFiles() {
        List<FileDetail> fileList = new ArrayList<>();
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName);
        ObjectListing objectListing;

        do {
            objectListing = s3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                fileList.add(new FileDetail(
                        objectSummary.getKey(),
                        objectSummary.getSize(),
                        objectSummary.getLastModified(),
                        objectSummary.getStorageClass() // 스토리지 클래스 정보 추가
                ));
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated()); // Handle pagination

        return fileList;
    }

    public static class FileDetail {
        private String key;
        private long size;
        private java.util.Date lastModified;
        private String storageClass; // 스토리지 클래스를 저장하기 위한 필드 추가

        public FileDetail(String key, long size, java.util.Date lastModified, String storageClass) {
            this.key = key;
            this.size = size;
            this.lastModified = lastModified;
            this.storageClass = storageClass;
        }

        public String getKey() {
            return key;
        }

        public long getSize() {
            return size;
        }

        public java.util.Date getLastModified() {
            return lastModified;
        }

        public String getStorageClass() {
            return storageClass; // 스토리지 클래스를 반환하는 메소드 추가
        }
    }
}
