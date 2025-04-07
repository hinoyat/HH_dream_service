import React, { useState, useEffect } from 'react';
import '../../styles/DiaryDisclose.css';

interface DiaryDiscloseProps {
  isPublic?: boolean; // 공개 여부 초기값
  onToggle?: (isPublic: boolean) => void; // 공개 여부 변경 핸들러
}

const DiaryDisclose: React.FC<DiaryDiscloseProps> = ({
  isPublic = true,
  onToggle,
}) => {
  const [publicStatus, setPublicStatus] = useState<boolean>(isPublic);

  // 초기값 변경 시 상태 업데이트
  useEffect(() => {
    setPublicStatus(isPublic);
  }, [isPublic]);

  // 공개 여부 변경 핸들러
  const handleToggle = (value: boolean) => {
    setPublicStatus(value);
    if (onToggle) {
      onToggle(value);
    }
  };

  return (
    <div className="flex flex-col my-4">
      <h1 className="text-white text-base font-semibold">공개 범위</h1>
      <div className="flex justify-around text-sm">
        <label className="radio-button">
          <input
            type="radio"
            name="example-radio"
            value="public"
            checked={publicStatus}
            onChange={() => handleToggle(true)}
          />
          <span className="radio"></span>
          공개
        </label>

        <label className="radio-button">
          <input
            type="radio"
            name="example-radio"
            value="private"
            checked={!publicStatus}
            onChange={() => handleToggle(false)}
          />
          <span className="radio"></span>
          비공개
        </label>
      </div>
    </div>
  );
};

export default DiaryDisclose;
