import React from 'react';
import Today from './Today';
import { IoClose } from 'react-icons/io5';

interface DiaryHeaderProps {
  onClose: () => void;
  isEditing?: boolean; // 수정 모드 여부 추가
}

const DiaryHeader: React.FC<DiaryHeaderProps> = ({
  onClose,
  isEditing = false,
}) => {
  return (
    <div className="flex justify-between relative">
      <div className="flex flex-col">
        <h1 className="text-[20px] font-semibold text-white">
          {isEditing ? '꿈 일기를 수정해보세요' : '오늘은 무슨 꿈을 꾸었나요?'}
        </h1>
        <Today />
      </div>

      <div
        className="absolute z-40 top-[7%] right-[0%] cursor-pointer"
        onClick={onClose}>
        <IoClose className="text-[rgba(255,255,255,0.7)] text-2xl" />
      </div>
    </div>
  );
};

export default DiaryHeader;
