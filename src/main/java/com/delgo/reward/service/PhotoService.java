package com.delgo.reward.service;


import com.delgo.reward.comm.CommService;
import com.delgo.reward.comm.ncp.ObjectStorageService;
import com.sksamuel.scrimage.ImmutableImage;

import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PhotoService extends CommService {
    private final ObjectStorageService objectStorageService;

    private final String DIR = "/var/www/delgo-reward-api/"; // dev
//    private final String DIR = "C:\\testPhoto\\"; // local

    // Encoding File Upload
    public String uploadCertEncodingFile(int certificationId, String photoUrl) {
        String fileName = certificationId + "_cert.jpeg";
        String ncpLink = "https://kr.object.ncloudstorage.com/reward-certification/" + fileName;

        try {
            byte[] decodedByte = Base64.getMimeDecoder().decode(photoUrl.replace("data:image/jpeg;base64,", "").getBytes());
            File convertFile = new File(DIR + fileName);
            if (convertFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(convertFile);
                fos.write(decodedByte);
                fos.close();
            }

            if (convertFile.exists()) {
                objectStorageService.uploadObjects("reward-certification", fileName, DIR + fileName);  // Upload NCP
                convertFile.delete(); // 서버에 저장된 사진 삭제
            }

            ncpLink += "?" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")) + numberGen(4, 1); // Cache 무효화
            return ncpLink;
        } catch (Exception e) {
            throw new NullPointerException("PHOTO UPLOAD ERROR");
        }
    }

    public String uploadCertMultipart(int certificationId, MultipartFile photo) {
        String[] type = Objects.requireNonNull(photo.getOriginalFilename()).split("\\."); // ex) png, jpg, jpeg
        String extension = type[type.length - 1];

        String fileName = certificationId + "_cert.webp";
        String ncpLink = "https://kr.object.ncloudstorage.com/reward-certification/" + fileName;

        try {
            File file = new File(DIR + certificationId + "_cert." + extension);
            photo.transferTo(file); // 서버에 저장

            File webpFile = convertWebp(fileName, file);  // filePath에서 File 불러온 뒤 webp로 변환 후 저장.
            objectStorageService.uploadObjects("reward-certification", fileName, DIR + fileName); // Upload NCP

            file.delete(); // 서버에 저장된 사진.jpg 삭제
            webpFile.delete(); // 서버에 저장된 사진.webp 삭제

            ncpLink += "?" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")) + numberGen(4, 1); // Cache 무효화
            return ncpLink;
        } catch (Exception e) {
            throw new NullPointerException("PHOTO UPLOAD ERROR");
        }
    }

    public String uploadMungple(int mungpleId, MultipartFile photo) {
        String[] type = Objects.requireNonNull(photo.getOriginalFilename()).split("\\."); // ex) png, jpg, jpeg
        String extension = type[type.length - 1];

        String fileName = mungpleId + "_mungple.webp";
        String ncpLink = "https://kr.object.ncloudstorage.com/reward-mungple/" + fileName;

        try {
            File file = new File(DIR + mungpleId + "_mungple." + extension); // 서버에 저장
            photo.transferTo(file);

            File webpFile = convertWebp(fileName, file);  // filePath에서 File 불러온 뒤 webp로 변환 후 저장.

            objectStorageService.uploadObjects("reward-mungple", fileName, DIR + fileName); // Upload NCP
            file.delete(); // 서버에 저장된 사진 삭제
            webpFile.delete();

            ncpLink += "?" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")) + numberGen(4, 1); // Cache 무효화
            return ncpLink;
        } catch (Exception e) {
            throw new NullPointerException("PHOTO UPLOAD ERROR");
        }
    }

    public String uploadMungpleNote(MultipartFile photo) {
        String[] photoName = photo.getOriginalFilename().split("\\.");

        String fileName = photoName[0] + ".webp";
        String ncpLink = "https://kr.object.ncloudstorage.com/reward-mungplenote/" + fileName;

        try {
            File file = new File(DIR + photo.getOriginalFilename()); // 서버에 저장
            photo.transferTo(file);

            File webpFile = convertWebp(fileName, file);  // filePath에서 File 불러온 뒤 webp로 변환 후 저장.

            objectStorageService.uploadObjects("reward-mungplenote", fileName, DIR + fileName); // Upload NCP
            file.delete(); // 서버에 저장된 사진 삭제
            webpFile.delete();

            ncpLink += "?" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")) + numberGen(4, 1); // Cache 무효화
            return ncpLink;
        } catch (Exception e) {
            throw new NullPointerException("PHOTO UPLOAD ERROR");
        }
    }

    public String uploadProfile(int userId, MultipartFile photo) {
        String[] type = Objects.requireNonNull(photo.getOriginalFilename()).split("\\."); // ex) png, jpg, jpeg
        String extension = type[type.length - 1];

        String fileName = userId + "_profile.webp";
        String ncpLink = "https://kr.object.ncloudstorage.com/reward-profile/" + fileName; // NCP Link

        try {
            File file = new File(DIR + userId + "_profile." + extension); // 서버에 저장
            photo.transferTo(file);

            File webpFile = convertWebp(fileName, file);  // filePath에서 File 불러온 뒤 webp로 변환 후 저장.

            objectStorageService.uploadObjects("reward-profile", fileName, DIR + fileName); // Upload NCP

            file.delete(); // 서버에 저장된 사진 삭제
            webpFile.delete();

            ncpLink += "?" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")) + numberGen(4, 1);   // Cache 무효화
            return ncpLink;
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    // jpg -> webp 변경
    public void convertWebp(MultipartFile photo) {
        String[] photoName = photo.getOriginalFilename().split("\\.");
        String fileName = photoName[0] + ".webp";

        log.info("fileName : {}", fileName);
        try {
            File file = new File(DIR + "fileName" + ".jpg"); // 서버에 저장
            photo.transferTo(file);

            File webpFile = convertWebp(fileName, file);  // filePath에서 File 불러온 뒤 webp로 변환 후 저장.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File convertWebp(String fileName, File file) throws IOException {
        return ImmutableImage.loader().fromFile(file)
                .output(WebpWriter.DEFAULT, new File(DIR + fileName));
    }
}
