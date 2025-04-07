// 일기 생성 관련 코드

import DiaryComponent from '@/domains/diary/modals/DiaryComponent';

// 일기 생성 인터페이스
interface CreateDiaryRequest {
  title: string;
  content: string;
  dreamDate: string;
  isPublic: string;
  mainEmotion: string;
  tags: string[];
}

// 일기 수정 인터페이스
interface DiaryResponse {
  diarySeq: number;
  userSeq: number;
  title: string;
  content: string;
  videoUrl: string | null;
  dreamDate: string;
  createdAt: string;
  updatedAt: string;
  isPublic: string;
  tags: Array<{ tagSeq: number; name: string }>;
  x: number;
  y: number;
  z: number;
  emotionSeq: number;
  emotionName: string;
  connectedDiaries: number[];
}

// ---- any인 부분 타입 찾아야 함 --- //
interface UniverseDiaryFormProps {
  showForm: boolean;
  isEditing: boolean;
  selectedEntry: DiaryResponse | null;
  controlsRef: React.RefObject<any>;
  onClose: () => void;
  onCreateDiary: (newEntry: CreateDiaryRequest) => void;
  onUpdateDiary: (
    entryId: number,
    updateData: Partial<CreateDiaryRequest>
  ) => void;
}

const UniverseDiaryForm: React.FC<UniverseDiaryFormProps> = ({
  showForm,
  isEditing,
  selectedEntry,
  controlsRef,
  onClose,
  onCreateDiary,
  onUpdateDiary,
}) => {
  if (!showForm) return null;

  return (
    <DiaryComponent
      onClose={(newDiaryData) => {
        if (!newDiaryData) {
          onClose();
          return;
        }

        // 새 일기 작성
        // 제목, 날짜, 콘텐츠, 감정태그, 태그, 공개범위, 영상(선택사항-만들건지안만들건지)
        if (!isEditing) {
          const createRequest: CreateDiaryRequest = {
            title: newDiaryData.title || '',
            content: newDiaryData.content || '',
            dreamDate:
              newDiaryData.dreamDate ||
              new Date().toISOString().slice(0, 10).replace(/-/g, ''),
            isPublic: newDiaryData.isPublic || 'N',
            mainEmotion: newDiaryData.mainEmotion || '',
            tags: newDiaryData.tags || [],
          };

          // 생성 요청 객체 전달
          onCreateDiary(createRequest);
        }

        // 일기 수정 완료
        else if (isEditing && selectedEntry) {
          // 수정 데이터 객체 생성
          const updateRequest = {
            title: newDiaryData.title,
            content: newDiaryData.content,
            tags: newDiaryData.tags,
            isPublic: newDiaryData.isPublic,
            mainEmotion: newDiaryData.mainEmotion,
            dreamDate: newDiaryData.dreamDate,
          };

          // 수정 요청 전달
          onUpdateDiary(selectedEntry.diarySeq, updateRequest);
        }
        onClose();
      }}
      // DiaryComponent에 전달할거
      isEditing={isEditing}
      diaryData={
        isEditing && selectedEntry
          ? {
              diarySeq: selectedEntry.diarySeq,
              title: selectedEntry.title,
              content: selectedEntry.content,
              tags: selectedEntry.tags.map((tag) => tag.name), // 태그 객체에서 이름만 추출,
              isPublic: selectedEntry.isPublic,
              mainEmotion: selectedEntry.emotionName,
            }
          : undefined
      }
    />
  );
};

export default UniverseDiaryForm;

// {showForm && (
//   <DiaryComponent
//     onClose={(newDiaryData) => {
//       setShowForm(false);

//       // 새 일기 작성 완료
//       if (!isEditing && newDiaryData) {
//         // 변경: 일기 추가 로직을 커스텀 훅 사용으로 대체
//         const newEntry = addEntry({
//           title: newDiaryData.title || '',
//           content: newDiaryData.content || '',
//           tags: newDiaryData.tags || [],
//           isPublic: newDiaryData.isPublic || false,
//           video_url: newDiaryData.dream_video,
//         });

//         // 새 별 ID 설정 (하이라이트 효과를 위해)
//         setNewStarId(newEntry.diary_seq);

//         // 카메라를 새로운 별 위치로 이동
//         if (controlsRef.current) {
//           // 카메라가 바라볼 목표 위치 설정
//           controlsRef.current.target.set(
//             newEntry.position.x,
//             newEntry.position.y,
//             newEntry.position.z
//           );

//           // 카메라 위치를 새로운 별 근처로 이동 (별에서 약간 떨어진 위치)
//           const distance = 20; // 별과의 거리
//           const cameraPos = {
//             x: newEntry.position.x * 1.2,
//             y: newEntry.position.y * 1.2,
//             z: newEntry.position.z * 1.2,
//           };

//           // 카메라 업데이트
//           controlsRef.current.update();
//         }

//         // 일정 시간 후 하이라이트 효과 제거
//         setTimeout(() => {
//           setNewStarId(null);
//         }, 10000); // 10초 동안 하이라이트 효과 유지
//       }

//       // 일기 수정 완료
//       else if (isEditing && newDiaryData && selectedEntry) {
//         // 변경: 일기 수정 로직을 커스텀 훅 사용으로 대체
//         editEntry(selectedEntry.diary_seq, {
//           title: newDiaryData.title,
//           content: newDiaryData.content,
//           tags: newDiaryData.tags,
//           isPublic: newDiaryData.isPublic,
//           video_url: newDiaryData.dream_video,
//         });

//         // 선택된 항목 초기화
//         setSelectedEntry(null);
//         setSelectedPosition(null);
//       }

//       // 수정 모드 종료
//       setIsEditing(false);
//     }}
//     // 수정 모드 및 현재 선택된 일기 데이터 전달
//     isEditing={isEditing}
//     diaryData={
//       isEditing && selectedEntry
//         ? {
//             id: selectedEntry.diary_seq,
//             title: selectedEntry.title,
//             content: selectedEntry.content,
//             tags: selectedEntry.tags,
//             isPublic: selectedEntry.is_public === 'Y', // 변경: 'Y'/'N'을 불리언으로 변환
//           }
//         : undefined
//     }
//   />
// )}
