package com.hanghae.finalProject.config.S3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor    // final 멤버변수가 있으면 생성자 항목에 포함시킴
@Component
@Service
public class S3Uploader {

     private final AmazonS3Client amazonS3Client;

     @Value("${cloud.aws.s3.bucket}")
     private String bucket;

     // MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
     public String upload(MultipartFile multipartFile, String dirName) throws IOException {
          String fileName = createFileName(multipartFile.getOriginalFilename());
          ObjectMetadata objectMetadata = new ObjectMetadata();
          objectMetadata.setContentLength(multipartFile.getSize());
          objectMetadata.setContentType(multipartFile.getContentType());

          try(InputStream inputStream = multipartFile.getInputStream()) {
               amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

               return amazonS3Client.getUrl(bucket, fileName).toString();
          } catch(IOException e) {
               throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
          }

//          File uploadFile = convert(multipartFile)
//                  .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
//          return upload(uploadFile, dirName);
     }

     private String upload(File uploadFile, String dirName) {
          String fileName = dirName + "/" + uploadFile.getName();
//          String fileName = dirName + "/" + createFileName(uploadFile.getName());
          String uploadImageUrl = putS3(uploadFile, fileName);

          removeNewFile(uploadFile);  // 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

          return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
     }
     
     private String createFileName(String fileName) { // 먼저 파일 업로드 시, 파일명을 난수화하기 위해 random으로 돌립니다.
          return UUID.randomUUID().toString().concat(getFileExtension(fileName));
     }
     
     private String getFileExtension(String fileName) { // file 형식이 잘못된 경우를 확인하기 위해 만들어진 로직이며, 파일 타입과 상관없이 업로드할 수 있게 하기 위해 .의 존재 유무만 판단하였습니다.
          try {
               return fileName.substring(fileName.lastIndexOf("."));
          } catch (StringIndexOutOfBoundsException e) {
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
          }
     }

     private String putS3(File uploadFile, String fileName) {
          amazonS3Client.putObject(
                  new PutObjectRequest(bucket, fileName, uploadFile)
                          .withCannedAcl(CannedAccessControlList.PublicRead)	// PublicRead 권한으로 업로드 됨
          );
          return amazonS3Client.getUrl(bucket, fileName).toString();
     }

     public void deleteFile(String fileName) {
          amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
     }

     private void removeNewFile(File targetFile) {
          if(targetFile.delete()) {
               log.info("파일이 삭제되었습니다.");
          }else {
               log.info("파일이 삭제되지 못했습니다.");
          }
     }

     private Optional<File> convert(MultipartFile file) throws  IOException {
          File convertFile = new File(file.getOriginalFilename());
          if(convertFile.createNewFile()) {
               try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
               }
               return Optional.of(convertFile);
          }
          return Optional.empty();
     }

}
