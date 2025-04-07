import '../styles/MyInformationContent.css';
import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import close from '@/assets/images/closeEye.svg';
import open from '@/assets/images/openEye.svg';
import check from '@/assets/images/pixcelCheck.svg';
import docs from '@/assets/images/pixcelDoc.svg';
import api from '@/apis/apiClient';
import { logoutUser } from '@/stores/auth/authThunks';

interface LeftProfileSectionProps {
  userData: {
    birthDate: string;
    iconSeq: number;
    introduction: string | null;
    nickname: string;
    userSeq: number;
    username: string;
  } | null;
}

const RightProfileSection: React.FC<LeftProfileSectionProps> = ({
  userData,
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [showPw_Check, setShowPassword_Check] = useState(false);
  const [nickname, setNickname] = useState('');
  const [nicknameCheck, setNicknameCheck] = useState(false);
  const [password, setPassword] = useState('');
  const [password_check, setPassword_check] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [passwordMatch, setPasswordMatch] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  const datePickerRef = useRef<HTMLDivElement>(null);

  const nav = useNavigate();

  const onClickCancle = () => {
    nav('/spaceship');
  };

  useEffect(() => {
    if (userData) {
      setNickname(userData.nickname);
      setBirthDate(formatDateInput(userData.birthDate));

      // birthDate가 유효한 형식이면 Date 객체로 변환
      if (userData.birthDate) {
        const formattedDate = formatDateString(userData.birthDate);
        if (formattedDate) {
          setSelectedDate(new Date(formattedDate));
        }
      }
    }
  }, [userData]);

  useEffect(() => {
    // 날짜 선택기 외부 클릭 시 닫기
    const handleClickOutside = (event: MouseEvent) => {
      if (
        datePickerRef.current &&
        !datePickerRef.current.contains(event.target as Node)
      ) {
        setShowDatePicker(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // 날짜 문자열을 YYYY-MM-DD 형식으로 변환
  const formatDateString = (dateStr: string): string | null => {
    // 숫자만 추출
    const numbers = dateStr.replace(/\D/g, '');

    if (numbers.length === 8) {
      const year = numbers.substring(0, 4);
      const month = numbers.substring(4, 6);
      const day = numbers.substring(6, 8);
      return `${year}-${month}-${day}`;
    }

    return null;
  };

  // 입력된 날짜 문자열 자동 포맷팅
  const formatDateInput = (value: string): string => {
    const formatted = formatDateString(value);
    return formatted || value;
  };

  const handleBirthDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setBirthDate(value);

    // 자동 포맷팅 적용
    if (value.length === 8 && /^\d+$/.test(value)) {
      const formattedDate = formatDateInput(value);
      setBirthDate(formattedDate);

      // Date 객체도 업데이트
      const year = parseInt(value.substring(0, 4));
      const month = parseInt(value.substring(4, 6)) - 1; // JS의 월은 0부터 시작
      const day = parseInt(value.substring(6, 8));
      setSelectedDate(new Date(year, month, day));
    }
  };

  const handleDateSelect = (date: Date) => {
    setSelectedDate(date);

    // YYYY-MM-DD 형식으로 변환
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    setBirthDate(`${year}-${month}-${day}`);

    setShowDatePicker(false);
  };

  const toggleDatePicker = () => {
    setShowDatePicker(!showDatePicker);
  };

  const handleNicknameCheck = async () => {
    try {
      if (nickname === userData?.nickname) {
        alert('현재 당신의 닉네임입니다.');
        return;
      }
      const response = await api.get(`/auth/check-nickname/${nickname}`);
      console.log(response);

      if (response && response.status === 200) {
        setNicknameCheck(true);
      } else {
        setNicknameCheck(false);
      }
    } catch (error) {
      console.error('닉네임 중복체크 도중 오류가 발생하였습니다.', error);
      setNicknameCheck(false);
    }
  };

  const handleSubmit = async () => {
    if (password !== password_check) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    if (!nickname) {
      alert('닉네임을 입력해주세요');
      return;
    }

    if (!birthDate) {
      alert('생년월일을 입력해주세요');
      return;
    }
    if (!nicknameCheck) {
      if (nickname === userData?.nickname) {
        setNicknameCheck(true);
      } else {
        alert('닉네임 중복체크를 해주세요');
        return;
      }
    }

    setIsLoading(true);

    try {
      // 백엔드에 전송할 때는 하이픈을 제거한 형식으로 변환
      const birthDateFormatted = birthDate.replace(/-/g, '');

      const response = await api.put('/users/me', {
        nickname,
        password,
        birthDate: birthDateFormatted,
      });
      console.log('회원정보 수정 응답', response);
      if (response.data && response.data.status === 200) {
        nav('/successedit');
      } else {
        nav('/failedit');
      }
    } catch (error) {
      console.error('회원정보 수정 실패:', error);
      nav('/failedit');
    } finally {
      setIsLoading(false);
    }
  };

  const handleWithDraw = async () => {
    try {
      const response = await api.delete('/users/me');
      if (response.data.status === 204) {
        console.log(response.data.message);
      } else if (response.data.status === 400) {
        console.log(response.data.message);
      }
    } catch (error) {
      console.error('회원탈퇴에 실패하였습니다.');
    } finally {
      logoutUser();
      window.location.reload();
    }
  };

  return (
    <div className="text-white flex-1">
      <div className="w-full h-full flex flex-col items-center justify-center gap-10 px-10 pl-30 pb-10 text-[#86F5FF]">
        {/* 닉네임 행 */}
        <div className="flex items-center gap-4 text-xl w-full dung-font relative">
          <p className="w-1/4">nickname</p>
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            className="flex-1 bg-transparent focus:outline-none pl-8"
          />
          <img
            src={check}
            alt="닉네임 확인"
            className="w-6 h-6 cursor-pointer absolute right-0"
            onClick={handleNicknameCheck}
          />
        </div>

        {/* 비밀번호 입력 행 */}
        <div className="flex items-center gap-8 text-xl w-full dung-font relative">
          <p className="w-1/4">password</p>
          <div className="flex items-center gap-2 w-3/4 overflow-visible">
            <input
              type={showPassword ? 'text' : 'password'}
              className="w-[235px] focus:outline-none rounded bg-transparent"
              placeholder="비밀번호를 입력하세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              onClick={() => setShowPassword(!showPassword)}
              className="cursor-pointer flex-shrink-0 ml-2 absolute right-0">
              {showPassword ? (
                <img
                  src={close}
                  alt="비밀번호 숨기기"
                  className="w-6 h-6"
                />
              ) : (
                <img
                  src={open}
                  alt="비밀번호 표시"
                  className="w-6 h-6"
                />
              )}
            </button>
          </div>
        </div>

        {/* 비밀번호 확인 입력 행 */}
        <div className="flex items-center gap-8 text-xl w-full dung-font relative">
          <p className="w-1/4">pw_check</p>
          <div className="flex items-center gap-2 w-3/4 overflow-visible">
            <input
              type={showPw_Check ? 'text' : 'password'}
              className="w-[235px] focus:outline-none rounded bg-transparent"
              placeholder="비밀번호를 입력하세요"
              value={password_check}
              onChange={(e) => setPassword_check(e.target.value)}
            />
            <button
              onClick={() => setShowPassword_Check(!showPw_Check)}
              className="cursor-pointer flex-shrink-0 ml-2 absolute right-0">
              {showPw_Check ? (
                <img
                  src={close}
                  alt="비밀번호 숨기기"
                  className="w-6 h-6"
                />
              ) : (
                <img
                  src={open}
                  alt="비밀번호 표시"
                  className="w-6 h-6"
                />
              )}
            </button>
          </div>
        </div>

        {/* 생년월일 행 */}
        <div className="flex items-center gap-8 text-xl w-full relative">
          <div className="w-1/4 dung-font">
            <p>birth</p>
          </div>
          <div className="flex items-center gap-2 w-3/4 overflow-visible">
            <input
              type="text"
              onChange={handleBirthDateChange}
              className="flex-1 bg-transparent focus:outline-none dung-font pl-8"
              value={birthDate}
              placeholder="YYYYMMDD"
            />
            <img
              src={docs}
              alt="생년월일 선택"
              className="w-6 h-6 cursor-pointer absolute right-0"
              onClick={toggleDatePicker}
            />
          </div>

          {/* 날짜 선택기 */}
          {showDatePicker && (
            <div
              ref={datePickerRef}
              className="absolute left-103 top-0 z-10 bg-gray-800 p-2 rounded">
              <DatePicker
                selected={selectedDate}
                onChange={handleDateSelect}
                inline
                dateFormat="yyyy-MM-dd"
                showYearDropdown
                scrollableYearDropdown
                yearDropdownItemNumber={100}
                maxDate={new Date()}
              />
            </div>
          )}
        </div>

        {/* 취소/수정 버튼 */}
        <div className="flex gap-5 text-black dung-font">
          <div className="box-button">
            <button
              onClick={handleWithDraw}
              className="infor-button">
              탈퇴
            </button>
          </div>

          <div className="box-button">
            <button
              onClick={handleSubmit}
              className="infor-button">
              수정
            </button>
          </div>

          <div className="box-button">
            <button
              onClick={onClickCancle}
              className="infor-button">
              취소
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RightProfileSection;
