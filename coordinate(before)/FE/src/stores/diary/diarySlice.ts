import { DiaryData } from '@/domains/diary/Types/diary.types';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

// 일기 인터페이스 정의
// export interface DiaryData {
//   diarySeq?: number;
//   title: string;
//   content: string;
//   dreamDate: string;
//   isPublic: string;
//   mainEmotion: string;
//   tags: string[];
// }

// 일기 상태 인터페이스 정의
interface DiaryState {
  diaries: DiaryData[];
  currentDiary: DiaryData | null;
  showDiaryModal: boolean; // 일기 모달 표시 상태
  selectedDiarySeq: number | null; // 선택된 일기 ID
  loading: boolean;
  error: string | null;
}

// 초기 상태 정의
const initialState: DiaryState = {
  diaries: [],
  currentDiary: null,
  showDiaryModal: false,
  selectedDiarySeq: null,
  loading: false,
  error: null,
};

// 슬라이스 생성
const diarySlice = createSlice({
  name: 'diary',
  initialState,
  reducers: {
    // 일기 목록 설정
    setDiaries: (state, action: PayloadAction<DiaryData[]>) => {
      state.diaries = action.payload;
    },
    // 새 일기 추가
    addDiary: (state, action: PayloadAction<DiaryData>) => {
      state.diaries.push(action.payload);
    },

    // 현재 편집 중인 일기 설정
    setCurrentDiary: (state, action: PayloadAction<DiaryData | null>) => {
      state.currentDiary = action.payload;
    },
    // 일기 업데이트
    updateDiary: (state, action: PayloadAction<DiaryData>) => {
      const index = state.diaries.findIndex(
        (diary) => diary.diarySeq === action.payload.diarySeq
      );
      if (index !== -1) {
        state.diaries[index] = action.payload;
      }
      if (state.currentDiary?.diarySeq === action.payload.diarySeq) {
        state.currentDiary = action.payload;
      }
    },

    // 일기 삭제
    removeDiary: (state, action: PayloadAction<number>) => {
      state.diaries = state.diaries.filter(
        (diary) => diary.diarySeq !== action.payload
      );
      if (state.currentDiary?.diarySeq === action.payload) {
        state.currentDiary = null;
      }
    },

    // 로딩 상태 설정
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    // 에러 설정
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
    showDiaryModal: (state, action: PayloadAction<number>) => {
      state.showDiaryModal = true;
      state.selectedDiarySeq = action.payload;
    },
    hideDiaryModal: (state) => {
      state.showDiaryModal = false;
      state.selectedDiarySeq = null;
    },
  },
});

// 액션 생성자 내보내기
export const {
  setDiaries,
  addDiary,
  setCurrentDiary,
  updateDiary,
  removeDiary,
  setLoading,
  setError,
  showDiaryModal,
  hideDiaryModal,
} = diarySlice.actions;

// 리듀서 내보내기
export default diarySlice.reducer;
