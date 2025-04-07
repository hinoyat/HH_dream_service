import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Canvas } from '@react-three/fiber';
import StarField from '@/domains/mainpage/components/universe/StarField';
import '../themes/Intro.css';

function Intro() {
  const nav = useNavigate();
  const [animationStarted, setAnimationStarted] = useState(false);

  useEffect(() => {
    // 컴포넌트가 마운트되면 바로 애니메이션 시작
    setAnimationStarted(true);
  }, []);

  const handleGoStart = () => {
    nav('/login');
  };

  return (
    <div className="hero-section">
      <div
        className={`space-scene-container ${animationStarted ? 'animate-zoom-in' : ''}`}
        style={{
          position: 'absolute',
          width: '100%',
          height: '100vh',
          zIndex: 0,
        }}>
        <Canvas
          camera={{ position: [0, 0, -30], fov: 90, far: 5000 }}
          style={{
            background: 'black',
            width: '100vw',
            height: '100vh',
          }}>
          <StarField />
        </Canvas>
      </div>
      <div className="absolute transform -translate-y-1/2 top-1/2 -translate-x-1/2 left-1/2 flex flex-col items-center gap-10">
        <div className="flex flex-col text-white items-center gap-5">
          <h1 className="text-white text-4xl tracking-widest dung-font">
            우리들의 꿈 다이어리
          </h1>
          <p className="text-gray-500 text-[20px] text-center dung-font">
            우리는 매일 밤 꿈을 꿉니다.
            <br />
            그 꿈을 기록하면, 우주에 하나의 별이 됩니다.
            <br />
            당신이 꾸었던 꿈을 일기로 남겨보세요.
            <br />
            오늘의 꿈은 어떤 별이 될까요? 🌌
          </p>
        </div>
        <button
          className="shadow__btn cursor-pointer dung-font"
          onClick={handleGoStart}>
          시작하기
        </button>
      </div>
    </div>
  );
}

export default Intro;
