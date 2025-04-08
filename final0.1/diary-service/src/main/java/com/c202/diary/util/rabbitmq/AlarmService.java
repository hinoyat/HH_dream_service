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
                .content("꿈 " + diaryTitle + "이 기록되었어요. 영상은 작업 중이니 금방 완성될 거예요!")
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
                .content("축하해요! " + diaryTitle + "의 꿈 영상이 완성됐어요. 지금 바로 감상해보세요!")
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
                .content(diaryTitle + "의 꿈 영상 제작에 문제가 생겼어요. 잠시 후 다시 확인해 주세요.")
                .type("VIDEO_CREATED_FAILED")
                .diarySeq(diarySeq)
                .requiresPersistence(false)
                .build();

        log.info("동영상 생성 실패 알림 발송: {}", alarmMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                alarmMessage
        );
    }

    public void sendDiaryLikeAlarm(Integer userSeq, String diaryTitle, Integer diarySeq, String nickname) {
        AlarmMessageDto alarmMessage = AlarmMessageDto.builder()
                .userSeq(userSeq)
                .content(nickname + "님이 꿈" + diaryTitle + "에 좋아요를 눌렀어요!")
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