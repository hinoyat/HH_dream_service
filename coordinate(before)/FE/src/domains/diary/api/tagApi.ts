import api from '@/apis/apiClient';

export const tagApi = {
  // 최근 사용 태그 가져오기
  getRecentTags: async () => {
    return await api.get('/tags/recent');
  },
};
