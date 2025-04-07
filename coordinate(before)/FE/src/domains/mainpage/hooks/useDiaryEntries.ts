// 일기 항목 관리 커스텀 훅
// 1. 일기목록 상태관리 : 앱 전체에서 사용될 수 있는 일기 목록 상태를 제공
// 2. 로컬 스토리지 동기화
// 3. 일기 추가, 수정, 삭제

import DiaryEntry from '@/domains/mainpage/models/DiaryEntry';
import { useState, useEffect } from 'react';

export interface Position {
  x: number;
  y: number;
  z: number;
}

// 일기 데이터 인터페이스 - 프론트엔드에서는 불리언으로 관리
interface DiaryData {
  title: string;
  content: string;
  tags: string[];
  isPublic: boolean; // 변경: 'Y'/'N' 대신 불리언 사용
  video_url?: string;
  dream_date?: string;
}

// 일기 목록, 일기 추가 및 삭제 메서드의 타입
interface UseDiaryEntriesReturn {
  entries: DiaryEntry[];
  addEntry: (newEntry: DiaryData) => DiaryEntry; // 변경: 반환 타입 추가
  editEntry: (id: number, updatedData: Partial<DiaryData>) => DiaryEntry | null; // 추가: 수정 메서드
  removeEntry: (id: number) => void;
  getEntryById: (id: number) => DiaryEntry | undefined; // 추가: ID로 일기 항목 조회
}

export const useDiaryEntries = (): UseDiaryEntriesReturn => {
  // 일기 목록 상태 관리
  // 앱 전체에서 사용될 일기 목록 상태
  const [entries, setEntries] = useState<DiaryEntry[]>([]);

  // 로컬 스토리지에서 일기 데이터 불러오기
  // 앱 초기 로드 시 실행되며, 저장된 데이터가 없으면 더미 데이터 사용
  useEffect(() => {
    const storedEntries = localStorage.getItem('diaryEntries');
    if (storedEntries) {
      try {
        const parsedData = JSON.parse(storedEntries);
        const parsedEntries = parsedData.map((entry: any) => {
          // 태그를 문자열 배열로 확실히 변환
          const tags = Array.isArray(entry.tags)
            ? entry.tags.map((tag: any) => String(tag))
            : [];

          // 객체 생성
          const diaryEntry = DiaryEntry.create({
            user_seq: entry.user_seq || 1, // 기본값 설정. 추후 currentUserId
            title: entry.title,
            content: entry.content,
            tags: tags,
            is_public: entry.is_public, // DiaryEntry 내부에서 변환 처리
            video_url: entry.video_url || null,
            dream_date: entry.dream_date,
          });
          // diary_seq 명시적 설정 (필요한 경우)
          if (entry.diary_seq) {
            diaryEntry.diary_seq = entry.diary_seq;
          }

          return diaryEntry;
        });
        setEntries(parsedEntries);
      } catch (error) {
        console.error('일기 데이터를 불러오는 중 오류 발생:', error);
        setEntries([]);
      }
    } else {
      setEntries([]);
    }
  }, []);

  // 일기 목록 변경 시 로컬 스토리지에 저장
  // 일기 추가/삭제 시 자동으로 로컬 스토리지 업데이트
  useEffect(() => {
    if (entries.length > 0) {
      localStorage.setItem('diaryEntries', JSON.stringify(entries));
    }
  }, [entries]);

  // ID로 일기 항목 조회
  const getEntryById = (id: number): DiaryEntry | undefined => {
    return entries.find((entry) => entry.diary_seq === id);
  };

  //------------------------일기 생성 -------------------------------//
  const addEntry = (newEntryData: DiaryData): DiaryEntry => {
    const newEntry = DiaryEntry.create({
      user_seq: 1, // 현재 사용자 ID
      title: newEntryData.title,
      content: newEntryData.content,
      tags: newEntryData.tags,
      is_public: newEntryData.isPublic, // 불리언 전달 (내부에서 'Y'/'N'으로 변환)
      video_url: newEntryData.video_url,
      dream_date: newEntryData.dream_date,
      position: DiaryEntry.generateRandomSpherePosition(100), // 랜덤 위치 생성
    });

    setEntries((prevEntries) => [...prevEntries, newEntry]);
    return newEntry;
  };

  //------------------------일기 수정------------------------//
  const editEntry = (
    id: number,
    updatedData: Partial<DiaryData>
  ): DiaryEntry | null => {
    // 일기 찾기
    const entryIndex = entries.findIndex((entry) => entry.diary_seq === id);
    if (entryIndex === -1) return null;

    // 기존 항목의 복사본 생성
    const updatedEntries = [...entries];
    const entryToUpdate = updatedEntries[entryIndex];

    // DiaryEntry의 update 메서드 사용하여 업데이트
    entryToUpdate.update({
      title: updatedData.title,
      content: updatedData.content,
      tags: updatedData.tags,
      is_public: updatedData.isPublic, // 불리언 전달 (내부에서 'Y'/'N'으로 변환)
      video_url: updatedData.video_url,
      dream_date: updatedData.dream_date,
    });

    // 상태 업데이트
    setEntries(updatedEntries);
    return entryToUpdate;
  };

  //----------------일기 삭제 메서드------------------------//
  const removeEntry = (id: number): void => {
    setEntries((prevEntries) =>
      prevEntries.filter((entry) => entry.diary_seq !== id)
    );
  };

  return {
    entries,
    addEntry,
    editEntry,
    removeEntry,
    getEntryById,
  };
};
