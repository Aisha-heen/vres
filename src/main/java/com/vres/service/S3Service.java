package com.vres.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

@Service
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    private final String bucketName = "vres-qr";
    
    @Value("${aws.s3.presigned-url.duration-minutes:15}")
    private long presignedUrlDurationMinutes;

    public S3Service(
            @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
            @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${spring.cloud.aws.region.static}") String region) {

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public String uploadQRCode(byte[] qrImage, String s3ObjectKey) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(qrImage);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(qrImage.length);
        metadata.setContentType("image/png");

        s3Client.putObject(bucketName, s3ObjectKey, inputStream, metadata);
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * presignedUrlDurationMinutes;
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, s3ObjectKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return url.toString();
        } catch (Exception e) {
            return s3Client.getUrl(bucketName, s3ObjectKey).toString();
        }
    }

    public byte[] downloadFileAsBytes(String s3Link) {
        try {
            String objectKey = extractKeyFromS3Link(s3Link);
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
            InputStream inputStream = s3Object.getObjectContent();
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from S3: " + s3Link, e);
        }
    }

	private String extractKeyFromS3Link(String s3Link) {
        try {
            URL url = URI.create(s3Link).toURL();
            String path = url.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            return s3Link.substring(s3Link.lastIndexOf("/") + 1);
        }
    }
}
