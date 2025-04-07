// 나의 우주페이지
import GuestBook from '@/domains/guestbook/GuestBook';
import Universe from '@/domains/mainpage/components/universe/Universe';
import UserSpaceHeader from '@/domains/mainpage/components/UserSpaceHeader';
import OnboardingModal from '@/domains/mainpage/components/onboarding/OnboardingModal';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { useAppDispatch, useAppSelector  } from '@/hooks/hooks';
import { logoutUser } from '@/stores/auth/authThunks';
import { useSelector } from 'react-redux';
import { selectUser } from '@/stores/auth/authSelectors';
import { selectVisitUser } from '../stores/userSelectors';
import { visitUserpage } from '../stores/userThunks';

import { AiFillSound } from 'react-icons/ai';
import { BiSolidVolumeMute } from 'react-icons/bi';
import { Tooltip } from 'react-tooltip';
import { selectMusicPlaying, selectCurrentTrack } from '@/stores/music/musicSelectors';
import { initializeAudio, togglePlayback } from '@/stores/music/musicThunks';

import onboardingBlackhole from '@/assets/images/boardingBlackhole.gif';
import onboardingUfo from '@/assets/images/boardingUfo.gif';

const MainPage = () => {
  const [step, setStep] = useState(0);
  const [showOnboarding, setShowOnboarding] = useState(false);

  const [showGuestbook, setShowGuestbook] = useState(false);
  const currentTrack = useAppSelector(selectCurrentTrack);
  const isMusicPlaying = useAppSelector(selectMusicPlaying);

  const params = useParams();
  const dispatch = useAppDispatch();
  const nav = useNavigate();

  const loginUser = useSelector(selectUser);
  const visitUser = useSelector(selectVisitUser);

  const slides = [
    {
      title: '일기 작성 🌌',
      description:
        '우주의 끝에서, 오늘의 꿈을 기록해보세요. 더블 클릭으로 나만의 별자리를 시작할 수 있어요',
      animation: 'double-click-animation',
    },
    {
      title: 'UFO 🛸',
      description: '내 우주에서 UFO를 찾아보세요. 무슨 기능이 있을까요?',
      imageUrl: onboardingUfo,
    },
    {
      title: '블랙홀 🪐',
      description:
        '블랙홀에 들어가면, 다른 사람들의 꿈 속으로 랜덤 여행이 시작됩니다. 신비한 우주 여행을 떠나보세요!',
      imageUrl: onboardingBlackhole,
    },
  ];

  //      방명록 표시 여부      //
  const onClickGuestBookModal = () => {
    setShowGuestbook(!showGuestbook);
  };

  useEffect(() => {
    const hasSeen = localStorage.getItem('hasSeenOnboarding');
    if (!hasSeen) {
      setShowOnboarding(true);
    }
    if (params.username) {
      console.log('✅ MainPage 마운트됨!');
      console.log('내 우주인가?: ' + isMySpace);
      dispatch(visitUserpage({ username: params.username }));
    }
  }, [params, dispatch]);

  // 방문하려는 유저 정보를 가져오는 데 실패하면 내 페이지로 이동
  useEffect(() => {
    if (params.username === '') {
      nav(`/${loginUser?.username}`, { replace: true });
    }
  }, [params.username, nav]);

  // 배경음악

  useEffect(() => {
    // initializeAudio()를 인자 없이 호출하면 내부에서 
    // 사용자 정의 배경음악 또는 기본 배경음악을 자동으로 선택함
    dispatch(initializeAudio()).then((result) => {
      if (!result) {
        console.error('오디오 초기화 실패....');
      }
    });
  }, [dispatch]);

  //          좌측 상단 UserSpaceHeader 컴포넌트          //
  const isMySpace = params.username === loginUser?.username ? true : false; // 내 우주인지 여부

  const handleOnboardingClose = () => {
    localStorage.setItem('hasSeenOnboarding', 'true');
    setShowOnboarding(false);
  };

  const handleButtonClick = async () => {
    // 로그아웃 버튼 로직
    await dispatch(logoutUser());
    nav('/intro', { replace: true });
    window.location.reload(); // 새로고침 -> refresh 삭제하려고, 백엔드한테 삭제 되나 요청해보기
  };

  // 음악 토글 핸들러 (임시)
  const handleMusicToggle = () => {
    dispatch(togglePlayback());
  };

  return (
    <div className="flex flex-col items-start text-white relative w-screen h-screen overflow-hidden">
      {/* 우주영역 */}

      {showOnboarding && (
        <OnboardingModal
          slides={slides}
          step={step}
          onNext={(newStep) => setStep(newStep)}
          onClose={handleOnboardingClose}
        />
      )}
      <Universe
        isMySpace={isMySpace}
        // 내 우주면 userSeq 안넘기고 다른 사람 우주면 넘겨줌 (목록조회를 위해)
        userSeq={isMySpace ? undefined : visitUser?.userSeq}
      />

      {/* 닉네임님의 우주입니다 & 버튼 */}
      <div className="absolute top-5 left-5">
        <UserSpaceHeader
          nickname={isMySpace ? loginUser?.nickname : visitUser?.nickname}
          icon={isMySpace ? loginUser?.iconSeq : visitUser?.iconSeq}
          isMySpace={isMySpace}
        />
      </div>

      {/* 방명록 모달 (조건부 렌더링) */}
      {showGuestbook && <GuestBook onClose={onClickGuestBookModal} />}

      {/* 음악 제어 버튼 - 오른쪽 상단에 배치 */}
      <div className="absolute top-5 right-5 z-10">
        <button
          onClick={handleMusicToggle}
          className="text-white/90 hover:text-white cursor-pointer"
          data-tooltip-id="music-tooltip"
          data-tooltip-content={isMusicPlaying ? '음악 끄기' : '음악 켜기'}>
          {isMusicPlaying ? (
            <AiFillSound size={23} />
          ) : (
            <BiSolidVolumeMute size={23} />
          )}
        </button>
      </div>

      <Tooltip
        id="music-tooltip"
        place="bottom"
        style={{ zIndex: 9999 }}
      />
    </div>
  );
};
export default MainPage;
