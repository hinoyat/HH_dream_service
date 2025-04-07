import { RootState } from "@/stores/store";

// 그래프 데이터 가져오기
export const selectTwoWeeksData = (state: RootState) => state.dashboard.twoWeeksData;
export const selectMonthlyData = (state: RootState) => state.dashboard.monthlyData;

// 로딩 상태 가져오기
export const selectLoading = (state: RootState) => state.dashboard.loading;

// 오류 상태 가져오기
export const selectError = (state: RootState) => state.dashboard.error;

// 주요 감정 가져오기
export const selectDominantEmotion = (state: RootState) => state.dashboard.dominantEmotion;