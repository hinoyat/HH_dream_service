import { useSelector } from 'react-redux';
import { selectVisitUser } from '../stores/userSelectors';
import { useAppDispatch } from '@/hooks/hooks';
import { logoutUser } from '@/stores/auth/authThunks';
import { useNavigate } from 'react-router-dom';
import api from '@/apis/apiClient';
import { updateSubscriptionStatus } from '../stores/userSlice';
import { useEffect, useState } from 'react';
import { getIconById } from '@/hooks/ProfileIcons';

interface UserSpaceHeaderProps {
  nickname: string | undefined;
  icon: number;
  isMySpace?: boolean;
}

const UserSpaceHeader = ({
  nickname,
  icon,
  isMySpace,
}: UserSpaceHeaderProps) => {
  const nav = useNavigate();
  const dispatch = useAppDispatch();
  const currentOwnerUser = useSelector(selectVisitUser);

  // 로컬 상태로 구독 상태 관리 추가
  const [isSubscribed, setIsSubscribed] = useState(
    currentOwnerUser?.isSubscribed === 'Y'
  );

  // 디버깅: Redux 상태 변경 확인 + 로컬 상태 동기화
  useEffect(() => {
    if (currentOwnerUser) {
      setIsSubscribed(currentOwnerUser.isSubscribed === 'Y');
    }
  }, [currentOwnerUser]);

  const buttonLabel = isMySpace
    ? '로그아웃'
    : isSubscribed
      ? '구독취소'
      : '구독';

  const handleButtonClick = async () => {
    if (buttonLabel === '로그아웃') {
      await dispatch(logoutUser());
      nav('/intro', { replace: true });
      window.location.reload();
    } else {
      try {
        const response = await api.patch(
          `/subscription/${currentOwnerUser.userSeq}`
        );

        // API 응답 구조 확인 및 예외 처리
        const newStatus =
          response.data?.isSubscribed || (isSubscribed ? 'N' : 'Y'); // 응답이 없을 경우 토글

        // 로컬 상태 즉시 업데이트
        setIsSubscribed(newStatus === 'Y');

        // Redux 상태도 업데이트
        dispatch(updateSubscriptionStatus(newStatus));
      } catch (error) {
        console.error('에러가 발생하였습니다:', error);
        // 오류 발생시 사용자에게 알림 (선택사항)
        alert('구독 상태 변경 중 오류가 발생했습니다.');
      }
    }
  };

  return (
    <div className="p-3">
      <div className="flex items-baseline">
        {isMySpace ? (
          <div className="flex items-center">
            <img
              src={getIconById(icon)}
              alt="내 프로필 사진"
              className="w-8 h-7 object-contain"
            />
            <span className="ml-1">
              <span className="text-lg font-bold text-[#e0cfaa]">나</span>
              <span className="text-base text-white/90">의 우주</span>
            </span>
          </div>
        ) : (
          <div className="flex items-center">
            <img
              src={getIconById(icon)}
              alt="내 프로필 사진"
              className="w-8 h-8 mr-1"
            />
            <>
              <h3 className="text-base font-bold text-[#e0cfaa]">{nickname}</h3>
              <p className="text-sm text-white/90 ml-1">님의 우주</p>
            </>
          </div>
        )}
      </div>
      <button
        onClick={handleButtonClick}
        className="mt-2 ml-2 w-[80px] h-[25px] bg-white/50 text-white rounded-[6px] text-xs cursor-pointer hover:bg-white/70 transition-colors">
        {buttonLabel}
      </button>
    </div>
  );
};

export default UserSpaceHeader;
