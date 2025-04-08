package com.c202.diary.like.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.like.entity.DiaryLike;
import com.c202.diary.like.repository.DiaryLikeRepository;
import com.c202.diary.util.rabbitmq.AlarmService;
import com.c202.exception.types.BadRequestException;
import com.c202.exception.types.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryLikeServiceImpl implements DiaryLikeService {

    private final WebClient.Builder webClientBuilder;
    private final DiaryLikeRepository diaryLikeRepository;
    private final DiaryRepository diaryRepository;
    private final AlarmService alarmService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Transactional
    @Override
    public String toggleLike(Integer diarySeq, Integer userSeq) {
        if (diarySeq == null || userSeq == null) {
            throw new BadRequestException("일기 또는 사용자 정보가 유효하지 않습니다.");
        }

        Diary diary = diaryRepository.findByDiarySeqAndIsDeleted(diarySeq, "N")
                .orElseThrow(() -> new NotFoundException("해당 일기를 찾을 수 없습니다."));

        Optional<DiaryLike> existingLike =
                diaryLikeRepository.findByDiarySeqAndUserSeq(diarySeq, userSeq);

        String nickname = webClientBuilder
                .baseUrl("http://user-service")
                .build()
                .get()
                .uri("/api/users/nickname")
                .header("X-User-Seq", String.valueOf(userSeq))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (existingLike.isPresent()) {
            diaryLikeRepository.delete(existingLike.get());
            return "좋아요 취소 완료";
        } else {
            String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            DiaryLike diaryLike = DiaryLike.builder()
                    .diarySeq(diarySeq)
                    .userSeq(userSeq)
                    .createdAt(now)
                    .build();
            diaryLikeRepository.save(diaryLike);

            if (diary.getUserSeq() != userSeq) {
                alarmService.sendDiaryLikeAlarm(
                        diary.getUserSeq(),
                        diary.getTitle(),
                        diary.getDiarySeq(),
                        nickname
                );
            }
            return "좋아요 추가 완료";
        }
    }


    @Override
    public Integer getLikeCount(Integer diarySeq) {
        return diaryLikeRepository.countByDiarySeq(diarySeq);
    }

    @Override
    public boolean hasUserLiked(Integer diarySeq, Integer userSeq) {
        return diaryLikeRepository.findByDiarySeqAndUserSeq(diarySeq, userSeq).isPresent();
    }
}