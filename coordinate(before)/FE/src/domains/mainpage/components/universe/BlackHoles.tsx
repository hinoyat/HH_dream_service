import React, { useState, useEffect, useRef } from 'react';
import { useGLTF, useCursor, Text } from '@react-three/drei';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { useAppDispatch } from '@/hooks/hooks';
import { visitOtherUserpage } from '@/domains/mainpage/stores/userThunks';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectVisitUser } from '@/domains/mainpage/stores/userSelectors';

export default function BlackHole() {
  const { scene } = useGLTF('/universe/blackholes/scene.gltf');
  const [modelReady, setModelReady] = useState(false);
  const [hovered, setHovered] = useState(false);
  const blackHoleRef = useRef();

  // useCursor 훅을 사용하여 hovered 상태에 따라 커서를 변경합니다.
  useCursor(hovered, 'pointer', 'auto');

  const dispatch = useAppDispatch();
  const nav = useNavigate();
  const visitUser = useSelector(selectVisitUser);

  // 모델이 로드되었는지 확인
  useEffect(() => {
    if (scene) {
      // scene을 복제하여 사용하면 참조 문제를 방지할 수 있습니다
      const clonedScene = scene.clone();
      setModelReady(true);
    }
  }, [scene]);

  // 블랙홀 회전 애니메이션 - scene 객체만 제자리에서 회전하도록 수정
  const sceneRef = useRef();

  useFrame((state, delta) => {
    if (sceneRef.current) {
      // scene 객체만 회전시켜 제자리 회전 구현
      sceneRef.current.rotation.y += delta * 0.5;
    }
  });

  const handleClick = async (e) => {
    if (!modelReady) return; // 모델이 로드되지 않았으면 무시

    e.stopPropagation();
    try {
      const result = await dispatch(visitOtherUserpage()).unwrap();
      console.log('누구를 랜덤 방문할까?', result);

      if (result?.username) {
        nav(`/${result.username}`);
        window.location.reload();
      }
    } catch (error) {
      console.error('랜덤 방문 실패:', error);
    }
  };

  // 모델이 로드되지 않았으면 아무것도 렌더링하지 않음
  if (!modelReady) return null;

  return (
    <group ref={blackHoleRef}>
      <mesh
        onPointerOver={() => setHovered(true)}
        onPointerOut={() => setHovered(false)}
        onClick={handleClick}
        position={[-150, 250, 150]}
        scale={[20, 20, 20]}
        rotation={[Math.PI / 1.2, Math.PI / 3.3, Math.PI / 1.4]}>
        <primitive
          ref={sceneRef}
          object={scene}
        />
      </mesh>

      {/* 호버 시 텍스트 표시 - 좌우 반전 적용 */}
      {hovered && (
        <Text
          position={[-150, 200, 150]} // 블랙홀 위에 텍스트 위치 조정
          fontSize={16}
          color="white"
          anchorX="center"
          anchorY="middle"
          outlineWidth={0.5}
          outlineColor="black"
          fillOpacity={1}
          userData={{ disablePicking: true }}
          billboard // 텍스트가 항상 카메라를 향하도록 함
          scale={[-1, 1, 1]} // X축으로 -1 스케일을 적용하여 좌우 반전
        >
          Blackhole
        </Text>
      )}
    </group>
  );
}
