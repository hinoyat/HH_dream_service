export interface Alarm {
  alarmSeq: number;
  content: string;
  createdAt: string;
  isRead: string;
  readYn: boolean;
  type: string;
  userSeq: number;
  diarySeq?: number;
}

export interface AlarmState {
  alarms: Alarm[];
  size: number;
  hasMore: boolean;
  page: number;
  loading: boolean;
  totalItems: number;
  totalPages: number;
}
