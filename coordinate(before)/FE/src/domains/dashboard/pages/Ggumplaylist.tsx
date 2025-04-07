import { useNavigate } from 'react-router-dom';
import MusicSection from '../components/MusicSection';
import GraphSection from '../components/GgumGraphSection';
import UserInfoData from '../components/UserInfoData';
import dreamSolve_bg from '@/assets/images/dreamSolve_bg.svg';
import { Canvas } from '@react-three/fiber';
import StarField from '@/domains/mainpage/components/universe/StarField';


const DreamSolve: React.FC = () => {
  const nav = useNavigate();
  const onClickHome = () => {
    nav('/spaceship');
  };

  return (
    <div className="w-screen h-screen relative">
      <Canvas
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          background: 'black',
        }}
        camera={{ position: [0, 0, 5] }}>
        <StarField />
      </Canvas>

      {/* 닫기버튼 */}
      <div className="absolute top-3 right-3 text-2xl text-white">
        <button
          onClick={onClickHome}
          className="cursor-pointer hover:text-gray-200">
          ✕
        </button>
      </div>

      <div className="absolute w-[93%] h-[88%] top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
        <div className="w-full h-full flex flex-col">
          {/* UserInfoData 중앙 상단에 배치 */}
          <div className="w-full flex justify-center mb-6">
            <UserInfoData />
          </div>
          
          {/* GraphSection과 MusicSection 가로 배치 */}
          <div className="w-full flex relative">
            {/* 왼쪽에 GraphSection */}
            <div className="w-3/5 pl-5">
              <GraphSection />
            </div>
            
            {/* 오른쪽에 MusicSection */}
            <div className="w-2/5 pl-3 relative h-[20px]">
              <MusicSection />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DreamSolve;
