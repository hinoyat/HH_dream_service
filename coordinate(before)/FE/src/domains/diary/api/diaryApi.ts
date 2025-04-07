import api from '@/apis/apiClient';
import {
  DiaryCreateUpdateRequest,
  DiaryData,
} from '@/domains/diary/Types/diary.types';

export const diaryApi = {
  // 일기 생성
  createDiary: async (diaryData: DiaryCreateUpdateRequest) => {
    return await api.post('/diaries', diaryData);
  },

  // 일기 목록 조회 (내꺼)
  getDiaries: async () => {
    return await api.get('/diaries/me');
  },

  // 일기 목록 조회 (다른사람꺼)
  getUserDiaries: async (userSeq: number) => {
    return await api.get(`/diaries/users/${userSeq}`);
  },

  // 일기 개별 조회
  getDiaryById: async (diarySeq: number) => {
    return await api.get(`/diaries/${diarySeq}`);
  },

  // 일기 수정
  updateDiary: async (
    diarySeq: number,
    diaryData: DiaryCreateUpdateRequest
  ) => {
    return await api.put(`/diaries/${diarySeq}`, diaryData);
  },

  // 일기 삭제
  deleteDiary: async (diarySeq: number) => {
    return await api.delete(`/diaries/${diarySeq}`);
  },

  // 좋아요 토글
  toggleLike: async (diarySeq: number) => {
    return await api.patch(`/diaries/${diarySeq}/like`);
  },
  getUniverseData: async () => {
    return await api.get('/diaries/universe');
  },
};
