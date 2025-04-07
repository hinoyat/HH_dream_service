export interface UserpageState {
  userSeq: number;
  username: string;
  nickname: string;
  birthDate: string;
  introduction: string | null;
  iconSeq: number;
  isSubscribed: string;
}

export interface visitResponse {
  data: UserpageState;
}
