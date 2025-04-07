import { tagApi } from '@/domains/diary/api/tagApi';
import React, { useState, FC, useRef, useEffect } from 'react';

interface Tag {
  tagSeq: number;
  name: string;
}

interface DiaryTagsProps {
  initialTags: (Tag | string)[];
  isEditing?: boolean;
  onTagsChange?: (tags: string[]) => void;
  emotionName?: string;
}

const DiaryTags: FC<DiaryTagsProps> = ({
  initialTags,
  isEditing = false,
  onTagsChange,
  emotionName,
}) => {
  // string[] 배열을 Tag[] 형태로 변환
  // initialTags가 api에서 온 경우에는 Tag[]로, 사용자가 입력했을 경우에는 string[]으로 오기 때문에
  const processedInitialTags = Array.isArray(initialTags)
    ? initialTags.map((tag) =>
        typeof tag === 'string' ? { tagSeq: 0, name: tag } : tag
      )
    : [];

  // ---------------------- 상태관리 ---------------------- //
  const [tags, setTags] = useState<string[]>(
    processedInitialTags.map((tag) =>
      typeof tag === 'string' ? tag : tag.name
    )
  );
  const [inputValue, setInputValue] = useState<string>('');
  const [historyTags, setHistoryTags] = useState<Tag[]>([]);
  const inputContainerRef = useRef<HTMLDivElement>(null);

  //--------------------------------------------------------//

  // 상수 정의
  const MAX_TAG_LENGTH = 5;
  const MAX_TAGS = 3;

  // 최근 사용 태그 가져오기
  useEffect(() => {
    const fetchRecentTags = async () => {
      // console.log('태그 api 호출 시작 ♥️♥️♥️♥️♥️♥️♥️');
      try {
        const response = await tagApi.getRecentTags();
        // console.log('최근태그응답====>', response.data);

        if (response.data && response.data.data) {
          setHistoryTags(response.data.data);
        } else {
          setHistoryTags([]);
        }
      } catch (error) {
        console.error('최근 태그 가져오기 실패 : ', error);
        // 에러 시 빈 배열 사용
        setHistoryTags([]);
      }
    };

    if (isEditing) {
      fetchRecentTags();
    }
  }, [isEditing]);

  // 사용자가 태그를 수정, 삭제, 변경할 수 있음
  if (isEditing) {
    return (
      <div className="flex flex-col gap-1 relative">
        <h1 className="text-white text-base font-semibold">태그</h1>
        <div className="flex flex-col border border-white p-2 rounded w-full relative">
          {/* 선택된 태그 영역 */}
          <div className="flex flex-wrap gap-1 mb-2">
            {emotionName && ( // emotionName이 존재할 때만 렌더링
              <div className="text-[#06061E] font-semibold bg-white/90 px-2 py-1 rounded-4xl flex items-center justify-center gap-1">
                <div className="flex items-center justify-center gap-1 text-sm w-15">
                  <p>{emotionName}</p>
                </div>
              </div>
            )}

            {tags.map((tag, index) => (
              <div
                key={index}
                className="text-white border border-white/80 px-2 py-1 rounded-4xl flex items-center justify-center gap-1">
                <div className="flex items-center justify-center gap-3 text-sm ">
                  <p>{tag}</p>
                  <button
                    onClick={() => {
                      const newTags = tags.filter((t, i) => i !== index);
                      setTags(newTags);
                      onTagsChange?.(newTags);
                    }}
                    className="text-white hover:text-gray-400">
                    x
                  </button>
                </div>
              </div>
            ))}
          </div>
          {/* 입력창 */}
          <div
            ref={inputContainerRef}
            className="relative w-full h-8">
            <input
              type="text"
              className="bg-transparent outline-none text-white w-full text-sm"
              value={inputValue}
              onChange={(e) => {
                if (e.target.value.length <= MAX_TAG_LENGTH) {
                  setInputValue(e.target.value);
                }
              }}
              onKeyDown={(e) => {
                if (
                  e.key === 'Enter' &&
                  inputValue.trim() &&
                  tags.length < MAX_TAGS
                ) {
                  const newTags = [...tags, inputValue.trim()];
                  setTags(newTags);
                  onTagsChange?.(newTags);
                  setInputValue('');
                }
              }}
              placeholder={
                tags.length < MAX_TAGS
                  ? '태그를 입력하세요 (최대 5글자)'
                  : '태그는 최대 3개까지 입력 가능합니다'
              }
              disabled={tags.length >= MAX_TAGS}
            />
          </div>
        </div>
        {/* history 태그 */}
        <div className="flex flex-wrap gap-2 text-white italic mt-2 text-sm px-1">
          {historyTags.map((tag) => (
            <p
              key={tag.tagSeq}
              className={`cursor-pointer hover:underline ${tags.length >= MAX_TAGS ? 'opacity-50' : ''}`}
              onClick={() => {
                if (tags.length < MAX_TAGS && !tags.includes(tag.name)) {
                  const newTags = [...tags, tag.name];
                  setTags(newTags);
                  onTagsChange?.(newTags);
                }
              }}>
              # {tag.name}
            </p>
          ))}
        </div>
      </div>
    );
  }

  // 읽기 전용 모드
  return (
    <div className="flex flex-wrap gap-3 mt-4">
      <div className="text-[#06061E] px-2 py-1 rounded-4xl  bg-white/90 font-semibold">
        <div className="flex items-center justify-center gap-1 text-sm w-15">
          <p>{emotionName}</p>
        </div>
      </div>

      {/* 기존 태그들 표시 */}
      {initialTags.map((tag, index) => {
        const tagName = typeof tag === 'string' ? tag : tag.name;
        const tagSeq = typeof tag === 'string' ? index : tag.tagSeq;

        return (
          <div
            key={tagSeq}
            className="text-white border border-white/80 px-2 py-1 rounded-4xl">
            <div className="flex items-center justify-center gap-1 text-sm w-15">
              <p>{tagName}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default DiaryTags;
