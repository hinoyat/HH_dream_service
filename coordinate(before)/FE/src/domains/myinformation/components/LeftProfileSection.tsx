import { useNavigate } from 'react-router-dom';
import profileFrame from '@/assets/images/profileFrame.svg';
import { useState, useEffect, useDebugValue } from 'react';
import { getRandomIcon } from '@/hooks/ProfileIcons';
import api from '@/apis/apiClient';
import { changeIcon } from '@/stores/auth/authSlice';
import { useAppDispatch } from '@/hooks/hooks';

interface LeftProfileSectionProps {
  userData: {
    birthDate: string;
    iconSeq: number;
    introduction: string | null;
    nickname: string;
    userSeq: number;
    username: string;
  } | null;
  onUpdateSuccess?: () => void; // 중요: 부모에게 업데이트 성공을 알리는 콜백
}

const LeftProfileSection: React.FC<LeftProfileSectionProps> = ({
  userData,
  onUpdateSuccess,
}) => {
  const [currentIconId, setCurrentIconId] = useState<number>(1);
  const [currentIconUrl, setCurrentIconUrl] = useState<string>('');
  const [shuffleCount, setShuffleCount] = useState<number>(0);
  const [showRareMessage, setShowRareMessage] = useState<boolean>(false);
  const [showEpicMessage, setShowEpicMessage] = useState<boolean>(false);
  const [showUniqueMessage, setShowUniqueMessage] = useState<boolean>(false);
  const [showLegendaryMessage, setShowLegendaryMessage] =
    useState<boolean>(false);
  const [isUpdating, setIsUpdating] = useState<boolean>(false);

  const dispatch = useAppDispatch();

  // userData가 변경될 때마다 아이콘 ID 업데이트
  useEffect(() => {
    if (userData && userData.iconSeq) {
      setCurrentIconId(userData.iconSeq);
    }
  }, [userData]);

  // 아이콘 ID가 변경될 때마다 아이콘 URL 업데이트
  useEffect(() => {
    const loadIcon = async () => {
      try {
        const iconModule = await import(
          `@/assets/profile-icon/icon${currentIconId}.svg`
        );
        setCurrentIconUrl(iconModule.default);
      } catch (err) {
        console.error('아이콘을 불러오는데 실패했습니다:', err);
        try {
          const defaultIconModule = await import(
            '@/assets/profile-icon/icon1.svg'
          );
          setCurrentIconUrl(defaultIconModule.default);
        } catch (fallbackErr) {
          console.error('기본 아이콘도 불러오지 못했습니다:', fallbackErr);
        }
      }
    };

    loadIcon();
  }, [currentIconId]);

  // 아이콘 변경 및 API 호출하는 함수
  const updateIconToServer = async (iconSeq: number) => {
    if (!userData) return;

    setIsUpdating(true);

    try {
      // 서버에 직접 아이콘 변경 요청
      const response = await api.put('/users/me', {
        iconSeq: iconSeq,
        // 다른 필드는 기존 값 유지
        nickname: userData.nickname,
        birthDate: userData.birthDate.replace(/-/g, ''),
      });

      console.log('아이콘 업데이트 성공:', response);

      // API 호출이 성공하면 부모 컴포넌트에 알림
      if (response.data && response.data.status === 200) {
        console.log('아이콘이 성공적으로 업데이트되었습니다.');

        // 중요: 부모에게 업데이트 성공을 알림 -> 즉각적인 데이터 갱신 트리거
        if (onUpdateSuccess) {
          onUpdateSuccess();
        }
      }
    } catch (error) {
      console.error('아이콘 업데이트 실패:', error);
      // 실패 시 원래 아이콘으로 롤백
      if (userData && userData.iconSeq) {
        setCurrentIconId(userData.iconSeq);
      }
    } finally {
      setIsUpdating(false);
      dispatch(changeIcon(iconSeq));
    }
  };

  const handleShuffle = () => {
    // 업데이트 중이면 중복 요청 방지
    if (isUpdating) return;

    // 랜덤 아이콘 정보 가져오기
    const randomIcon = getRandomIcon();

    // UI를 즉시 업데이트 (낙관적 업데이트)
    setCurrentIconId(randomIcon.id);
    setShuffleCount((prev) => prev + 1);

    // 희귀 아이콘일 경우 메시지 표시
    if (randomIcon.isRare) {
      setShowEpicMessage(false);
      setShowUniqueMessage(false);
      setShowLegendaryMessage(false);
      setShowRareMessage(true);
      setTimeout(() => setShowRareMessage(false), 3000);
    }
    if (randomIcon.isEpic) {
      setShowRareMessage(false);
      setShowUniqueMessage(false);
      setShowLegendaryMessage(false);
      setShowEpicMessage(true);
      setTimeout(() => setShowEpicMessage(false), 3000);
    }
    if (randomIcon.isUnique) {
      setShowRareMessage(false);
      setShowEpicMessage(false);
      setShowLegendaryMessage(false);
      setShowUniqueMessage(true);
      setTimeout(() => setShowUniqueMessage(false), 3000);
    }
    if (randomIcon.isLegendary) {
      setShowRareMessage(false);
      setShowEpicMessage(false);
      setShowUniqueMessage(false);
      setShowLegendaryMessage(true);
      setTimeout(() => setShowLegendaryMessage(false), 3000);
    }

    // 서버에 아이콘 변경 요청
    updateIconToServer(randomIcon.id);
  };

  return (
    <div className="text-white flex-1">
      <div className="w-full h-full flex flex-col gap-10 items-center justify-center pr-20 pb-10">
        <div className="relative">
          <img
            src={profileFrame}
            alt="Profile Frame"
            className="w-33"
          />
          {currentIconUrl && (
            <img
              src={currentIconUrl}
              alt="프로필 이미지"
              className="w-20 object-cover rounded-full absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"
            />
          )}

          {showRareMessage && (
            <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-full bg-blue-500 text-white p-2 rounded-md animate-bounce w-full">
              🎉 희귀 아이콘 획득!
            </div>
          )}

          {showEpicMessage && (
            <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-full bg-purple-500 text-white p-2 rounded-md animate-bounce w-full">
              🎉 에픽 아이콘 획득!
            </div>
          )}

          {showUniqueMessage && (
            <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-full bg-yellow-500 text-white p-2 rounded-md animate-bounce w-full">
              🎉 유니크 아이콘 획득!
            </div>
          )}

          {showLegendaryMessage && (
            <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-full bg-emerald-400 text-white p-2 rounded-md animate-bounce w-full">
              🎉 레전드리 아이콘 획득!
            </div>
          )}

          {isUpdating && (
            <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 translate-y-full text-xs text-blue-300">
              업데이트 중...
            </div>
          )}
        </div>

        <div className="flex flex-col items-center justify-center text-[20px] tracking-wide dung-font">
          <p>Hello, {userData?.nickname || 'Guest'} !!!</p>
          <p>버튼을 눌러 아이콘을 수정하세요</p>
        </div>

        <div className="box-button">
          <button
            className="infor-button text-black dung-font"
            onClick={handleShuffle}
            disabled={isUpdating}>
            <span>{isUpdating ? '업데이트 중...' : 'Shuffle'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default LeftProfileSection;
