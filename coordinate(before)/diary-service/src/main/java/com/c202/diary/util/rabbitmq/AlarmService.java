package com.c202.diary.util.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final RabbitTemplate rabbitTemplate;

    public void sendDiaryCreatedAlarm(Integer userSeq, String diaryTitle, Integer diarySeq) {
        AlarmMessageDto alarmMessage = AlarmMessageDto.builder()
                .userSeq(userSeq)
                .content("새로운 일기 \"" + diaryTitle + "\"가 작성되었습니다.")
                .type("DIARY_CREATED")
                .diarySeq(diarySeq)
                .requiresPersistence(false)
                .build();

        log.info("일기 생성 알림 발송: {}", alarmMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                alarmMessage
        );
    }

    public void sendVideoCreatedAlarm(Integer userSeq, String diaryTitle, Integer diarySeq) {
        AlarmMessageDto alarmMessage = AlarmMessageDto.builder()
                .userSeq(userSeq)
                .content("일기 \"" + diaryTitle + "\"의 동영상이 생성이 완료되었습니다. 일기를 보러 가실래요?")
                .type("VIDEO_CREATED")
                .diarySeq(diarySeq)
                .requiresPersistence(true)
                .build();

        log.info("동영상 생성 알림 발송: {}", alarmMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                alarmMessage
        );
    }

    public void sendVideoFailedAlarm(Integer userSeq, String diaryTitle, Integer diarySeq) {
        AlarmMessageDto alarmMessage = AlarmMessageDto.builder()
                .userSeq(userSeq)
                .content("일기 \"" + diaryTitle + "\"의 동영상 생성에 실패했어요.")
                .type("VIDEO_CREATED_FAILED")
                .diarySeq(diarySeq)
                .requiresPersistence(true)
                .build();

        log.info("동영상 생성 실패 알림 발송: {}", alarmMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                alarmMessage
        );
    }

    public void sendDiaryLikeAlarm(Integer userSeq, String diaryTitle, Integer diarySeq) {
        AlarmMessageDto alarmMessage = AlarmMessageDto.builder()
                .userSeq(userSeq)
                .content("누군가가 \"" + diaryTitle + "\"일기에 좋아요를 눌렀어요.")
                .type("LIKE_CREATED")
                .diarySeq(diarySeq)
                .requiresPersistence(true)
                .build();

        log.info("좋아요 생성 알림 발송: {}", alarmMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                alarmMessage
        );
    }
}