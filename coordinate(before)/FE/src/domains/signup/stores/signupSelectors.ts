import { RootState } from '@/stores/store';

export const selectUsername = (state: RootState) => state.signup.username;

export const selectUsernameMessage = (state: RootState) =>
  state.signup.usernameMessage;

export const selectUsernameStatus = (state: RootState) =>
  state.signup.usernameStatus;

export const selectNickname = (state: RootState) => state.signup.nickname;

export const selectNicknameMessage = (state: RootState) =>
  state.signup.nicknameMessage;

export const selectNicknameStatus = (state: RootState) =>
  state.signup.nicknameStatus;

export const selectPassword = (state: RootState) => state.signup.password;

export const selectPasswordMessage = (state: RootState) =>
  state.signup.passwordMessage;

export const selectPasswordStatus = (state: RootState) =>
  state.signup.passwordStatus;

export const selectIconSeq = (state: RootState) => state.signup.iconSeq;

export const selectBirthDate = (state: RootState) => state.signup.birthDate;

export const selectBirthDateMessage = (state: RootState) =>
  state.signup.birthDateMessage;

export const selectBirthDateStatus = (state: RootState) =>
  state.signup.birthDateStatus;

export const selectConfirmPassword = (state: RootState) =>
  state.signup.confirmPassword;

export const selectConfirmPasswordMessage = (state: RootState) =>
  state.signup.confirmPasswordMessage;

export const selectConfirmPasswordStatus = (state: RootState) =>
  state.signup.confirmPasswordStatus;
