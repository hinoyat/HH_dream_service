import { RootState } from '@/stores/store';

// 음악이 현재 재생 중인지 여부 (true/false)
export const selectMusicPlaying = (state: RootState) => state.music.isPlaying;

// 음악 볼륨
export const selectMusicVolume = (state: RootState) => state.music.volume;

// 오디오 파일이 로드 되었는가
export const selectAudioLoaded = (state: RootState) => state.music.audioLoaded;

// 현재 음악의 파일 경로
export const selectCurrentTrack = (state: RootState) =>
  state.music.currentTrack;

// 기본 배경음악 경로
export const selectDefaultBackgroundTrack = (state: RootState) =>
  state.music.defaultBackgroundTrack;

// 사용자 정의 배경음악 경로
export const selectCustomBackgroundTrack = (state: RootState) =>
  state.music.customBackgroundTrack;

// 플레이리스트 재생 중인지 여부
export const selectPlaylistPlaying = (state: RootState) =>
  state.music.isPlaylistPlaying;

// 활성화된 배경음악 (사용자 정의 있으면 그것, 없으면 기본값)
export const selectActiveBackgroundTrack = (state: RootState) =>
  state.music.customBackgroundTrack || state.music.defaultBackgroundTrack;