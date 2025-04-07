import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import '@/domains/login/themes/SpaceLoginForm.css';
import '@/domains/signup/themes/BoxButton.css';
import BoxButton from '@/domains/signup/components/BoxButton';
import ProfileIconSelector from '@/domains/signup/components/ProfileIconSelector';
import { useAppDispatch } from '@/hooks/hooks';
import { useSelector } from 'react-redux';
import {
  selectUsernameMessage,
  selectUsernameStatus,
  selectNickname,
  selectNicknameMessage,
  selectNicknameStatus,
  selectUsername,
  selectBirthDate,
  selectBirthDateMessage,
  selectBirthDateStatus,
  selectPassword,
  selectPasswordMessage,
  selectPasswordStatus,
  selectConfirmPassword,
  selectConfirmPasswordMessage,
  selectConfirmPasswordStatus,
  selectIconSeq,
} from '../stores/signupSelectors';
import {
  setUsername,
  setIconSeq,
  setNickname,
  setBirthDate,
  setPassword,
  setConfirmPassword,
} from '../stores/signupSlice';
import {
  checkUsername,
  checkNickname,
  signupUser,
} from '../stores/signupThunks';
import { events } from '@react-three/fiber';

// 프로필 아이콘 이미지 추가
const SignupForm = () => {
  const dispatch = useAppDispatch();
  const nav = useNavigate();
  const [showPassword, setShowPassword] = useState(false);

  const username = useSelector(selectUsername);
  const usernameMessage = useSelector(selectUsernameMessage);
  const IsUsernameDuplicate = useSelector(selectUsernameStatus);

  const nickname = useSelector(selectNickname);
  const nicknameMessage = useSelector(selectNicknameMessage);
  const IsNicknameDuplicate = useSelector(selectNicknameStatus);

  const birthDate = useSelector(selectBirthDate);
  const birthDateMessage = useSelector(selectBirthDateMessage);
  const IsBirthDate = useSelector(selectBirthDateStatus);

  const password = useSelector(selectPassword);
  const passwordMessage = useSelector(selectPasswordMessage);
  const IsPassword = useSelector(selectPasswordStatus);

  const confirmPassword = useSelector(selectConfirmPassword);
  const confirmPasswordMessage = useSelector(selectConfirmPasswordMessage);
  const IsConfirmPassword = useSelector(selectConfirmPasswordStatus);

  const iconSeq = useSelector(selectIconSeq);

  const onClickGoToHome = () => {
    nav('/intro');
  };

  const onClickGoToLogin = () => {
    nav('/login');
  };

  // 아이콘 선택 핸들러
  const handleIconSelect = (iconSrc: string, iconIndex: number): void => {
    dispatch(setIconSeq(iconIndex));
    console.log('선택된 아이콘:', iconSrc, '인덱스:', iconIndex);
  };

  // 아이디
  // 아이디 중복확인 핸들러
  const handleIDCheckDuplicate = () => {
    dispatch(checkUsername(username));
    console.log('중복 확인할 아이디:', username);
  };

  // 아이디 유효성 검사
  const handleUsernameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    dispatch(setUsername(event.target.value));
  };

  // 닉네임
  // 닉네임 중복확인 핸들러
  const handleNicknameCheckDuplicate = () => {
    dispatch(checkNickname(nickname));
    console.log('중복 확인할 닉네임:', nickname);
  };

  // 닉네임 유효성 검사
  const handleNicknameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    dispatch(setNickname(event.target.value));
  };

  // 생일
  const handleBirthDateChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    dispatch(setBirthDate(event.target.value));
  };

  // 비밀번호
  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    dispatch(setPassword(event.target.value));
  };

  const handleConfirmPasswordChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    dispatch(setConfirmPassword(event.target.value));
  };

  // 회원가입
  const validStates = {
    IsUsernameDuplicate,
    IsNicknameDuplicate,
    IsBirthDate,
    IsPassword,
    IsConfirmPassword,
  };

  const handleSignup = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (Object.values(validStates).every((status) => status === 'available')) {
      dispatch(
        signupUser({ username, nickname, birthDate, password, iconSeq })
      );
      nav('/login');
    } else {
      alert('회원가입 실패!');
    }
  };

  return (
    <div className="flex flex-col items-center">
      <form
        className="form"
        style={{ width: '750px', maxWidth: '95vw' }}>
        {/* 폼 타이틀 부분 */}
        <div
          className="form-title"
          style={{ marginBottom: '3px' }}>
          <span className="dung-font tracking-wider ">create your</span>
        </div>
        <div className="title-2">
          <span>ACCOUNT</span>
        </div>

        {/* 별이 흐르는 배경 효과 */}
        <section className="bg-stars">
          <span className="star"></span>
          <span className="star"></span>
          <span className="star"></span>
          <span className="star"></span>
        </section>

        {/* 두 칼럼으로 나누기 위한 컨테이너 */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            gap: '45px',
            marginTop: '25px',
            marginBottom: '23px',
          }}>
          {/* 왼쪽 칼럼: 아이디, 비밀번호, 비밀번호 확인 */}
          <div style={{ flex: 1 }}>
            {/* 아이디 입력 부분 */}
            <div
              className="input-container"
              style={{
                display: 'flex',
                flexDirection: 'column', // 세로 방향으로 변경
                width: '100%',
                marginTop: '20px',
              }}>
              <div style={{ display: 'flex', gap: '8px', width: '100%' }}>
                <input
                  className="input-mail"
                  type="text"
                  placeholder="아이디를 입력하세요"
                  style={{ flex: 3, width: 'auto' }}
                  value={username}
                  onChange={handleUsernameChange}
                />

                <BoxButton
                  text="중복확인"
                  onClick={
                    IsUsernameDuplicate != 'invalid'
                      ? handleIDCheckDuplicate
                      : () => {}
                  }
                />
              </div>

              {/* 유효성 검사 메시지 */}
              <p
                className="validation-message dung-font"
                style={{
                  color:
                    IsUsernameDuplicate != 'invalid' ? '#4ade80' : '#ff6b6b', // 중복 확인용 색상 골라야할 듯
                  fontSize: '0.7rem',
                  marginTop: '4px',
                  marginLeft: '4px',
                  height: '16px', // 고정 높이로 공간 확보
                }}>
                {usernameMessage}
              </p>
            </div>

            {/* 비밀번호 입력 부분 */}
            <div
              className="input-container"
              style={{
                width: '100%',
                display: 'flex',
                flexDirection: 'column',
              }}>
              <div style={{ position: 'relative' }}>
                <input
                  className="input-mail"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 입력하세요"
                  style={{ width: '100%' }}
                  value={password}
                  onChange={handlePasswordChange}
                />
                {/* 비밀번호 보이고 안보이게 하는거 */}
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: 'absolute',
                    right: '12px',
                    top: '35%',
                    transform: 'translateY(-50%)',
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: '#333',
                    zIndex: 10,
                  }}>
                  {showPassword ? (
                    <FaEyeSlash size={16} />
                  ) : (
                    <FaEye size={16} />
                  )}
                </button>
              </div>

              <p
                className="validation-message dung-font"
                style={{
                  color: IsPassword != 'invalid' ? '#4ade80' : '#ff6b6b',
                  fontSize: '0.7rem',
                  marginTop: '4px',
                  marginLeft: '4px',
                  height: '16px',
                }}>
                {passwordMessage}
              </p>
            </div>

            {/* 비밀번호 확인 입력 부분 */}
            <div
              className="input-container"
              style={{
                width: '100%',
                display: 'flex',
                flexDirection: 'column',
              }}>
              <div style={{ position: 'relative' }}>
                <input
                  className="input-mail"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 한 번 더 입력하세요."
                  style={{ width: '100%' }}
                  value={confirmPassword}
                  onChange={handleConfirmPasswordChange}
                />
                {/* 비밀번호 보이고 안보이게 하는거 */}
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: 'absolute',
                    right: '12px',
                    top: '35%',
                    transform: 'translateY(-50%)',
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: '#333',
                    zIndex: 10,
                  }}>
                  {showPassword ? (
                    <FaEyeSlash size={16} />
                  ) : (
                    <FaEye size={16} />
                  )}
                </button>
              </div>

              <p
                className="validation-message dung-font"
                style={{
                  color: IsConfirmPassword != 'invalid' ? '#4ade80' : '#ff6b6b',
                  fontSize: '0.7rem',
                  marginTop: '4px',
                  marginLeft: '4px',
                  height: '16px',
                }}>
                {confirmPasswordMessage}
              </p>
            </div>
          </div>

          {/* 오른쪽 칼럼: 프로필 아이콘, 닉네임, 생년월일 */}
          <div style={{ flex: 1 }}>
            {/* 프로필 이미지 부분 */}

            <div
              style={{
                marginBottom: '15px',
                marginLeft: '23px',
                marginTop: '10.5px',
              }}>
              <ProfileIconSelector onSelectIcon={handleIconSelect} />
            </div>

            {/* 닉네임 입력 부분 */}
            <div
              className="input-container"
              style={{
                display: 'flex',
                flexDirection: 'column',
                width: '100%',
                marginTop: '26.5px',
              }}>
              <div
                style={{
                  display: 'flex',
                  gap: '8px',
                  width: '100%',
                }}>
                <input
                  className="input-mail"
                  type="text"
                  placeholder="닉네임을 입력하세요"
                  style={{ flex: 3, width: 'auto' }}
                  value={nickname}
                  onChange={handleNicknameChange}
                />
                <BoxButton
                  text="중복확인"
                  onClick={
                    IsNicknameDuplicate != 'invalid'
                      ? handleNicknameCheckDuplicate
                      : () => {}
                  }
                />
              </div>

              <p
                className="validation-message dung-font"
                style={{
                  color:
                    IsNicknameDuplicate != 'invalid' ? '#4ade80' : '#ff6b6b',
                  fontSize: '0.7rem',
                  marginTop: '4px',
                  marginLeft: '4px',
                  height: '16px',
                }}>
                {nicknameMessage}
              </p>
            </div>

            {/* 생년월일 입력 부분 */}
            <div
              className="input-container"
              style={{ width: '100%' }}>
              <input
                className="input-mail"
                type="text"
                placeholder="생년월일을 입력하세요(양력) (yyyymmdd)"
                style={{ width: '100%' }}
                value={birthDate}
                onChange={handleBirthDateChange}
                maxLength={8}
              />

              <p
                className="validation-message dung-font"
                style={{
                  color: IsBirthDate != 'invalid' ? '#4ade80' : '#ff6b6b', // 중복 확인용 색상 골라야할 듯
                  fontSize: '0.7rem',
                  marginTop: '4px',
                  marginLeft: '4px',
                  height: '16px', // 고정 높이로 공간 확보
                }}>
                {birthDateMessage}
              </p>
            </div>
          </div>
        </div>

        {/* 회원가입 버튼 */}
        <button
          type="submit"
          className="submit dung-font"
          style={{ marginTop: '20px' }}
          onClick={handleSignup}>
          <span className="sign-text dung-font">CREATE ACCOUNT</span>
        </button>

        {/* 로그인 링크 */}
        <p className="signup-link">
          Already have an account?{' '}
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              onClickGoToLogin();
            }}
            className="up">
            Log in
          </a>
        </p>
      </form>

      {/* 뒤로가기 버튼 */}
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

export default SignupForm;
