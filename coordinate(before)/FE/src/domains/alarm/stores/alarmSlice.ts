import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import {
  fetchAlarms,
  readAlarm,
  deleteAlarm,
  readAllAlarms,
  deleteAllAlarms,
} from './alarmThunks';
import { Alarm, AlarmState } from './alarmTypes';

const initialState: AlarmState = {
  alarms: [],
  size: 5,
  hasMore: true,
  loading: false,
  page: 0,
  totalItems: 1,
  totalPages: 0,
};

const alarmSlice = createSlice({
  name: 'alarm',
  initialState,
  reducers: {
    resetAlarms: (state) => {
      state.alarms = [];
      state.hasMore = true;
      state.page = 0;
      state.totalItems = 1;
      state.totalPages = 0;
    },
    resetPage: (state) => {
      state.page = 0;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAlarms.pending, (state) => {
        state.loading = true;
      })
      .addCase(
        fetchAlarms.fulfilled,
        (
          state,
          action: PayloadAction<{
            alarms: Alarm[];
            totalPages: number;
            currentPage: number;
          }>
        ) => {
          state.alarms = [...state.alarms, ...action.payload.alarms];
          state.page = action.payload.currentPage + 1;
          state.totalPages = action.payload.totalPages;
          state.hasMore = state.page < state.totalPages;
          state.loading = false;
        }
      )
      .addCase(fetchAlarms.rejected, (state) => {
        state.loading = false;
      })
      .addCase(readAlarm.fulfilled, (state, action) => {
        // 특정 alarmSeq를 가진 알람을 찾아 isRead를 'Y'로 변경
        const alarmIndex = state.alarms.findIndex(
          (alarm) => alarm.alarmSeq === action.payload
        );
        if (alarmIndex !== -1) {
          state.alarms[alarmIndex].isRead = 'Y';
        }
      })
      .addCase(readAllAlarms.fulfilled, (state) => {
        state.alarms = state.alarms.map((alarm) => ({ ...alarm, isRead: 'Y' }));
      })
      .addCase(deleteAlarm.fulfilled, (state, action) => {
        state.alarms = state.alarms.filter(
          (alarm) => alarm.alarmSeq !== action.payload
        );
      })
      .addCase(deleteAllAlarms.fulfilled, (state) => {
        state.alarms = [];
      });
  },
});

export const { resetAlarms, resetPage } = alarmSlice.actions;
export default alarmSlice.reducer;
