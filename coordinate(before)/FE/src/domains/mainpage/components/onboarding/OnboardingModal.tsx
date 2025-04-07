
import React, { useEffect, useState } from 'react';
import DoubleClickMouse from './DoubleClickMouse';

interface Slide {
  title: string;
  description: string;
  targetId?: string;
  imageUrl?: string;
  animation?: string;
}

interface OnboardingModalProps {
  slides: Slide[];
  step: number;
  onNext: (nextStep: number) => void;
  onClose: () => void;
}

const OnboardingModal: React.FC<OnboardingModalProps> = ({ slides, step, onNext, onClose }) => {
  const current = slides[step];
  const [highlightStyle, setHighlightStyle] = useState<React.CSSProperties>({});

  useEffect(() => {
    if (!current.targetId) return;
    const el = document.getElementById(current.targetId);
    if (el) {
      const rect = el.getBoundingClientRect();
      setHighlightStyle({
        top: rect.top - 8 + window.scrollY,
        left: rect.left - 8 + window.scrollX,
        width: rect.width + 16,
        height: rect.height + 16,
        position: 'absolute',
        border: '3px solid yellow',
        borderRadius: '8px',
        zIndex: 9999,
        pointerEvents: 'none',
        animation: 'pulse 2s infinite'
      });
    }
  }, [current.targetId]);

  return (
      <div className="fixed inset-0 z-50">
        <div className="absolute inset-0 bg-black" style={{ opacity: 0.7 }} />
        {/* 이미지 or 애니메이션 */}
        {current.imageUrl && (
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50 pointer-events-none">
            <img
              src={current.imageUrl}
              alt="onboarding"
              className="w-64 h-auto rounded-lg shadow-lg"
            />
          </div>
        )}
        {current.animation === 'double-click-animation' && (
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50 pointer-events-none">
            <DoubleClickMouse />
          </div>
        )}

        {/* 설명 박스 */}
        <div className="absolute bottom-20 left-1/2 transform -translate-x-1/2 z-60 bg-white p-6 rounded-lg shadow-lg max-w-md w-full text-black pointer-events-auto">
          <h2 className="text-lg font-bold mb-2">{current.title}</h2>
          <p className="mb-4">{current.description}</p>
          <div className="flex justify-end items-center space-x-2">
            {/* "이전" 버튼은 왼쪽에, "다음" 버튼은 오른쪽에 배치 */}
            {step > 0 && (
              <button
                onClick={() => onNext(step - 1)}
                className="bg-gray-300 text-black px-4 py-2 rounded"
              >
                이전
              </button>
            )}
            {step < slides.length - 1 ? (
              <button
                onClick={() => onNext(step + 1)}
                className="bg-blue-500 text-white px-4 py-2 rounded"
              >
                다음
              </button>
            ) : (
              <button
                onClick={onClose}
                className="bg-green-500 text-white px-4 py-2 rounded"
              >
                시작하기
              </button>
            )}
          </div>
        </div>
        {/* 강조 테두리 */}
        {current.targetId && (
          <div style={highlightStyle} />
        )}

        <style>{`
          @keyframes pulse {
            0% { box-shadow: 0 0 0 0 rgba(255, 255, 0, 0.7); }
            70% { box-shadow: 0 0 0 10px rgba(255, 255, 0, 0); }
            100% { box-shadow: 0 0 0 0 rgba(255, 255, 0, 0); }
          }
        `}</style>
      </div>
    );
  };

export default OnboardingModal;
