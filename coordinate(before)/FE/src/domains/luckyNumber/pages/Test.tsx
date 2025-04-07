import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../style/LuckyNumber.css';
import { Canvas } from '@react-three/fiber';
import StarField from '@/domains/mainpage/components/universe/StarField';
import LuckyDrawButton from '@/domains/luckyNumber/components/LuckyDrawButton';

// Redux 상태에서 필요한 데이터 불러오기 위해 import
import { useSelector } from 'react-redux';
import { selectUser } from '@/stores/auth/authSelectors';
import api from '@/apis/apiClient';

const LuckyNumber = () => {
  const [visibleStars, setVisibleStars] = useState<number[]>([]);
  const [visibleLines, setVisibleLines] = useState<number[][]>([]);
  const [showText, setShowText] = useState(false);
  const [typingText, setTypingText] = useState('');
  const [visibleNumbers, setVisibleNumbers] = useState<number[]>([]);
  const [showButton, setShowButton] = useState(false); // 초기값을 false로 설정하여 API 응답 전까지 버튼이 보이지 않게 함
  const [animationStage, setAnimationStage] = useState(0);
  const [luckyNumbers, setLuckyNumbers] = useState<number[]>([]);
  const [dataLoaded, setDataLoaded] = useState(false); // API 데이터 로딩 상태 추적

  const user = useSelector(selectUser);

  useEffect(() => {
    const fetchLuckyNumbers = async () => {
      try {
        const response = await api.get('/lucky-numbers');
        // 응답 데이터에서 번호 배열 가져오기
        // response.data.data가 있는지 먼저 확인
        let numbers;
        numbers = response.data?.data ?? response.data;
        if (!Array.isArray(numbers)) {
          numbers = [];
        }
        setLuckyNumbers(numbers);

        // 번호가 있으면 바로 별자리 화면을 보여줌
        if (numbers && numbers.length > 0) {
          setShowButton(false); // 번호가 있으면 버튼 숨기기
          setShowText(true);
          setTypingText('오늘의 행운의 번호는'); // 타이핑 애니메이션 없이 바로 텍스트 설정

          // 가져온 번호를 표시할 별들에 맞게 설정
          if (numbers.length <= stars.length) {
            // API에서 받은 lucky number로 stars 배열 업데이트
            const updatedStars = [...stars];
            numbers.forEach((num, index) => {
              if (index < updatedStars.length) {
                updatedStars[index].id = num;
              }
            });
            setStars(updatedStars);

            // 별과 연결선 즉시 표시
            const allStarIds = updatedStars.map((star) => star.id);
            setVisibleStars(allStarIds);

            // 모든 연결선 표시
            const allConnections = connections.map((conn) => conn);
            setVisibleLines(allConnections);

            // 모든 번호 즉시 표시
            setVisibleNumbers(allStarIds);

            // 애니메이션 단계 4로 바로 설정
            setAnimationStage(4);
          }
        } else {
          setShowButton(true); // 번호가 없으면 버튼 표시
        }

        // 데이터 로딩 완료 표시
        setDataLoaded(true);
      } catch (error) {
        console.error('행운의 번호를 불러오는데 실패했습니다.', error);
        console.error('에러 세부 정보:', {
          message: error.message,
          response: error.response,
          request: error.request,
        });

        // 에러 발생 시에도 버튼 표시
        setShowButton(true);
        setDataLoaded(true);
      }
    };

    console.log('fetchLuckyNumbers 실행 (의존성 배열 빈 배열)');
    fetchLuckyNumbers();
  }, []);

  const nav = useNavigate();
  const onClickHome = () => {
    nav('/spaceship');
  };

  // 별자리 위치 옮기고 싶으면 여기 고치면 됩니다.!!
  // x 좌표는 커질수록 오른쪽으로
  // y 좌표는 커질수록 아래로 이동합니다.!
  // 고칠 때 참고 부탁드립니다.!
  // size는 별 자체 크기, glowSize는 주변 빛 효과 크기
  const [stars, setStars] = useState([
    { id: 16, x: 20, y: 56, size: 1.5, glowSize: 3.2 }, //  더 큰 별
    { id: 32, x: 33, y: 35, size: 0.9, glowSize: 2.0 }, //  작은 별
    { id: 27, x: 36, y: 68, size: 1.8, glowSize: 3.6 }, //  가장 큰 별
    { id: 3, x: 59, y: 44, size: 1.2, glowSize: 2.6 }, //  기본 크기
    { id: 15, x: 72, y: 22, size: 1.0, glowSize: 2.2 }, //  중간 크기
    { id: 9, x: 99, y: 20, size: 1.4, glowSize: 3.0 }, //  큰 별
  ]);

  const connections = [
    [0, 1],
    [1, 3],
    [3, 4],
    [4, 5],
    [3, 2],
    [2, 0],
  ];

  const handleDrawClick = async () => {
    console.log('행운의 번호 버튼 클릭 - 애니메이션 상태 변경');
    setShowButton(false); // 버튼 클릭 즉시 버튼 숨기기
    setShowText(true);
    setAnimationStage(1); // 애니메이션 시작

    try {
      // 버튼 클릭 시 새로운 행운의 번호 생성 API 호출
      const response = await api.post('/lucky-numbers');
      console.log('API 응답:', response.data);

      // API 응답 구조에 따라 데이터 처리
      let numbersFromResponse;

      // POST 요청 후 GET 요청을 통해 데이터 가져오기
      if (response.data.status === 201 || response.status === 201) {
        const getResponse = await api.get('/lucky-numbers');
        numbersFromResponse = getResponse.data?.data ?? getResponse.data;
      } else {
        // POST 응답에서 직접 데이터 가져오기
        numbersFromResponse = response.data?.data ?? response.data;
      }

      if (!Array.isArray(numbersFromResponse)) {
        numbersFromResponse = [];
      }

      setLuckyNumbers(numbersFromResponse);

      // 응답으로 받은 번호로 stars 배열 업데이트
      if (numbersFromResponse && numbersFromResponse.length > 0) {
        console.log(
          '응답으로 받은 번호로 별 업데이트 시작:',
          numbersFromResponse
        );
        const updatedStars = [...stars];
        numbersFromResponse.forEach((num, index) => {
          if (index < updatedStars.length) {
            updatedStars[index].id = num;
          }
        });
        console.log('업데이트된 별 배열:', updatedStars);
        setStars(updatedStars);
      } else {
        console.warn('응답으로 받은 번호가 없거나 빈 배열입니다.');
      }
    } catch (error) {
      console.error('행운의 번호 생성에 실패했습니다.', error);
      // 에러 발생 시에도 애니메이션은 계속 진행 (테스트용 더미 데이터 사용 가능)
    }
  };

  // animationStage 변경 추적
  useEffect(() => {
    console.log(`애니메이션 단계 변경: ${animationStage}`);
  }, [animationStage]);

  useEffect(() => {
    if (animationStage === 1) {
      console.log('애니메이션 단계 1: 텍스트 타이핑 시작');
      const fullText = '오늘의 행운의 번호는';
      let currentText = '';
      let index = 0;

      const typingInterval = setInterval(() => {
        if (index < fullText.length) {
          currentText += fullText[index];
          setTypingText(currentText);
          index++;
        } else {
          clearInterval(typingInterval);
          console.log('텍스트 타이핑 완료, 애니메이션 단계 2로 전환');
          setAnimationStage(2);
        }
      }, 100);

      return () => clearInterval(typingInterval);
    }
  }, [animationStage]);

  useEffect(() => {
    if (animationStage === 2) {
      console.log('애니메이션 단계 2: 별과 선 표시 시작');
      const timer = setTimeout(() => {
        stars.forEach((star, index) => {
          setTimeout(() => {
            console.log(`별 ${star.id} 표시`);
            setVisibleStars((prev) => [...prev, star.id]);

            if (index > 0) {
              setTimeout(() => {
                const visibleStarIds = stars
                  .filter((_, starIdx) => starIdx <= index)
                  .map((s) => s.id);

                connections.forEach((conn) => {
                  const star1 = stars[conn[0]];
                  const star2 = stars[conn[1]];

                  if (
                    visibleStarIds.includes(star1.id) &&
                    visibleStarIds.includes(star2.id)
                  ) {
                    setVisibleLines((prev) => {
                      const lineKey = [conn[0], conn[1]].sort().join('-');
                      if (
                        prev.some(
                          (line) =>
                            [line[0], line[1]].sort().join('-') === lineKey
                        )
                      ) {
                        return prev;
                      }
                      return [...prev, conn];
                    });
                  }
                });
              }, 200);
            }

            if (index === stars.length - 1) {
              setTimeout(() => {
                console.log(
                  '모든 별과 선 표시 완료, 애니메이션 단계 3으로 전환'
                );
                setAnimationStage(3);
              }, 1000);
            }
          }, 800 * index);
        });
      }, 500);

      return () => clearTimeout(timer);
    }
  }, [animationStage]);

  useEffect(() => {
    if (animationStage === 3) {
      console.log('애니메이션 단계 3: 번호 표시 시작');
      stars.forEach((s, numIndex) => {
        setTimeout(() => {
          console.log(`번호 ${s.id} 표시`);
          setVisibleNumbers((prev) => [...prev, s.id]);

          // 마지막 숫자가 추가되면 다음 단계로
          if (numIndex === stars.length - 1) {
            setTimeout(
              () => {
                console.log('모든 번호 표시 완료, 애니메이션 단계 4로 전환');
                setAnimationStage(4);
              },
              600 * numIndex + 300
            );
          }
        }, 600 * numIndex);
      });
    }
  }, [animationStage]);

  // stars 상태 변경 감지
  useEffect(() => {
    console.log('stars 상태 변경됨:', stars);
  }, [stars]);

  return (
    <div className="w-screen h-screen relative clover-font">
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
      <div className="absolute top-3 right-4 text-2xl text-white">
        <button
          onClick={onClickHome}
          className="cursor-pointer hover:text-gray-200">
          ✕
        </button>
      </div>
      {showText && (
        <div className="lucky-text visible absolute bottom-38 left-0 right-0 mx-auto w-full text-center text-white text-[22px] clover-font tracking-widest">
          {typingText}
          {(animationStage === 3 || animationStage === 4) && (
            <span
              className="number-container"
              style={{ fontSize: '22px' }}>
              {stars.map((star, index) => (
                <span
                  key={star.id}
                  className={`number-slot ${visibleNumbers.includes(star.id) ? 'filled' : ''}`}>
                  <span className="number-value">
                    {visibleNumbers.includes(star.id) ? star.id : ''}
                  </span>
                </span>
              ))}
            </span>
          )}
        </div>
      )}

      {animationStage >= 2 && (
        <svg
          className="absolute inset-0 w-full h-full"
          viewBox="-3 -11 130 130"
          preserveAspectRatio="xMidYMid meet"
          style={{ pointerEvents: 'none' }}>
          {connections.map((conn, index) => {
            const isVisible = visibleLines.some(
              (line) =>
                (line[0] === conn[0] && line[1] === conn[1]) ||
                (line[0] === conn[1] && line[1] === conn[0])
            );
            return (
              <line
                key={`line-${index}`}
                x1={stars[conn[0]].x}
                y1={stars[conn[0]].y}
                x2={stars[conn[1]].x}
                y2={stars[conn[1]].y}
                stroke="white"
                strokeWidth="0.2"
                className={`constellation-line ${isVisible ? 'visible' : ''}`}
              />
            );
          })}

          {stars.map((star) => {
            const visible = visibleStars.includes(star.id);
            return (
              <g
                key={`star-${star.id}`}
                className={`star ${visible ? 'visible' : ''}`}>
                <circle
                  cx={star.x}
                  cy={star.y}
                  r={star.glowSize}
                  fill="rgba(255, 255, 255, 0.3)"
                  className={`star-glow ${visible ? 'visible' : ''}`}
                />
                <circle
                  cx={star.x}
                  cy={star.y}
                  r={star.size}
                  fill="white"
                  className={`star-center ${visible ? 'visible' : ''}`}
                />
                <text
                  x={star.x}
                  y={star.y - star.size - 1}
                  fontSize="3"
                  fill="white"
                  textAnchor="middle">
                  {star.id}
                </text>
              </g>
            );
          })}
        </svg>
      )}
      {showButton && dataLoaded && (
        <div className="absolute inset-0 flex items-center justify-center">
          <LuckyDrawButton
            onClick={handleDrawClick}
            visible={true}
          />
        </div>
      )}
    </div>
  );
};

export default LuckyNumber;
