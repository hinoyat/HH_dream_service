// 우주선 내 버튼들
import { useNavigate } from 'react-router-dom';
import '../themes/SpaceShip.css';
import { IoClose } from 'react-icons/io5';
import EditProfileButton from '@/domains/spaceship/components/EditProfileButton';
import { useSelector } from 'react-redux';
import { selectVisitUser } from '@/domains/mainpage/stores/userSelectors';

const SpaceShipContent = () => {
  const nav = useNavigate();
  const visitUser = useSelector(selectVisitUser);

  const onClickLucky = () => {
    nav('/luckynumber');
  };
  const onClickDream = () => {
    nav('/ggumplaylist');
  };
  const onClickFortune = () => {
    nav('/todayfortune');
  };
  const onClickHome = () => {
    nav(`/${visitUser.username}`); // 지금 구경중인 우주 주인장 닉네임 넣기
  };
  const onClickEditMyInformation = () => {
    nav('/passwordcheck');
  };

  return (
    <div>
      {/* 닫기 버튼 */}
      <div
        className="absolute z-40 top-[2%] right-[1%] cursor-pointer"
        onClick={onClickHome}>
        <IoClose className="text-white text-4xl" />
      </div>

      {/* 메뉴 버튼들 */}
      <div className="absolute dung-font top-[44%] w-full flex justify-center items-center gap-x-65 z-20">
        {/* 행운번호 뽑기 버튼 */}
        <button
          onClick={onClickLucky}
          className="text-white text-2xl font-bold cursor-pointer hover:scale-105 transition-transform neon-text">
          행운번호 뽑기
        </button>

        {/* 꿈해몽 하기 버튼 */}
        <button
          onClick={onClickDream}
          className="relative top-[-20px] text-white text-2xl font-bold cursor-pointer hover:scale-105 transition-transform neon-text whitespace-nowrap">
          꿈 플레이리스트
        </button>

        {/* 오늘의 운세 버튼 */}
        <button
          onClick={onClickFortune}
          className="text-white text-2xl font-bold cursor-pointer hover:scale-105 transition-transform neon-text">
          오늘의 운세
        </button>
      </div>

      {/* 회원정보수정 버튼 */}
      <div className="absolute dung-font z-20 top-[75%] left-1/2 transform -translate-x-1/2">
        <EditProfileButton onClick={onClickEditMyInformation}>
          회원정보수정
        </EditProfileButton>
      </div>
    </div>
  );
};

export default SpaceShipContent;
