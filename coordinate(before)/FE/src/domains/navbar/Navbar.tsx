import React, { useState } from 'react';
import { MdHomeFilled } from 'react-icons/md';
import { BsEnvelopePaperHeartFill } from 'react-icons/bs';

import { VscSearch } from 'react-icons/vsc';
import { FaRegBell } from 'react-icons/fa6';
import { Link } from 'react-router-dom';
import SearchModal from '../search/modal/SearchModal'; // 모달 임포트
import { Tooltip } from 'react-tooltip';
import GuestBook from '../guestbook/GuestBook';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { useEffect } from 'react';

import AlarmList from '../alarm/AlarmList';
import { selectUser } from '@/stores/auth/authSelectors';
import { resetAlarms } from '../alarm/stores/alarmSlice';

const Navbar = () => {
  // 모달 열림 여부
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 모달 열기
  const openModal = () => setIsModalOpen(true);
  // 모달 닫기
  const closeModal = () => setIsModalOpen(false);

  //방명록을 열기 위해 유저정보 가져오기
  const params = useParams();
  const dispatch = useDispatch();

  const user = useSelector(selectUser);

  useEffect(() => {
    if (params.username) {
      console.log(params.username);
    }
  }, [params.username, dispatch]);

  //방명록 모달
  const [showGuestbook, setShowGuestbook] = useState(false);
  const onClickGuestBookModal = () => {
    console.log('방명록 버튼 클릭됨:', showGuestbook);
    setShowGuestbook(!showGuestbook);
  };

  // 알림 모달 열림 여부
  const [isAlarmOpen, setIsAlarmOpen] = useState(false);

  // 모달 열기
  const openAlarm = async () => {
    setIsAlarmOpen(true);
  };
  // 모달 닫기
  const closeAlarm = () => {
    setIsAlarmOpen(false);
    dispatch(resetAlarms());
  };

  return (
    <>
      <nav className="fixed top-0 flex w-fit left-1/2 transform -translate-x-1/2 text-2xl text-gray-200/85 items-center justify-center gap-15 p-6 bg-transparent z-50">
        {/* home */}
        <Link
          to={`/${user?.username}`}
          reloadDocument
          className="hover:text-white"
          data-tooltip-id="home-tooltip"
          data-tooltip-content="홈으로 이동">
          <MdHomeFilled />
        </Link>
        {/* Rocket */}
        <div
          onClick={onClickGuestBookModal}
          className="hover:text-white cursor-pointer"
          data-tooltip-id="guestbook-tooltip"
          data-tooltip-content="방명록">
          <BsEnvelopePaperHeartFill className="w-5 h-5" />
        </div>
        {/* search (클릭하면 모달 열림) */}
        <VscSearch
          onClick={openModal}
          className="hover:text-white cursor-pointer w-5 h-5"
          data-tooltip-id="search-tooltip"
          data-tooltip-content="검색하기"
        />
        {/* bell */}
        <FaRegBell
          onClick={isAlarmOpen ? closeAlarm : openAlarm} // 알림창 뜨기
          className="hover:text-white cursor-pointer w-5 h-5"
          data-tooltip-id="notification-tooltip"
          data-tooltip-content="알림"
        />
      </nav>
      {/* 툴팁 컴포넌트들 */}
      <Tooltip
        id="home-tooltip"
        place="bottom"
        style={{ zIndex: 9999 }}
      />
      <Tooltip
        id="guestbook-tooltip"
        place="bottom"
        style={{ zIndex: 9999 }}
      />
      <Tooltip
        id="search-tooltip"
        place="bottom"
        style={{ zIndex: 9999 }}
      />
      <Tooltip
        id="notification-tooltip"
        place="bottom"
        style={{ zIndex: 9999 }}
      />

      {/* SearchModal 컴포넌트 */}
      <SearchModal
        isOpen={isModalOpen}
        onClose={closeModal}
      />

      {/* 방명록 모달 (조건부 렌더링) */}
      {showGuestbook && <GuestBook onClose={onClickGuestBookModal} />}

      {/* 알림 컴포넌트 */}
      <AlarmList
        isOpen={isAlarmOpen}
        onClose={closeAlarm}
      />
    </>
  );
};

export default Navbar;
