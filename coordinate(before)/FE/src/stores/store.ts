import { configureStore } from '@reduxjs/toolkit';
import authReducer from './auth/authSlice';
import signupReducer from '@/domains/signup/stores/signupSlice';
import diaryReducer from '@/stores/diary/diarySlice';
import userpageReducer from '@/domains/mainpage/stores/userSlice';
import alarmReducer from '@/domains/alarm/stores/alarmSlice';
import musicReducer from '@/stores/music/musicSlice';
import dashboardSlice from '@/domains/dashboard/store/dashboardSlice'


export const store = configureStore({
  reducer: {
    auth: authReducer, // auth 슬라이스 등록
    signup: signupReducer,
    diary: diaryReducer,
    userpage: userpageReducer,
    alarm: alarmReducer,
    music: musicReducer,
    dashboard: dashboardSlice,
  },
});

// 스토어의 타입 정의
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
