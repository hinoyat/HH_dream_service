export interface EmotionCountItem {
    emotion: string;
    count: number;
  }

export interface DashboardEmotionData {
  periodDays: number;
  data: EmotionCountItem[];
}

export interface EmotionDataResponse {
    data: DashboardEmotionData;
}

export interface DashboardGraphState {
    twoWeeksData: Record<string, number> | null;    // 14일 데이터
    monthlyData: Record<string, number> | null;     // 30일 데이터
    loading: boolean;
    error: string | null;
    dominantEmotion : string | null;                // 가장 많이 꾼 꿈(주 감정)
}