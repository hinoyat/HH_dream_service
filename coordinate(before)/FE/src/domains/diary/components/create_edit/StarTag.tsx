// 별자리를 이을 기준 태그 (감정태그)

import React, { useState, useRef, useEffect } from 'react';

interface StarTagProps {
  onSelect: (emotion: string) => void;
  initialEmotion?: string;
}

const StarTag: React.FC<StarTagProps> = ({ onSelect, initialEmotion }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedEmotion, setSelectedEmotion] = useState(initialEmotion || '');
  const dropdownRef = useRef<HTMLDivElement>(null);

  const emotions = ['행복', '슬픔', '분노', '불안', '평화', '희망', '공포'];

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // initialEmotion이 변경될 때 selectedEmotion도 업데이트
  useEffect(() => {
    if (initialEmotion) {
      setSelectedEmotion(initialEmotion);
      onSelect(initialEmotion);
    }
  }, [initialEmotion, onSelect]);

  const handleEmotionSelect = (emotion: string) => {
    setSelectedEmotion(emotion);
    setIsOpen(false);
    onSelect(emotion);
  };

  return (
    <div
      className="relative w-full"
      ref={dropdownRef}>
      {/* 선택 버튼 */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between w-full px-3 py-2 border border-white focus:border-2 text-white rounded text-sm">
        <span>{selectedEmotion || '선택'}</span>
        <span className="ml-2">{isOpen ? '▲' : '▼'}</span>
      </button>

      {/* 드롭다운 목록 */}
      {isOpen && (
        <div className="absolute z-10 w-full mt-1 bg-black/30 backdrop-blur-md border border-white/30 rounded shadow-lg text-sm">
          <ul className="py-1">
            {emotions.map((emotion) => (
              <li
                key={emotion}
                onClick={() => handleEmotionSelect(emotion)}
                className="px-3 py-2 text-white cursor-pointer hover:bg-white/10">
                {emotion}
              </li>
            ))}
          </ul>
        </div>
      )}

      <p className="text-gray-300 text-xs mt-1 italic">
        * 선택한 감정태그를 바탕으로 별자리가 생성됩니다
      </p>
    </div>
  );
};

export default StarTag;
