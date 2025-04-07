// 우주선 레이아웃 담당 부모 컴포넌트

import spaceShip_bg from '@/assets/images/spaceShip_bg.svg';
import StarField from '@/domains/mainpage/components/universe/StarField';
import { Canvas } from '@react-three/fiber';

interface SpaceShipComponentProps {
  children: React.ReactNode;
}

const SpaceShipComponent = ({ children }: SpaceShipComponentProps) => {
  return (
    <div className="relative w-screen h-screen">
      <div className="w-full h-full bg-black"></div>
      {/* 우주배경 */}
      <Canvas
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          zIndex: 10, // 기존 이미지와 동일한 z-index 사용
        }}
        camera={{ position: [0, 0, 5] }}>
        <StarField />
      </Canvas>

      <img
        src={spaceShip_bg}
        alt=""
        // className="absolute top-0 left-0 w-full h-full z-10 opacity-75"
        className="object-cover w-full h-full absolute left-0 top-0 z-10 opacity-75" // object-cover 사진이 잘리지 않게 함
      />
      {children}
    </div>
  );
};

export default SpaceShipComponent;
