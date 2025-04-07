import axios from 'axios';
import { store } from '../stores/store';
import { logoutUser, updateToken } from '../stores/auth/authThunks';
import { setAccessToken } from '../stores/auth/authSlice';

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: '/api',
});

let isRefreshing = false;
let isLoggingOut = false;
let failedQueue: any[] = [];


const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (token) {
      prom.resolve(token);
    } else {
      prom.reject(error);
    }
  });
  failedQueue = [];
};


// 요청 인터셉터 - accessToken 자동 추가
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터 - 401/403 처리
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isOnLoginPage = window.location.pathname === '/login';
    const isRefreshRequest = originalRequest.url?.includes('/auth/refresh');

    // 공통 로그아웃 처리 함수
    const handleLogout = async () => {
      if (!isLoggingOut && !isOnLoginPage) {
        isLoggingOut = true;
        console.warn('[apiClient] 자동 로그아웃 처리');
        await store.dispatch(logoutUser());
        window.location.href = '/login';
      }
    };

    // 🔁 리프레시 요청 자체가 실패한 경우 -> 바로 로그아웃
    if (isRefreshRequest && (error.response?.status === 401 || error.response?.status === 403)) {
      console.warn('[apiClient] refresh 요청 실패 → 로그아웃 진행');
      await handleLogout();
      return Promise.reject(error);
    }

    // 🔁 accessToken 만료 → 토큰 재발급 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: (token: string) => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(api(originalRequest));
            },
            reject: (err: any) => reject(err),
          });
        });
      }

      isRefreshing = true;


      console.warn('[apiClient] 401 → accessToken 갱신 시도');

      try {
        const refreshResult = await store.dispatch(updateToken()).unwrap();

        const token = refreshResult.accessToken;

        localStorage.setItem('accessToken', token);
        store.dispatch(setAccessToken(token));
        processQueue(null, token);
        isRefreshing = false;
        originalRequest.headers.Authorization = `Bearer ${token}`;

        return api(originalRequest); // ✅ 기존 요청 재시도
      } catch {
        isRefreshing = false;
        await handleLogout();
        return Promise.reject(error);
      }
    }

    // 🔒 403 → 바로 로그아웃
    if (error.response?.status === 403  && !isOnLoginPage) {
      await handleLogout();
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

export default api;
