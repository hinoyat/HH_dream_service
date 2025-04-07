export interface MusicState {
  defaultBackgroundTrack: string          // 기본 배경음악 경로
  customBackgroundTrack: string | null;   // 사용자가 설정한 배경음악 경로
  isPlaying: boolean;                     // 음악이 현재 재생 중인지 여부
  isPlaylistPlaying: boolean;             // 플레이리스트 음악이 재생 중인지 여부
  volume: number;                         // 음악 볼륨 (0.0 ~ 1.0)
  audioLoaded: boolean;                   // 오디오가 로드됐는지 여부
  currentTrack: string | null;            // 현재 트랙 경로
}
