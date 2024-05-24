package com.example.tests3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example.tests3.entity.User;
import com.example.tests3.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StorageService {

    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private UserRepository userRepository;

    public void loginAndSetBucketName(String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new IllegalStateException("No user found with the provided email.");
        }
        this.bucketName = extractBucketNameFromURI(user.getS3_bucket_uri());
    }

    private String extractBucketNameFromURI(String s3Uri) {
        if (s3Uri != null && s3Uri.startsWith("s3://")) {
            String pathWithoutScheme = s3Uri.substring(5); // Remove 's3://'
            int firstSlashIndex = pathWithoutScheme.indexOf('/');
            if (firstSlashIndex != -1) {
                return pathWithoutScheme.substring(0, firstSlashIndex);
            }
            return pathWithoutScheme; // In case the URI does not have a path or slash.
        }
        throw new IllegalArgumentException("Invalid S3 URI: " + s3Uri);
    }

    public String uploadChunk(MultipartFile file, String fileName, int chunkIndex, int totalChunks) {
        File tempDir = null;
        File chunkFile = null;
        File mergedFile = null;
        try {
            // Create a temporary directory to store chunk files
            tempDir = new File(System.getProperty("java.io.tmpdir"), "chunks");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Decode the file name to handle URL encoded characters
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
            chunkFile = new File(tempDir, decodedFileName + "-" + chunkIndex);
            log.info("Saving chunk to file: " + chunkFile.getAbsolutePath());

            // Save the chunk to a temporary file
            file.transferTo(chunkFile);
            log.info("Chunk saved: " + chunkFile.exists());

            // If all chunks are uploaded, merge them into a single file and upload to S3
            if (chunkIndex == totalChunks - 1) {
                mergedFile = new File(tempDir, decodedFileName);
                try (FileOutputStream fos = new FileOutputStream(mergedFile);
                     BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
                    for (int i = 0; i < totalChunks; i++) {
                        File chunk = new File(tempDir, decodedFileName + "-" + i);
                        if (!chunk.exists()) {
                            log.error("Chunk file not found: " + chunk.getAbsolutePath());
                            throw new FileNotFoundException("Chunk file not found: " + chunk.getAbsolutePath());
                        }
                        log.info("Merging chunk: " + chunk.getAbsolutePath());
                        try (FileInputStream fis = new FileInputStream(chunk);
                             BufferedInputStream in = new BufferedInputStream(fis)) {
                            IOUtils.copy(in, mergingStream);
                        }
                        chunk.delete();
                    }
                }

                // Upload the merged file to S3
                log.info("Uploading merged file to S3: " + mergedFile.getAbsolutePath());
                try (InputStream inputStream = new FileInputStream(mergedFile)) {
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(mergedFile.length());
                    s3Client.putObject(new PutObjectRequest(bucketName, decodedFileName, inputStream, metadata));
                }

                log.info("File uploaded to S3: " + mergedFile.getAbsolutePath());
                mergedFile.delete();
            }
            return "File uploaded successfully";
        } catch (Exception e) {
            log.error("Failed to upload chunk: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload chunk: " + e.getMessage(), e);
        }
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
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

    public List<FileDetail> listFiles() {
        List<FileDetail> fileList = new ArrayList<>();
        var listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
        ObjectListing objectListing;

        do {
            objectListing = s3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                fileList.add(new FileDetail(
                        objectSummary.getKey(),
                        objectSummary.getSize(),
                        objectSummary.getLastModified(),
                        objectSummary.getStorageClass()));
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        return fileList;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileDetail {
        @JsonProperty("key")
        private String key;

        @JsonProperty("size")
        private long size;

        @JsonProperty("lastModified")
        private java.util.Date lastModified;

        @JsonProperty("storageClass")
        private String storageClass;

        public FileDetail(String key, long size, java.util.Date lastModified, String storageClass) {
            this.key = key;
            this.size = size;
            this.lastModified = lastModified;
            this.storageClass = storageClass;
        }

        // Getters and setters (optional)
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
            return storageClass;
        }
    }
}
