// 태그
export interface Tag {
  tagSeq: number;
  name: string;
}

// api 요청용 - 일기 생성/수정 시 사용
export interface DiaryCreateUpdateRequest {
  title: string;
  content: string;
  dreamDate: string;
  isPublic: string; // 'Y' 또는 'N'
  mainEmotion: string;
  tags: string[]; // 항상 문자열 배열로만 정의
}

// api 응답용 - 백에서 보내주는 일기 데이터
export interface DiaryResponse {
  diarySeq: number;
  userSeq: number;
  title: string;
  content: string;
  videoUrl: string | null;
  dreamDate: string;
  createdAt: string;
  updatedAt: string;
  isPublic: string;
  tags: Tag[];
  likeCount: number;
  hasLiked: boolean;
  x: number;
  y: number;
  z: number;
  emotionSeq: number;
  emotionName: string | null;
  connectedDiaries: number[] | null;
}

// 프론트에서 사용하는 일기 데이터
// DiaryResponse + DiaryCreateUpdateRequest
export interface DiaryData {
  diarySeq?: number; // 생성 시에는 없을 수 있음
  userSeq?: number; // 생성 시에는 없을 수 있음
  title: string;
  content: string;
  videoUrl?: string | null;
  dreamDate: string;
  isPublic: string; // 'Y' 또는 'N'
  mainEmotion?: string;
  emotionName?: string;
  emotionSeq?: number;
  tags?: string[] | Tag[]; // 입력 시에는 string[], 출력 시에는 Tag[]

  createdAt?: string;
  updatedAt?: string;

  likeCount?: number;
  hasLiked?: boolean;
  x?: number;
  y?: number;
  z?: number;
  connectedDiaries?: number[] | null;
}

// 영상 생성 요청
export interface VideoCreateRequest {
  diary_pk: number;
  content: string;
}

// 꿈해몽 요청 인터페이스
export interface DreamSolveRequest {
  inputContent: string;
}

// 꿈해몽 응답 인터페이스
export interface DreamSolveResponse {
  dreamMeaningSeq: number;
  userSeq: number;
  diarySeq: number;
  isGood: string;
  inputContent: string;
  resultContent: string;
  createdAt: string;
}
