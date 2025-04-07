import { createAsyncThunk } from '@reduxjs/toolkit';
import { Alarm } from './alarmTypes';
import { RootState } from '@/stores/store';
import api from '@/apis/apiClient';

export const fetchAlarms = createAsyncThunk<
  { alarms: Alarm[]; totalPages: number; currentPage: number },
  void,
  { state: RootState }
>('alarm/fetchAlarms', async (_, { getState }) => {
  const { page, size } = getState().alarm;
  const response = await api.get('/notifications/list/page', {
    params: { page, size },
  });
  console.log('⏰⏰⏰⏰⏰알람 목록 페이지네이션 thunks', response);
  return {
    alarms: response.data.data.alarms,
    totalPages: response.data.data.totalPages,
    currentPage: response.data.data.currentPage,
  };
});

export const readAlarm = createAsyncThunk<number, number>(
  'alarm/redaAlarm',
  async (alarmSeq, { rejectWithValue }) => {
    try {
      await api.put(`/notifications/read/${alarmSeq}`);

      return alarmSeq;
    } catch (error) {
      return rejectWithValue('알림 개별 읽기 실패');
    }
  }
);

export const readAllAlarms = createAsyncThunk<void, void>(
  'alarm/readAllAlarms',
  async (_, { rejectWithValue }) => {
    try {
      await api.put('/notifications/read-all');
    } catch (error) {
      return rejectWithValue('알림 전체 읽기 실패');
    }
  }
);

export const deleteAlarm = createAsyncThunk<number, number>(
  'alarm/deleteAlarm',
  async (alarmSeq, { rejectWithValue }) => {
    try {
      await api.delete(`/notifications/delete/${alarmSeq}`);

      return alarmSeq;
    } catch (error) {
      return rejectWithValue('알림 개별 삭제 실패');
    }
  }
);

export const deleteAllAlarms = createAsyncThunk<void, void>(
  'alarm/deleteAllAlarms',
  async (_, { rejectWithValue }) => {
    try {
      await api.delete('/notifications/delete-all');
    } catch (error) {
      return rejectWithValue('알림 전체 삭제 실패');
    }
  }
);
