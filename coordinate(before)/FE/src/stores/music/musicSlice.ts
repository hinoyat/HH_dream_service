import { MusicState } from '@/stores/music/music.types';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import UniverseBgm from '@/assets/music/UniverseBgm.mp3'

// 로컬 스토리지에서 유저의 음악 설정을 불러옴
const getSavedMusicState = (): { isPlaying: boolean; volume: number, customBackgroundTrack: string | null; } => {
  try {
    // 저장된 상태 확인
    const savedState = localStorage.getItem('musicSettings');
    if (savedState) {
      // 저장된 설정이 있으면 파싱하여 변환
      return JSON.parse(savedState);
    }
  } catch (error) {
    console.error('음악 불러오기 실패ㅠㅠㅠㅠㅠ:', error);
  }
  // 저장된 설정이 없으면 기본값=음악 켜진상태
  return { isPlaying: true, volume: 0.5, customBackgroundTrack: null };
};

// ------------------------------------------------------------------------------------

// 저장된 설정 불러오기
const { isPlaying, volume, customBackgroundTrack } = getSavedMusicState();

// 초기 상태 설정
const initialState: MusicState = {
  defaultBackgroundTrack: UniverseBgm,
  customBackgroundTrack,
  isPlaying,
  isPlaylistPlaying: false,
  volume,
  audioLoaded: false,
  currentTrack: null, 
};

const musicSlice = createSlice({
  name: 'music',
  initialState,
  reducers: {
    // 음악 재생 상태 토글 (재생 <-> 일시정지)
    toggleMusic: (state) => {
      state.isPlaying = !state.isPlaying;
    },

    // 볼륨 설정
    setVolume: (state, action: PayloadAction<number>) => {
      state.volume = action.payload;
    },

    // 오디오 로드 상태 설정
    setAudioLoaded: (state, action: PayloadAction<boolean>) => {
      state.audioLoaded = action.payload;
    },

    // 사용자 정의 배경음악 설정
    setCustomBackgroundTrack: (state, action: PayloadAction<string | null>) => {
      state.customBackgroundTrack = action.payload;
    },

    // 플레이리스트 재생 상태 설정
    setPlaylistPlaying: (state, action: PayloadAction<boolean>) => {
      state.isPlaylistPlaying = action.payload;
    },

    // 배경음악 초기화 (기본값으로 리셋)
    resetBackgroundMusic: (state) => {
      state.customBackgroundTrack = null;
    }
  },
});

export const {
  toggleMusic,
  setVolume,
  setAudioLoaded,
  setCustomBackgroundTrack,
  setPlaylistPlaying,
  resetBackgroundMusic
} = musicSlice.actions;

export default musicSlice.reducer;
