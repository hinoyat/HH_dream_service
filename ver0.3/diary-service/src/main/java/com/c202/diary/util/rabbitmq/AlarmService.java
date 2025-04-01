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

    public void sendDiaryCreatedAlarm(Integer userSeq, String diaryTitle) {
        AlarmMessageDto alarmMessage = AlarmMessageDto.builder()
                .userSeq(userSeq)
                .content("새로운 일기 \"" + diaryTitle + "\"가 작성되었습니다.")
                .type("DIARY_CREATED")
                .build();

        log.info("일기 생성 알림 발송: {}", alarmMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                alarmMessage
        );
    }
}