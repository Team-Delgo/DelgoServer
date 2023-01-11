package com.delgo.reward.controller;

import com.delgo.reward.comm.CommController;
import com.delgo.reward.comm.exception.ApiCode;
import com.delgo.reward.service.CertService;
import com.delgo.reward.service.MungpleService;
import com.delgo.reward.service.PhotoService;
import com.delgo.reward.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/photo")
public class PhotoController extends CommController {

    private final CertService certService;
    private final UserService userService;
    private final PhotoService photoService;
    private final MungpleService mungpleService;

    /*
     * profile 등록 및 수정 [회원가입 or AccountPage]
     * Request Data : userId, photo
     * - userId : NCP에 올릴 Photo File명에 사용됨. & DB에 photoUrl 등록할 때 User 식별
     * - photo : 확장자는 .png를 기본으로 한다. [.jpeg도 가능] [ 따로 확장자를 체크하진 않는다.]
     * Response Data : ApiCode
     */
    @PostMapping(value={"/upload/profile/{userId}","/upload/profile"})
    public ResponseEntity<?> uploadProfile(@PathVariable Integer userId, @RequestPart(required = false) MultipartFile photo) {
        if (photo.isEmpty()) return ErrorReturn(ApiCode.PARAM_ERROR);

        String ncpLink = photoService.uploadProfile(userId, photo);
        userService.changePhoto(userId, ncpLink);

        return SuccessReturn(ncpLink);
    }

    /*
     * 인증 사진 등록 및 수정
     * Request Data : certificationId, photo
     * - certificationId : NCP에 올릴 Photo File명에 사용됨. & DB에 photoUrl 등록할 때 User 식별
     * - photo : 확장자는 .png를 기본으로 한다. [.jpeg도 가능] [ 따로 확장자를 체크하진 않는다.]
     * Response Data : 사진 저장된 URL
     */
    @PostMapping(value={"/upload/certification/{certificationId}","/upload/certification"})
    public ResponseEntity<?> uploadCertificationPhoto(@PathVariable Integer certificationId, @RequestPart(required = false) MultipartFile photo) {
        if (photo.isEmpty()) return ErrorReturn(ApiCode.PARAM_ERROR);

        String ncpLink = photoService.uploadCertMultipart(certificationId, photo);
        certService.register(certService.getCert(certificationId).setPhotoUrl(ncpLink)); // PhotoUrl 등록

        return SuccessReturn(ncpLink);
    }

    /*
     * 멍플 사진 등록 및 수정
     * Request Data : mungpleId, photo
     * Response Data : 사진 저장된 URL
     */
    @PostMapping(value={"/upload/mungple/{mungpleId}","/upload/mungple"})
    public ResponseEntity<?> uploadMungplePhoto(@PathVariable Integer mungpleId, @RequestPart(required = false) MultipartFile photo) {
        if (photo.isEmpty()) return ErrorReturn(ApiCode.PARAM_ERROR);

        String ncpLink = photoService.uploadMungple(mungpleId, photo);
        mungpleService.register(mungpleService.getMungpleById(mungpleId).setPhotoUrl(ncpLink)); // PhotoUrl 등록

        return SuccessReturn(ncpLink);
    }

    /*
     * 멍플노트 등록
     * Request Data : mungpleId, photo
     * Response Data : 사진 저장된 URL
     */
    @PostMapping(value={"/upload/mungplenote/{mungpleId}","/upload/mungplenote"})
    public ResponseEntity<?> uploadMungpleNote(@PathVariable Integer mungpleId, @RequestPart(required = false) MultipartFile photo) {
        if (photo.isEmpty()) return ErrorReturn(ApiCode.PARAM_ERROR);

        String ncpLink = photoService.uploadMungpleNote(photo);
        mungpleService.register(mungpleService.getMungpleById(mungpleId).setDetailUrl(ncpLink)); // PhotoUrl 등록

        return SuccessReturn(ncpLink);
    }

    /*
     * photo file -> webp로 변환
     * Request Data : photo
     * Response Data : null
     */
    @PutMapping("/webp")
    public ResponseEntity<?> convertWebp(@RequestPart MultipartFile photo) {
        photoService.convertWebp(photo);

        return SuccessReturn();
    }
}
