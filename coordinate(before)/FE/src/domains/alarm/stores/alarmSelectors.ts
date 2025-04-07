import { RootState } from '@/stores/store';

export const selectAlarms = (state: RootState) => state.alarm.alarms;
export const selectLoading = (state: RootState) => state.alarm.loading;
export const selectHasMore = (state: RootState) => state.alarm.hasMore;
export const selectPage = (state: RootState) => state.alarm.page;
