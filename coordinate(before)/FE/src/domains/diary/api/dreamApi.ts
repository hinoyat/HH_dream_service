import api from '@/apis/apiClient';
import { DreamSolveRequest } from '@/domains/diary/Types/diary.types';

export const dreamApi = {
  // 꿈해몽 생성
  createDreamMeaning: async (diarySeq: number, content: string) => {
    // console.log(`dreamApi - 꿈해몽 생성 API 요청 시작 (diarySeq: ${diarySeq})`);
    const requestData: DreamSolveRequest = {
      inputContent: content,
    };
    return await api.post(`/dream-meaning/${diarySeq}`, requestData);
  },

  // 꿈해몽조회
  getDreamMeaningById: async (diarySeq: number) => {
    return await api.get(`/dream-meaning/${diarySeq}`);
  },
};
