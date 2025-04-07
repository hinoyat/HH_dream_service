// 로그인 페이지

import LoginForm from '@/domains/login/components/LoginForm';
import StarField from '@/domains/mainpage/components/universe/StarField';
import { Canvas } from '@react-three/fiber';
import '../themes/SpaceLoginForm.css';
import wormholeVideo from '@/assets/video/wormhole.mp4';

const Login = () => {
  return (
    <div className="relative h-screen w-screen overflow-hidden">
      {/* 우주배경 */}
      <div className="absolute inset-0 flex items-center justify-center showWormHole z-5">
        <video
          src={wormholeVideo}
          autoPlay
          loop
          muted
          playsInline
          style={{ width: '100%', height: '100%', objectFit: 'fill' }}></video>
      </div>
      <div className="absolute inset-0">
        <Canvas
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: 'black',
            zIndex: 0,
          }}
          camera={{ position: [0, 0, 5] }}>
          {/* 카메라 위치 조정 */}
          <StarField />
        </Canvas>
      </div>

      {/* 로그인 폼을 중앙에 배치하기 위한 컨테이너 */}
      <div className="absolute inset-0 flex items-center justify-center z-10 LoginAppear">
        {/* 로그인 폼 컴포넌트 */}
        <LoginForm />

        {/* 오른쪽 이미지 */}
        {/* <div className="h-[75vh]">
          <img
            src={login_image}
            alt="loginImage"
            className="object-contain h-full rounded"
          />
        </div> */}
      </div>
    </div>
  );
};

export default Login;
