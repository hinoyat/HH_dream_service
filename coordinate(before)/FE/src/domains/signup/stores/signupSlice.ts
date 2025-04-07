import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { SignupState } from './signupTypes';
import { checkUsername, checkNickname, signupUser } from './signupThunks';

const initialState: SignupState = {
  username: '',
  usernameStatus: 'invalid',
  usernameMessage: '',
  nickname: '',
  nicknameStatus: 'invalid',
  nicknameMessage: '',
  password: '',
  confirmPassword: '',
  confirmPasswordMessage: '',
  confirmPasswordStatus: 'invalid',
  passwordMessage: '',
  passwordStatus: 'invalid',
  iconSeq: 0,
  birthDate: '',
  birthDateMessage: '',
  birthDateStatus: 'invalid',
};

const isValidBirthdate = (birthdate: string): boolean => {
  // 기본 형식 검사 (YYYYMMDD)
  const regex = /^(\d{4})(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])$/;

  if (!regex.test(birthdate)) {
    return false;
  }

  // 년, 월, 일 추출
  const year = parseInt(birthdate.substring(0, 4));
  const month = parseInt(birthdate.substring(4, 6));
  const day = parseInt(birthdate.substring(6, 8));

  // 현재 년도 계산
  const currentYear = new Date().getFullYear();

  // 년도 범위 체크 (현재 년도 - 100 ~ 현재 년도)
  if (year < currentYear - 100 || year > currentYear) {
    return false;
  }

  // 월별 일수 체크
  const daysInMonth = new Date(year, month, 0).getDate();
  if (day > daysInMonth) {
    return false;
  }

  return true;
};

const signupSlice = createSlice({
  name: 'signup',
  initialState,
  reducers: {
    setUsername: (state, action: PayloadAction<string>) => {
      state.username = action.payload;
      // 유효성 검사
      if (!action.payload) {
        state.usernameMessage = '';
        state.usernameStatus = 'invalid';
      } else if (!/^[a-zA-Z0-9]{5,12}$/.test(action.payload)) {
        state.usernameMessage = '아이디는 5자에서 12자 사이로 입력해주세요.';
        state.usernameStatus = 'invalid';
      } else {
        state.usernameMessage = '중복 확인을 해주세요.';
        state.usernameStatus = 'idle';
      }
    },
    setNickname: (state, action: PayloadAction<string>) => {
      state.nickname = action.payload;
      if (!action.payload) {
        state.nicknameMessage = '';
        state.nicknameStatus = 'invalid';
      } else if (2 > action.payload.length || action.payload.length > 10) {
        state.nicknameMessage = '닉네임은 2자에서 10자 사이로 입력해주세요.';
        state.nicknameStatus = 'invalid';
      } else {
        state.nicknameMessage = '중복 확인을 해주세요.';
        state.nicknameStatus = 'idle';
      }
    },
    setPassword: (state, action: PayloadAction<string>) => {
      state.password = action.payload;
      if (!action.payload) {
        state.passwordMessage = '';
        state.passwordStatus = 'invalid';
      } else if (
        !/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/.test(
          action.payload
        )
      ) {
        state.passwordMessage =
          '비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.';
        state.passwordStatus = 'invalid';
      } else {
        state.passwordMessage = '사용 가능한 비밀번호 입니다.';
        state.passwordStatus = 'available';
      }
    },
    setConfirmPassword: (state, action: PayloadAction<string>) => {
      state.confirmPassword = action.payload;
      if (!action.payload) {
        state.confirmPasswordMessage = '';
        state.confirmPasswordStatus = 'invalid';
      } else if (action.payload && action.payload === state.password) {
        state.confirmPasswordMessage = '비밀번호가 일치합니다.';
        state.confirmPasswordStatus = 'available';
      } else {
        state.confirmPasswordMessage = '비밀번호가 일치하지 않습니다.';
        state.confirmPasswordStatus = 'invalid';
      }
    },
    setIconSeq: (state, action: PayloadAction<number>) => {
      state.iconSeq = action.payload;
    },
    setBirthDate: (state, action: PayloadAction<string>) => {
      state.birthDate = action.payload;
      // 유효성 검사
      if (!action.payload) {
        state.birthDateMessage = '';
        state.birthDateStatus = 'invalid';
      } else if (!isValidBirthdate(action.payload)) {
        state.birthDateMessage = '유효하지 않는 생년월일 입니다.';
        state.birthDateStatus = 'invalid';
      } else {
        state.birthDateMessage = '유효한 생년월일 입니다.';
        state.birthDateStatus = 'available';
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(checkUsername.pending, (state) => {
        state.usernameStatus = 'checking';
      })
      .addCase(checkUsername.fulfilled, (state, action) => {
        if (action.payload === '이미 사용 중인 아이디입니다.') {
          state.usernameStatus = 'invalid';
          state.usernameMessage = '이미 사용 중인 아이디입니다.';
          state.username = '';
        } else {
          state.usernameStatus = 'available';
          state.usernameMessage = '사용 가능한 아이디 입니다.';
        }
      })
      .addCase(checkUsername.rejected, (state) => {
        state.usernameStatus = 'unavailable';
        state.usernameMessage = '다시 시도해 주세요.';
      })
      .addCase(checkNickname.pending, (state) => {
        state.nicknameStatus = 'checking';
      })
      .addCase(checkNickname.fulfilled, (state, action) => {
        console.log(action.payload);
        if (action.payload === '이미 사용 중인 닉네임입니다.') {
          state.nicknameStatus = 'invalid';
          state.nicknameMessage = '이미 사용 중인 닉네임입니다.';
          state.nickname = '';
        } else {
          state.nicknameStatus = 'available';
          state.nicknameMessage = '사용 가능한 닉네임 입니다.';
        }
      })
      .addCase(checkNickname.rejected, (state) => {
        state.nicknameStatus = 'unavailable';
        state.nicknameMessage = '다시 시도해 주세요.';
      })
      .addCase(signupUser.fulfilled, (state) => {
        state.username= '';
        state.usernameStatus= 'invalid';
        state.usernameMessage= '';
        state.nickname= '';
        state.nicknameStatus= 'invalid';
        state.nicknameMessage= '';
        state.password= '';
        state.confirmPassword= '';
        state.confirmPasswordMessage= '';
        state.confirmPasswordStatus= 'invalid';
        state.passwordMessage= '';
        state.passwordStatus= 'invalid';
        state.iconSeq= 0;
        state.birthDate= '';
        state.birthDateMessage= '';
        state.birthDateStatus= 'invalid';
      })
  },
});

export const {
  setUsername,
  setNickname,
  setPassword,
  setConfirmPassword,
  setIconSeq,
  setBirthDate,
} = signupSlice.actions;
export default signupSlice.reducer;
