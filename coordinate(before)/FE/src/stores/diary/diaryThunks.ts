import {
  addDiary,
  setCurrentDiary,
  setDiaries,
  setError,
  setLoading,
  updateDiary,
} from '@/stores/diary/diarySlice';
import { AppDispatch } from '../store';
import { diaryApi } from '@/domains/diary/api/diaryApi';
import { DiaryData } from '@/domains/diary/Types/diary.types';

// 모든 일기 가져오기
export const fetchDiaries = () => async (dispatch: AppDispatch) => {
  try {
    dispatch(setLoading(true));
    const response = await diaryApi.getDiaries();
    dispatch(setDiaries(response.data));
    dispatch(setError(null));
  } catch (error) {
    console.error('Failed to fetch diaries:', error);
    dispatch(setError('일기 목록을 불러오는 데 실패했습니다.'));
  } finally {
    dispatch(setLoading(false));
  }
};

// 단일 일기 가져오기 및 현재 일기로 설정하기
export const fetchDiaryById =
  (diarySeq: number) => async (dispatch: AppDispatch) => {
    try {
      dispatch(setLoading(true));
      const response = await diaryApi.getDiaryById(diarySeq);
      dispatch(setCurrentDiary(response.data));
      dispatch(setError(null));
      return response.data;
    } catch (error) {
      console.error(`Failed to fetch diary with ID ${diarySeq}:`, error);
      dispatch(setError('일기를 불러오는 데 실패했습니다.'));
      return null;
    } finally {
      dispatch(setLoading(false));
    }
  };

// 새 일기 생성
export const createNewDiary =
  (diaryData: DiaryData) => async (dispatch: AppDispatch) => {
    try {
      dispatch(setLoading(true));
      const response = await diaryApi.createDiary(diaryData);
      dispatch(addDiary(response.data));
      dispatch(setError(null));
      return response.data;
    } catch (error) {
      console.error('Failed to create diary:', error);
      dispatch(setError('일기 생성에 실패했습니다.'));
      throw error;
    } finally {
      dispatch(setLoading(false));
    }
  };

// 일기 수정
export const updateExistingDiary =
  (diarySeq: number, diaryData: DiaryData) => async (dispatch: AppDispatch) => {
    try {
      dispatch(setLoading(true));
      const response = await diaryApi.updateDiary(diarySeq, diaryData);
      dispatch(updateDiary(response.data));
      dispatch(setError(null));
      return response.data;
    } catch (error) {
      console.error(`Failed to update diary with ID ${diarySeq}:`, error);
      dispatch(setError('일기 수정에 실패했습니다.'));
      throw error;
    } finally {
      dispatch(setLoading(false));
    }
  };
