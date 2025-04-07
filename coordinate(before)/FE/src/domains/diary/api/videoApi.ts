import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import api from '@/apis/apiClient';
import { VideoCreateRequest } from '@/domains/diary/Types/diary.types';

interface JwtPayload {
  userSeq: number;
}

export const videoApi = {
  createVideo: async (videoData: VideoCreateRequest) => {
    // const token = localStorage.getItem('accessToken') || '';
    // const jwtTMP: JwtPayload = jwtDecode(token);

    // return await axios.post('/ai/test-sample-video', videoData, {
    return await api.post(
      '/ai/test-sample-video', // 요청 확인용
      // '/ai/create-video', // 이걸로 보내야 영상 생성됨
      videoData
      // headers: {

      //   Authorization: `Bearer ${token}`,
      //   'X-User-Seq': jwtTMP.userSeq,
      // },
    );
  },
};
