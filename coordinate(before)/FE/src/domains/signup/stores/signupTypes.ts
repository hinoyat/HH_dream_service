export interface SignupState {
  username: string;
  usernameStatus: 'idle' | 'checking' | 'available' | 'unavailable' | 'invalid';
  usernameMessage: string;
  nickname: string;
  nicknameStatus: 'idle' | 'checking' | 'available' | 'unavailable' | 'invalid';
  nicknameMessage: string;
  password: string;
  confirmPassword: string;
  confirmPasswordMessage: string;
  confirmPasswordStatus: 'checking' | 'available' | 'unavailable' | 'invalid';
  passwordStatus: 'idle' | 'checking' | 'available' | 'unavailable' | 'invalid';
  passwordMessage: string;
  iconSeq: number;
  birthDate: string;
  birthDateMessage: string;
  birthDateStatus: 'checking' | 'available' | 'unavailable' | 'invalid';
}

export interface SignupForm {
  username: string;
  password: string;
  nickname: string;
  iconSeq: number;
  birthDate: string;
}

export interface SignupResponse {
  data: SignupResponseData;
}

export interface SignupResponseData {
  userSeq: number;
  username: string;
  password: string;
  nickname: string;
  iconSeq: number;
  birthDate: string;
}
