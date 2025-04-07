import { RootState } from '../store';

// 사용자 정보 가져오기
export const selectUser = (state: RootState) => state.auth.user;

// 로그인 여부 확인
export const selectIsAuthenticated = (state: RootState) =>
  state.auth.isAuthenticated;

// 액세스 토큰 가져오기 (API 호출 시 사용 가능)
export const selectAccessToken = (state: RootState) => state.auth.accessToken;
