import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import '@/domains/login/themes/SpaceLoginForm.css';
import { useAppDispatch } from '@/hooks/hooks';
import { loginUser } from '@/stores/auth/authThunks';

const LoginForm = () => {
  const dispatch = useAppDispatch();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const nav = useNavigate();

  const handleLogin = async (event: any) => {
    event.preventDefault();
    await dispatch(loginUser({ username, password }));
    await nav(`/${username}`, { replace: true }); // 여기 `/${username}` 내 우주 주인장 닉네임 넣기
  };

  const onClickGoToSignup = () => {
    nav('/signup');
  };
  const onClickGoToHome = () => {
    nav('/intro');
  };

  return (
    <div className="flex flex-col items-center">
      <form className="form">
        {/* 폼 타이틀 부분 */}
        <div className="form-title">
          <span className="dung-font">sign in to your</span>
        </div>
        <div className="title-2">
          <span>SPACE</span>
        </div>

        {/* 로그인 입력 폼 피그마 그대로 했는데 아이디랑 비밀번호 border-b gray-200 했더니 그냥 안보여서 400으로 up */}
        {/* 아이디 입력 부분 - placeholder 한국어로 유지 */}
        <div className="input-container">
          <input
            className="input-mail"
            type="text"
            placeholder="아이디를 입력하세요"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <span> </span>
        </div>

        {/* 별이 흐르는 배경 효과 */}
        <section className="bg-stars">
          <span className="star"></span>
          <span className="star"></span>
          <span className="star"></span>
          <span className="star"></span>
        </section>

        {/* 비밀번호 입력 부분 - 기존 코드의 보이기/숨기기 기능 유지 */}
        <div className="input-container">
          <div style={{ position: 'relative', width: '100%' }}>
            <input
              className="input-pwd"
              type={showPassword ? 'text' : 'password'}
              placeholder="비밀번호를 입력하세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {/* 비밀번호 보이고 안보이게 하는거 */}
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              style={{
                position: 'absolute',
                right: '10px',
                top: '35%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#333',
              }}>
              {showPassword ? <FaEyeSlash size={20} /> : <FaEye size={20} />}
            </button>
          </div>
        </div>

        {/* 로그인 버튼 */}
        <button
          onClick={handleLogin}
          // type="submit"
          className="submit">
          <span className="sign-text dung-font">Login</span>
        </button>

        {/* 회원가입 링크 */}
        <p className="signup-link">
          Don't have an account?{' '}
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              onClickGoToSignup();
            }}
            className="up">
            Signup Here
          </a>
        </p>
        {/*버튼 영역 종료 */}
        {/*로그인 폼 종료 */}
        {/* 로그인 패딩 준거 종료 */}
      </form>

      <button
        type="button"
        onClick={onClickGoToHome}
        className="mt-4 flex items-center text-white hover:text-gray-300 transition-colors duration-300 cursor-pointer"
        style={{
          fontFamily: 'monospace',
          fontSize: '1rem',
        }}>
        뒤로가기
      </button>
    </div>
  );
};

export default LoginForm;
