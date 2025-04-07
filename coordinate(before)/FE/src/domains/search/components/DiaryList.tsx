import React, { useState, useEffect } from 'react';
import api from '@/apis/apiClient';
import { getIconById } from '@/hooks/ProfileIcons';
import { useNavigate } from 'react-router-dom';

interface Tag {
  tagSeq: number | null;
  name: string;
}

interface Diary {
  diarySeq: number;
  userSeq: number;
  title: string;
  content: string;
  dreamDate: string;
  isPublic: string;
  mainEmotion: string;
  tags: Tag[];
  userNickname?: string;
  profileImage?: string;
  username?: string; // username 추가
}

interface User {
  userSeq: number;
  username: string;
  nickname: string;
  birthDate: string;
  introduction: string | null;
  iconSeq: number;
  isSubscribed: string;
}

interface ApiResponse {
  timestamp: string;
  status: number;
  message: string;
  data: Diary[]; // 이제 DiarySearch에서 content 배열만 전달
}

interface DiaryListProps {
  data: ApiResponse;
}

const DiaryList: React.FC<DiaryListProps> = ({ data }) => {
  const [diaries, setDiaries] = useState<Diary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const nav = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      // API 응답에서 일기 데이터 배열을 추출
      if (
        !data ||
        !data.data ||
        !Array.isArray(data.data) ||
        data.data.length === 0
      ) {
        setDiaries([]);
        setLoading(false);
        return;
      }

      try {
        // Create a map to store user info by userSeq to avoid duplicate API calls
        const userMap = new Map<number, User>();
        const uniqueUserSeqs = [
          ...new Set(data.data.map((diary) => diary.userSeq)),
        ];
        const userPromises = uniqueUserSeqs.map(async (userSeq) => {
          try {
            const response = await api.get(`/users/seq/${userSeq}`);
            if (
              response.data &&
              response.data.status === 200 &&
              response.data.data
            ) {
              userMap.set(userSeq, response.data.data);
            }
            return response.data;
          } catch (error) {
            console.warn(`Error fetching user ${userSeq}:`, error);
            return null;
          }
        });

        await Promise.all(userPromises);
        const diariesWithUserInfo = data.data.map((diary) => {
          const userInfo = userMap.get(diary.userSeq);
          return {
            ...diary,
            userNickname: userInfo?.nickname || `사용자 ${diary.userSeq}`,
            username: userInfo?.username, // username 추가
            // iconSeq가 1부터 시작하는 것으로 보이므로 iconSeq 그대로 사용
            profileImage: userInfo ? getIconById(userInfo.iconSeq) : undefined,
          };
        });

        setDiaries(diariesWithUserInfo);
      } catch (error) {
        console.error('Error fetching user data:', error);
        // If there's an error, still show diaries but without user info
        setDiaries(data.data);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [data]);

  if (loading) {
    return <div className="text-white text-center mt-4">로딩 중...</div>;
  }

  if (!diaries || diaries.length === 0) {
    return (
      <div className="text-white text-center mt-4">검색 결과가 없습니다.</div>
    );
  }

  return (
    <div className="flex flex-col gap-5 mt-4">
      {diaries.map((diary) => (
        <div
          key={diary.diarySeq}
          className="truncate w-full">
          <div className="bg-[#505050]/90 flex px-10 py-5 gap-8 w-full rounded-lg">
            {/*방명록 왼쪽 시작 */}
            {/* 제목 */}
            <div className="flex flex-col truncate gap-2 basis-6/8">
              <h1 className="text-lg text-white font-semibold">
                {diary.title || '(제목 없음)'}
              </h1>
              <div className="px-2 flex flex-col gap-2">
                <p className="text-[14px] truncate text-white w-full">
                  {diary.content}
                </p>
                <div className="flex gap-2 flex-wrap">
                  {Array.isArray(diary.tags) && diary.tags.length > 0 ? (
                    diary.tags.map((tag, index) => (
                      <p
                        key={index}
                        className="bg-[#D9D9D9]/29 text-white rounded text-xs px-2 py-1">
                        {tag.name}
                      </p>
                    ))
                  ) : (
                    <p className="text-white/50 text-xs">태그 없음</p>
                  )}
                </div>
              </div>
            </div>
            {/* 방명록 왼쪽 끝 */}
            <div className="flex gap-10 items-center ">
              <div className="flex gap-3 items-center">
                {diary.profileImage ? (
                  <img
                    src={diary.profileImage}
                    alt="프로필"
                    className="w-6 h-6 rounded-full"
                  />
                ) : (
                  <div className="w-6 h-6 bg-gray-600 rounded-full flex items-center justify-center text-xs text-white">
                    {diary.userNickname?.charAt(0) ||
                      String(diary.userSeq).charAt(0)}
                  </div>
                )}
                <p className="text-[15px] text-white">
                  {diary.userNickname || `사용자 ${diary.userSeq}`}
                </p>
              </div>

              <div>
                <button
                  className="text-[14px] bg-[#363736] text-white px-4 py-1 rounded cursor-pointer hover:bg-neutral-500"
                  onClick={() => {
                    // 현재 선택한 일기 정보를 LocalStorage에 저장
                    localStorage.setItem(
                      'selectedDiarySeq',
                      diary.diarySeq.toString()
                    );
                    // 페이지 이동
                    nav(`/${diary.username || ''}`);
                  }}>
                  보러가기
                </button>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default DiaryList;
