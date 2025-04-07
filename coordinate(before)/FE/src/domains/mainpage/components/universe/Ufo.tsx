import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useFBX, useCursor, Text } from '@react-three/drei';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { ThreeEvent } from '@react-three/fiber';

export default function Ufo() {
  const fbx = useFBX('/universe/ufo/scene.fbx');
  const ufoRef = useRef<THREE.Group>(null);
  const navigate = useNavigate();

  // 마우스 오버 상태 관리
  const [hovered, setHovered] = useState(false);
  // hovered 상태에 따라 커서 변경: true이면 pointer, 아니면 auto
  useCursor(hovered, 'pointer', 'auto');

  useEffect(() => {
    fbx.traverse((child) => {
      const mesh = child as THREE.Mesh;
      if (mesh.isMesh && mesh.material instanceof THREE.MeshStandardMaterial) {
        console.log('Mesh name:', mesh.name);
        console.log('Material:', mesh.material);
      }
    });
  }, [fbx]);

  // 궤도 회전 애니메이션
  useFrame((state, delta) => {
    if (ufoRef.current) {
      // Y축을 중심으로 회전
      ufoRef.current.rotation.y += delta * 0.6;
      // 약간의 상하 움직임 추가
      ufoRef.current.position.y = Math.sin(state.clock.elapsedTime) * 0.1;
    }
  });

  // 클릭 이벤트 핸들러
  const handleUfoClick = (e: ThreeEvent<MouseEvent>) => {
    e.stopPropagation(); // 이벤트 버블링 방지
    console.log('🚀 UFO가 클릭되었습니다! 👽');
    navigate('/spaceship'); // '/spaceship' 경로로 이동
  };

  return (
    <>
      {/* 주변 조명 */}
      <ambientLight intensity={0.7} />

      {/* 중앙에서 빛이 퍼져나가는 효과 */}
      <pointLight
        position={[0, 0, 0]}
        intensity={1}
        distance={300}
        decay={2}
      />
      <group
        ref={ufoRef}
        position={[200, -140, -300]}>
        <primitive
          object={fbx}
          position={[0, 0, 0]}
          scale={[0.18, 0.18, 0.18]}
          rotation={[Math.PI / 0.82, Math.PI / 2.45, Math.PI / 1.2]}
          onClick={handleUfoClick}
          onPointerOver={() => setHovered(true)}
          onPointerOut={() => setHovered(false)}
        />

        {/* 호버 시 3D 텍스트 표시 */}
        {hovered && (
          <Text
            position={[0, 80, 0]}
            fontSize={14}
            color="white"
            anchorX="center"
            anchorY="middle"
            outlineWidth={0.5}
            outlineColor="black"
            fillOpacity={1}
            userData={{ disablePicking: true }} // 마우스 이벤트가 투과되도록 설정
          >
            UFO
          </Text>
        )}
      </group>
    </>
  );
}
