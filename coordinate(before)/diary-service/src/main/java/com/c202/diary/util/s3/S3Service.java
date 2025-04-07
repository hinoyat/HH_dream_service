package com.c202.diary.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3Client s3Client;
    private final RestTemplate restTemplate;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadVideoFromUrl(String videoUrl) {
        try {
            log.info("받은 URL: {}", videoUrl);

            // 파일명 생성
            String fileName = generateFileName("video", "mp4");

            // URL에서 데이터 가져오기
            URI uri = new URI(videoUrl);
            return downloadAndUploadToS3(uri, fileName);
        } catch (Exception e) {
            log.error("URL 처리 중 오류가 발생", e);
            throw new RuntimeException("URL 처리 중 오류가 발생했습니다", e);
        }
    }


    private String downloadAndUploadToS3(URI uri, String fileName) {
        return restTemplate.execute(uri, HttpMethod.GET, null, clientResponse -> {
            // 메타데이터 설정
            ObjectMetadata metadata = extractAndCreateMetadata(clientResponse);

            try (InputStream inputStream = clientResponse.getBody()) {
                // S3에 파일 업로드
                return uploadToS3(inputStream, fileName, metadata);
            } catch (IOException e) {
                log.error("S3 업로드 실패", e);
                throw new RuntimeException("S3 업로드 실패", e);
            }
        });
    }

    private ObjectMetadata extractAndCreateMetadata(ClientHttpResponse response) throws IOException {
        long contentLength = response.getHeaders().getContentLength();
        String contentType = response.getHeaders().getContentType() != null ?
                response.getHeaders().getContentType().toString() :
                "application/octet-stream";

        log.info("컨텐츠 길이: {}, 컨텐츠 타입: {}", contentLength, contentType);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        return metadata;
    }

    private String uploadToS3(InputStream inputStream, String fileName, ObjectMetadata metadata) {
        // S3에 파일 업로드
        s3Client.putObject(new PutObjectRequest(
                bucket,
                fileName,
                inputStream,
                metadata
        ));

        // S3에 저장된 파일의 URL 반환
        return getS3FileUrl(fileName);
    }

    private String getS3FileUrl(String fileName) {
        return s3Client.getUrl(bucket, fileName).toString();
    }

    private String generateFileName(String prefix, String extension) {
        // UUID와 현재 날짜시간을 조합하여 고유한 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("%s_%s_%s.%s", prefix, uuid, datetime, extension);
    }
}