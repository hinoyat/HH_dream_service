import SignupForm from '@/domains/signup/components/SignupForm';
import StarField from '@/domains/mainpage/components/universe/StarField';
import { Canvas } from '@react-three/fiber';

const Signup = () => {
  return (
    <div className="relative h-screen w-screen">
      {/* 우주배경 */}
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

      {/* 회원가입 폼을 중앙에 배치하기 위한 컨테이너 */}
      <div className="absolute inset-0 flex items-center justify-center z-10">
        {/* 회원가입 폼 컴포넌트 */}
        <SignupForm />
      </div>
    </div>
  );
};

export default Signup;
