// 검색 모달. 탭 전환

import React, { useState } from 'react';
import DiarySearch from '../components/DiarySearch';
import UserSearch from '../components/UserSearch';
import { IoClose } from 'react-icons/io5';

interface SearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const SearchModal: React.FC<SearchModalProps> = ({ isOpen, onClose }) => {
  const [activeTab, setActiveTab] = useState<'diary' | 'user'>('diary');

  if (!isOpen) return null;

  const handleClose = () => {
    onClose(); // 먼저 모달 닫기 함수 호출

    // 약간의 지연 후 새로고침 (모달이 시각적으로 사라지는 것을 확인할 수 있도록)
    setTimeout(() => {
      window.location.reload();
    }, 10); // 50ms 정도의 짧은 지연
  };

  return (
    <div className="absolute inset-0">
      <div className="absolute inset-0 z-10 backdrop-blur-[2px]"></div>
      {/* 모달 배경 */}
      <div className="absolute w-[70%] h-[80%] rounded-lg py-10 px-20 top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-[#6E6E6E]/90 z-25 flex flex-col items-center">
        {/* 닫기버튼 */}
        <div
          className="absolute z-40 top-[2%] right-[1%] cursor-pointer"
          onClick={handleClose}>
          <IoClose className="text-white text-3xl" />
        </div>
        {/* 검색탭 */}
        <div className="flex gap-10 text-white text-xl">
          <button
            onClick={() => setActiveTab('diary')}
            className={`${
              activeTab === 'diary'
                ? 'font-bold underline underline-offset-6'
                : 'hover:font-bold hover:underline hover:underline-offset-5'
            } cursor-pointer`}>
            일기검색
          </button>
          <p>|</p>
          <button
            onClick={() => setActiveTab('user')}
            className={`${
              activeTab === 'user'
                ? 'font-bold underline underline-offset-6'
                : 'hover:font-bold hover:underline hover:underline-offset-5'
            } cursor-pointer`}>
            유저검색
          </button>
        </div>

        {activeTab === 'diary' && <DiarySearch onClose={onClose} />}
        {activeTab === 'user' && <UserSearch onClose={onClose} />}
      </div>
    </div>
  );
};

export default SearchModal;
