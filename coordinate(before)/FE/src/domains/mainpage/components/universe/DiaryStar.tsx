// DiaryStar 컴포넌트
// 'DiaryEntry' 데이터를 기반으로 별 모양을 생성하고,
// 해당 별을 클릭하면 일기의 상세 정보가 표시됨.

// 별은 마우스 호버 시 확대됨.
// 일기 미리보기는 universe.tsx에서!

// 호버했을 때 위치를 universe로 넘겨서 2D위치에 맞게 미리보기 띄우는걸로 추후 수정해봐야겠음
// 나중에 작성 날짜에 따라서 별의 수명주기에 맞게 별 색상을 표현해도 재밌을 것 같음

import React, { useState, useRef, useEffect } from 'react';
import { extend, useFrame } from '@react-three/fiber';
import * as THREE from 'three';
// import DiaryEntry from '@/domains/mainpage/models/DiaryEntry';

// THREE.js 메쉬 및 재질 확장
extend({
  Mesh: THREE.Mesh,
  SphereGeometry: THREE.SphereGeometry,
  MeshStandardMaterial: THREE.MeshStandardMaterial,
});

interface DiaryStarProps {
  entry: {
    diarySeq: number;
    title: string;
    content: string;
    tags: Array<{ tagSeq: number; name: string }>;
    x: number;
    y: number;
    z: number;
    isPublic: string;
    createdAt: string; // API 형식: "20250324 172604"
    emotionSeq?: number;
    emotionName: string;
    connectedDiaries: any | null;
  };
  onClick: (entry: any, position: { x: number; y: number }) => void;
  onHover: (
    entry: any | null,
    position: { x: number; y: number } | null
  ) => void;
  isNew?: boolean; // 노란색 별 표시용 (10분)
  isHighlight?: boolean; // 반짝이는 효과용 (5초)
}

const DiaryStar: React.FC<DiaryStarProps> = ({
  entry, // 일기 조회
  onClick, // 호버 or 조회
  onHover, // 미리보기
  isNew, // 노란색 효과 적용 (10분)
  isHighlight = false, // 반짝이는 효과 적용 (5초)
}) => {
  // 상태
  const [hovered, setHovered] = useState<boolean>(false);
  const [highlightIntensity, setHighlightIntensity] = useState(isNew ? 8 : 5);

  const { x, y, z } = entry;
  const meshRef = useRef<THREE.Mesh>(null); // 직접 별을 클릭하기 위해
  const glowRef = useRef<THREE.Mesh>(null); // 새 별 주변에 발광 효과를 위해

  // 별 크기
  const starSize = 3;

  // 별 생성 시간 확인 함수 (하이라이트 효과를 위해)
  const isWithin30Minutes = (dateString: string) => {
    const year = parseInt(dateString.substring(0, 4));
    const month = parseInt(dateString.substring(4, 6)) - 1; // 월은 0부터 시작
    const day = parseInt(dateString.substring(6, 8));
    const hour = parseInt(dateString.substring(9, 11));
    const minute = parseInt(dateString.substring(11, 13));
    const second = parseInt(dateString.substring(13, 15));

    const date = new Date(year, month, day, hour, minute, second);
    return Date.now() - date.getTime() < 30 * 60 * 1000;
  };

  // 별 색상 - 새 별은 노란색, 기존 별은 파란색
  // ? '#ffcc00' // 새 별은 노란색
  // : '#00ffe0' // 기존 별은 파란색
  const starColor = new THREE.Color(
    isNew
      ? '#9932CC' // 새 별은 보라색
      : '#FF4500' // 기존 별은 선명한 주황색
  );

  // 새 별의 경우 특별한 애니메이션 효과 적용
  useEffect(() => {
    if (isHighlight) {
      // 5초 동안 깜빡임 효과
      const startTime = Date.now();
      const duration = 15000; // 15초동안 반짝임!

      const animateNewStar = () => {
        const elapsed = Date.now() - startTime;
        if (elapsed < duration) {
          // 깜빡임 효과 (사인 파동)
          const intensity = 8 + 7 * Math.sin(elapsed / 300);
          setHighlightIntensity(intensity);
          requestAnimationFrame(animateNewStar);
        } else {
          // 15초 후 일반 별로 변환
          setHighlightIntensity(3);
        }
      };

      const animationFrame = requestAnimationFrame(animateNewStar);
      return () => cancelAnimationFrame(animationFrame);
    }
  }, [isHighlight]);

  // 매 프레임마다 실행되는 애니메이션 로직
  useFrame((state) => {
    if (meshRef.current) {
      // 별의 확대/축소 효과 (펄스 애니메이션)
      const pulseFactor = isHighlight ? 0.15 : 0.05; // 새 별은 더 큰 펄스 효과
      const pulseSpeed = isHighlight ? 3 : 1.5; // 새 별은 더 빠른 펄스
      const scale =
        1 + pulseFactor * Math.sin(state.clock.elapsedTime * pulseSpeed);

      const baseScale = hovered ? 1.3 : 1.0; // 호버 상태에 따른 기본 크기

      // 별의 크기 조정
      meshRef.current.scale.set(
        baseScale * scale,
        baseScale * scale,
        baseScale * scale
      );
    }

    // 반짝이는 효과가 있는 별의 경우 발광 효과 애니메이션
    if (isHighlight && glowRef.current) {
      const glowScale = 2.5 + 0.5 * Math.sin(state.clock.elapsedTime * 2);
      glowRef.current.scale.set(glowScale, glowScale, glowScale);
    }
  });

  // 마우스가 별에 올려졌을 때와 벗어났을 때의 핸들러
  const handlePointerOver = (event: THREE.Event): void => {
    setHovered(true);
    // 마우스 포인터 위치 가져오기
    const x = event.clientX + 20; // 오른쪽으로 20만큼 이동
    const y = event.clientY + 20; // 아래로 20만큼 이동

    // console.log('호버된 entry 데이터:', entry);

    onHover(entry, { x, y });
    document.body.style.cursor = 'pointer';
  };

  const handlePointerOut = (): void => {
    setHovered(false);
    onHover(null, null);
    document.body.style.cursor = 'auto';
  };

  return (
    <group position={[x, y, z]}>
      {/* 반짝이는 효과가 있는 별이라면 발광 효과 추가 */}
      {isHighlight && (
        <mesh ref={glowRef}>
          <sphereGeometry args={[(starSize / 2) * 6, 16, 16]} />
          <meshBasicMaterial
            color={starColor}
            transparent={true}
            opacity={0.2}
          />
        </mesh>
      )}

      {/* 별 위치 설정 */}
      <mesh
        ref={meshRef} // 메쉬 참조 연결
        onClick={(e) => {
          e.stopPropagation();
          // 클릭위치 전달
          onClick(entry, { x: e.clientX, y: e.clientY });
        }}
        // 영역 내 마우스 올라와 있는지 상태
        onPointerOver={handlePointerOver}
        onPointerOut={handlePointerOut}>
        {/* 별 모양의 지오메트리 */}
        <sphereGeometry
          args={[(starSize / 2) * (isHighlight ? 2.5 : 1.3), 16, 16]}
        />
        {/* 별의 색과 발광 효과 */}
        <meshStandardMaterial
          color={starColor}
          emissive={starColor}
          emissiveIntensity={hovered ? 5 : highlightIntensity} // 하이라이트 강도 사용
        />
      </mesh>
      {/* 반짝이는 효과가 있는 별이라면 빛나는 효과 추가 */}
      {isHighlight && (
        <pointLight
          color={starColor}
          intensity={2.5}
          distance={15}
          decay={2}
        />
      )}
    </group>
  );
};

export default DiaryStar;
