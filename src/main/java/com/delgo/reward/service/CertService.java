package com.delgo.reward.service;


import com.delgo.reward.comm.code.CategoryCode;
import com.delgo.reward.comm.ncp.ReverseGeoService;
import com.delgo.reward.domain.certification.Certification;
import com.delgo.reward.domain.achievements.Achievements;
import com.delgo.reward.domain.common.Location;
import com.delgo.reward.domain.like.LikeList;
import com.delgo.reward.dto.certification.LiveCertDTO;
import com.delgo.reward.dto.certification.ModifyCertDTO;
import com.delgo.reward.dto.certification.PastCertDTO;
import com.delgo.reward.repository.CertRepository;
import com.delgo.reward.repository.JDBCTemplateRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CertService {

    // Service
    private final UserService userService;
    private final PointService pointService;
    private final PhotoService photoService;
    private final ArchiveService archiveService;
    private final MungpleService mungpleService;
    private final RankingService rankingService;
    private final LikeListService likeListService;
    private final ReverseGeoService reverseGeoService;
    private final AchievementsService achievementsService;

    // Repository
    private final CertRepository certRepository;
    private final JDBCTemplateRankingRepository jdbcTemplateRankingRepository;

    private final LocalDateTime start = LocalDate.now().atTime(0, 0, 0);
    private final LocalDateTime end = LocalDate.now().atTime(0, 0, 0).plusDays(1);

    // Certification ??????
    public Certification register(Certification certification) {
        return certRepository.save(certification);
    }

    // Past ??????
    public Certification registerLive(LiveCertDTO dto) {
        Location location = reverseGeoService.getReverseGeoData(new Location(dto.getLatitude(), dto.getLongitude()));
        Certification certification = register(dto.toEntity(location));

        // ?????? ????????? ?????? Check
        List<Achievements> earnAchievements = achievementsService.checkEarnAchievements(dto.getUserId(), dto.getMungpleId() != 0);
        if (!earnAchievements.isEmpty()) {
            archiveService.registerArchives(earnAchievements.stream()
                    .map(achievement -> achievement.toArchive(dto.getUserId())).collect(Collectors.toList()));
            certification.setAchievements(earnAchievements);
        }

        // ?????? ?????? ?????? ??????
        String photoUrl = photoService.uploadCertEncodingFile(certification.getCertificationId(), dto.getPhoto());
        register(certification.setPhotoUrl(photoUrl));
        // Point ??????
        pointService.givePoint(userService.getUserById(dto.getUserId()).getUserId(), CategoryCode.valueOf(dto.getCategoryCode()).getPoint());
        // ?????? ??????????????? ??????
        rankingService.rankingByPoint();

        log.info("requestBody : {}", dto.toLog()); // log ?????? ??? photo ??????
        return certification;
    }

    // Past ??????
    public Certification registerPast(PastCertDTO dto) {
        boolean isMungple = (dto.getMungpleId() != 0);
        Certification certification = register((isMungple) ? dto.toEntity(mungpleService.getMungpleById(dto.getMungpleId())) : dto.toEntity());

        // ?????? ????????? ?????? Check
        List<Achievements> earnAchievements = achievementsService.checkEarnAchievements(dto.getUserId(), dto.getMungpleId() != 0);
        if (!earnAchievements.isEmpty()) {
            archiveService.registerArchives(earnAchievements.stream()
                    .map(achievement -> achievement.toArchive(dto.getUserId())).collect(Collectors.toList()));
            certification.setAchievements(earnAchievements);
        }

        return register(certification);
    }

    // Certification ??????
    public Certification modify(ModifyCertDTO dto) {
        return certRepository.save(getCert(dto.getCertificationId()).modify(dto.getDescription()));
    }

    // Certification ??????
    public void delete(Certification certification) {
        certRepository.delete(certification);
    }

    // ?????? Certification ????????? ??????
    public Slice<Certification> getCertAll(int userId, int currentPage, int pageSize, boolean isDesc) {
        PageRequest pageRequest = (isDesc)
                ? PageRequest.of(currentPage, pageSize, Sort.by("regist_dt").descending()) // ???????????? ??????
                : PageRequest.of(currentPage, pageSize, Sort.by("regist_dt")); // ???????????? ??????

        Slice<Certification> certifications = certRepository.findAllByPaging(userId, pageRequest);
        certifications.getContent().forEach(cert -> setUserAndLike(userId, cert));
        return certifications;
    }

    // ???????????? ??? ??????
    public Slice<Certification> getCertByCategory(int userId, String categoryCode, int currentPage, int pageSize, boolean isDesc) {
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, (isDesc) ? Sort.by("registDt").descending() :
                Sort.by("registDt"));

        Slice<Certification> certifications = (!categoryCode.equals(CategoryCode.TOTAL.getCode()))
                ? certRepository.findByUserIdAndCategoryCode(userId, categoryCode, pageRequest)
                : certRepository.findByUserId(userId, pageRequest);

        certifications.getContent().forEach(cert -> setUserAndLike(userId, cert));
        return certifications;
    }

    // ???????????? ??? ?????? ??????
    public Map<String, Long> getCountByCategory(int userId) {
        Map<String, Long> map = getCertByUserId(userId).stream().collect(groupingBy(cert -> CategoryCode.valueOf(cert.getCategoryCode()).getValue(),counting()));
        //*** putIfAbsent
        //- Key ?????? ???????????? ?????? Map??? Value??? ?????? ????????????, Key?????? ???????????? ?????? ?????? Key??? Value??? Map??? ???????????? Null??? ???????????????.
         for (CategoryCode categoryCode : CategoryCode.values())
             map.putIfAbsent(categoryCode.getValue(), 0L);
        return map;
    }


    public Certification setUserAndLike(int userId, Certification cert) {
        return cert.setUserAndLike(
                userService.getUserById(cert.getUserId()), // USER
                likeListService.hasLiked(userId, cert.getCertificationId()), // User is Liked?
                likeListService.getLikeCount(cert.getCertificationId()) // Like Count
        );
    }

    // Live Certification ??????
    public List<Certification> getLive(int userId) {
        return certRepository.findByUserIdAndIsLive(userId, true);
    }

    // Past Certification ??????
    public List<Certification> getPast(int userId) {
        return certRepository.findByUserIdAndIsLive(userId, false);
    }

    // Id??? Certification ??????
    public Certification getCert(int certificationId) {
        return certRepository.findById(certificationId)
                .orElseThrow(() -> new NullPointerException("NOT FOUND Certification id : " + certificationId));
    }

    // userId??? Certification ??????
    public List<Certification> getCertByUserId(int userId) {
        return certRepository.findByUserId(userId);
    }

    // ?????? 2??? ?????? ??????
    public List<Certification> getTheLastTwoCert(int userId) {
        return certRepository.findTwoRecentCert(userId).stream()
                .peek(cert -> setUserAndLike(userId, cert)).collect(Collectors.toList());
    }

    // ????????? Check
    public void like(int userId, int certificationId, int ownerId) throws IOException {
        // ???????????? ?????? Certification ????????? ???????????? ??????.
        boolean isLike = LikeListService.likeHashMap.getOrDefault(new LikeList(userId, certificationId), false);
        if (isLike)  // ????????? ??????
            likeListService.unlike(userId, certificationId);
        else
            likeListService.like(userId, certificationId, ownerId);
    }

    // Comment Count + 1
    public void plusCommentCount(int certificationId) {
        jdbcTemplateRankingRepository.plusCommentCount(certificationId);
    }

    // Comment Count - 1
    public void minusCommentCount(int certificationId) {
        jdbcTemplateRankingRepository.minusCommentCount(certificationId);
    }

    // ????????? ?????? ???????????? 5??? ?????? ?????? ????????? ??????
    public boolean checkCategoryCountIsFive(int userId, String categoryCode, boolean isLive) {
        List<Certification> list = certRepository.findByUserIdAndCategoryCodeAndIsLiveAndRegistDtBetween(userId, categoryCode, isLive, start, end);
        return list.size() < 5;
    }

    // 6?????? ?????? ?????? ?????? ?????? ????????? ( ????????? )
//    public boolean checkContinueRegist(int userId, int mungpleId, boolean isLive) {
//        List<Certification> certifications =
//                certRepository.findByUserIdAndMungpleIdAndIsLiveAndRegistDtBetween(userId, mungpleId, isLive, start,
//                        end).stream().sorted(Comparator.comparing(Certification::getRegistDt).reversed()).collect(Collectors.toList());
//        if (certifications.isEmpty())
//            return true;
//
//        // ?????? ?????????????????? ??????????????? 6??????(21600???)????????? ?????? ?????? ??????
//        return ChronoUnit.SECONDS.between(certifications.get(0).getRegistDt(), LocalDateTime.now()) > 21600;
//    }
}
