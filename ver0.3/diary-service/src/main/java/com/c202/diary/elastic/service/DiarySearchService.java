package com.c202.diary.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.c202.diary.elastic.document.DiaryDocument;
import com.c202.diary.elastic.model.request.DiarySearchRequestDto;
import com.c202.diary.elastic.model.response.DiarySearchListResponseDto;
import com.c202.diary.elastic.repository.DiarySearchRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.tag.model.response.TagResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiarySearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final DiarySearchRepository diarySearchRepository;
    private final EmotionRepository emotionRepository;

    public List<DiarySearchListResponseDto> searchDiaries(DiarySearchRequestDto requestDto, Integer userSeq) {
        // 쿼리 빌더 생성
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1. 삭제되지 않은 일기만 검색
        boolQueryBuilder.must(Query.of(q -> q
                .term(t -> t
                        .field("isDeleted")
                        .value("N")
                )
        ));

        // 2. 현재 사용자만 검색 OR 공개된 일기만 검색
        if (requestDto.isCurrentUserOnly()) {
            // 현재 사용자의 일기만 검색
            boolQueryBuilder.must(Query.of(q -> q
                    .term(t -> t
                            .field("userSeq")
                            .value(userSeq)
                    )
            ));
        } else {
            // 공개된 일기만 검색
            boolQueryBuilder.must(Query.of(q -> q
                    .term(t -> t
                            .field("isPublic")
                            .value("Y")
                    )
            ));
        }

        // 3. 키워드 검색 조건 설정
        if (requestDto.getKeyword() != null && !requestDto.getKeyword().isEmpty()) {
            BoolQuery.Builder keywordQueryBuilder = new BoolQuery.Builder();

            if (requestDto.isSearchTitle()) {
                keywordQueryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field("title")
                                .query(requestDto.getKeyword())
                        )
                ));
            }

            if (requestDto.isSearchContent()) {
                keywordQueryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field("content")
                                .query(requestDto.getKeyword())
                        )
                ));
            }

            if (requestDto.isSearchTag()) {
                keywordQueryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field("tags")
                                .query(requestDto.getKeyword())
                        )
                ));
            }

            boolQueryBuilder.must(Query.of(q -> q.bool(keywordQueryBuilder.build())));
        }

        // 쿼리 생성
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .build();

        // 검색 실행
        SearchHits<DiaryDocument> searchHits = elasticsearchOperations.search(
                searchQuery, DiaryDocument.class);

        // 결과 변환
        List<DiarySearchListResponseDto> result = new ArrayList<>();
        for (SearchHit<DiaryDocument> hit : searchHits) {
            DiaryDocument doc = hit.getContent();

            // 감정 이름 조회
            String emotionName = "";
            if (doc.getEmotionSeq() != null) {
                emotionName = emotionRepository.findById(doc.getEmotionSeq())
                        .map(Emotion::getName)
                        .orElse("");
            }

            // 태그를 DTO로 변환
            List<TagResponseDto> tagDtos = doc.getTags().stream()
                    .map(tagName -> TagResponseDto.builder().name(tagName).build())
                    .collect(Collectors.toList());

            DiarySearchListResponseDto diaryDto = DiarySearchListResponseDto.fromDocument(doc, emotionName, tagDtos);
            result.add(diaryDto);
        }

        return result;
    }
}