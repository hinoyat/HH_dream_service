import { AiOutlineEnter } from 'react-icons/ai';
import { useNavigate } from 'react-router-dom';
import { IoClose } from 'react-icons/io5';
import passwordFrame from '@/assets/images/passwordFrame.svg';
import enter from '@/assets/images/enter.svg';
import { useState } from 'react';
import api from '@/apis/apiClient';

const PasswordCheckModal = () => {
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const glowingTextStyle = {
    textShadow: '0 0 5px #9decf9, 0 0 10px #9decf9, 0 0 15px #67e8f9',
  };

  const nav = useNavigate();

  const handleSubmit = async() => {
    if(!password) {
      setErrorMessage("비밀번호를 입력해주세요");
      return;
    }

    setIsLoading(true);
    setErrorMessage("");
    try {
      const response = await api.post('/users/me/check-password', {
        password: password
      });
      
      // 성공적으로 응답을 받았을 때
      if(response.data.status === 200) {
        nav('/myinformation');
      } else {
        // 서버에서 200이 아닌 상태 코드를 반환한 경우 (이 부분은 API 구현에 따라 다를 수 있음)
        setErrorMessage(response.data.message || '비밀번호가 일치하지 않습니다.');
        setPassword("");
      }
    } catch(error) {
      console.error("비밀번호 확인 오류: ", error);
      
      // 에러 객체에서 응답 데이터 가져오기
      if (error.response) {
        // 서버가 응답을 반환했지만 2xx 범위가 아닌 상태 코드
        setErrorMessage(error.response.data?.message || '비밀번호가 일치하지 않습니다.');
      } else if (error.request) {
        // 요청이 만들어졌으나 응답을 받지 못한 경우
        setErrorMessage('서버 응답이 없습니다. 네트워크 연결을 확인해주세요.');
      } else {
        // 요청을 설정하는 중에 문제가 발생한 경우
        setErrorMessage('요청 중 오류가 발생했습니다.');
      }
      
      // 비밀번호 초기화
      setPassword("");
    } finally {
      setIsLoading(false);
    }
  };

  const onClickHome = () => {
    nav('/spaceship');
  };

  // 엔터 키 입력 이벤트
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  };
  
  const handlePasswordChange = (e) => {
    setPassword(e.target.value);
    if(errorMessage) {
      setErrorMessage("");
    }
  }

  return (
    <div className="absolute inset-0">
      <div
        className="absolute z-40 top-[2%] right-[1%] cursor-pointer"
        onClick={onClickHome}>
        <IoClose className="text-white text-4xl" />
      </div>
      {/* 전체 영역을 덮는 어두운 오버레이 */}
      <div className="absolute inset-0 bg-black opacity-50 z-20"></div>
      <img
        src={passwordFrame}
        alt=""
        className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-30  animate-pulse w-[600px]"
      />
      <div className="absolute top-[43%] left-1/2 transform -translate-x-1/2 z-50 tracking-widest animate-pulse flex flex-col items-center gap-5">
        <h1
          className="text-white text-xl press-font "
          style={glowingTextStyle}>
          ENTER PASSWORD!!!
        </h1>

        {/* 비밀번호 동그라미, 엔터 아이콘 */}
        <div className="flex items-center justify-center">
          <input
            type="password"
            value={password}
            onChange={handlePasswordChange}
            disabled={isLoading}
            className="bg-transparent text-cyan-300 text-[40px] w-110 h-15 focus:outline-none"
            onKeyDown={handleKeyDown}
          />
          <button
            onClick={handleSubmit}
            disabled={isLoading}
            className="cursor-pointer">
            <img
              src={enter}
              alt="enter"
              className="w-6"
            />
          </button>
        </div>
        {errorMessage && (
           <div className="text-red-500 text-sm mt-2 press-font absolute bottom-0 ">
           {errorMessage}
         </div>
        )}
        {isLoading && (
          <div className="text-cyan-300 text-sm mt-2 press-font">
            확인 중...
          </div>
        )}
      </div>
    </div>
  );
};

export default PasswordCheckModal;