import React from 'react';
import { getIconById } from '@/hooks/ProfileIcons';

// 사용자 데이터 타입 정의
interface User {
  userSeq?: number;
  subscribedSeq?: number;
  username?: string;
  nickname: string;
  iconSeq: number;
  birthDate?: string;
  introduction?: string | null;
  isSubscribed?: boolean;
}

interface UserListProps {
  data: User[];
  onSubscribe: (userSeq: number) => void;
  onUnsubscribe: (subscribedSeq: number) => void;
  onVisit: (userSeq: number) => void;
}

const UserList: React.FC<UserListProps> = ({
  data,
  onSubscribe,
  onUnsubscribe,
  onVisit,
}) => {
  if (!data || data.length === 0) {
    return (
      <div className="text-white text-center py-4">검색 결과가 없습니다.</div>
    );
  }

  return (
    <div className="flex flex-col gap-5 mt-4">
      {data.map((user) => {
        // userSeq나 subscribedSeq 중 하나를 키로 사용
        const key = user.subscribedSeq || user.userSeq || Math.random();
        // subscribedSeq가 있으면 이미 구독 중인 사용자(친구)
        const isSubscribed = !!user.subscribedSeq || user.isSubscribed;
        // 방문할 사용자 ID (친구인 경우 subscribedSeq, 검색 결과인 경우 userSeq)
        const visitId = user.userSeq || user.subscribedSeq;

        return (
          <div
            key={key}
            className="bg-[#505050]/90 rounded-lg flex px-15 py-5 gap-5 justify-between">
            <div className="flex items-center gap-2">
              <img
                src={getIconById(user.iconSeq)}
                className="w-7"
                alt={`${user.nickname}의 프로필 아이콘`}
                onError={(e: React.SyntheticEvent<HTMLImageElement>) => {
                  e.currentTarget.onerror = null;
                  e.currentTarget.src = '/default-icon.png'; // Fallback image
                }}
              />
              <p className="text-white text-[17px]">{user.nickname}</p>
              {user.username && (
                <span className="text-white/70 text-sm">({user.username})</span>
              )}
            </div>

            <div className="flex gap-3 text-white outline:none">
              {isSubscribed ? (
                // 이미 구독 중인 경우 - 구독취소 버튼
                <button
                  className="bg-[#363736] text-white text-[16px] py-1 w-28 rounded cursor-pointer hover:bg-neutral-500"
                  onClick={() =>
                    user.subscribedSeq && onUnsubscribe(user.subscribedSeq)
                  }>
                  구독취소
                </button>
              ) : (
                // 구독 중이 아닌 경우 - 구독하기 버튼
                <button
                  className="bg-[#363736] text-white text-[16px] py-1 w-28 rounded cursor-pointer hover:bg-neutral-500"
                  onClick={() => user.userSeq && onSubscribe(user.userSeq)}>
                  구독하기
                </button>
              )}

              <button
                className="bg-[#363736] text-white text-[16px] py-1 w-28 rounded cursor-pointer hover:bg-neutral-500"
                onClick={() => visitId && onVisit(visitId)}>
                놀러가기
              </button>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default UserList;
