// ProfileIconSelector.jsx
import { useState } from 'react';
import { GrPowerCycle } from 'react-icons/gr';

// 아이콘 56개
import icon1 from '@/assets/profile-icon/icon1.svg';
import icon2 from '@/assets/profile-icon/icon2.svg';
import icon3 from '@/assets/profile-icon/icon3.svg';
import icon4 from '@/assets/profile-icon/icon4.svg';
import icon5 from '@/assets/profile-icon/icon5.svg';
import icon6 from '@/assets/profile-icon/icon6.svg';
import icon7 from '@/assets/profile-icon/icon7.svg';
import icon8 from '@/assets/profile-icon/icon8.svg';
import icon9 from '@/assets/profile-icon/icon9.svg';
import icon10 from '@/assets/profile-icon/icon10.svg';

import icon11 from '@/assets/profile-icon/icon11.svg';
import icon12 from '@/assets/profile-icon/icon12.svg';
import icon13 from '@/assets/profile-icon/icon13.svg';
import icon14 from '@/assets/profile-icon/icon14.svg';
import icon15 from '@/assets/profile-icon/icon15.svg';
import icon16 from '@/assets/profile-icon/icon16.svg';
import icon17 from '@/assets/profile-icon/icon17.svg';
import icon18 from '@/assets/profile-icon/icon18.svg';
import icon19 from '@/assets/profile-icon/icon19.svg';
import icon20 from '@/assets/profile-icon/icon20.svg';

import icon21 from '@/assets/profile-icon/icon21.svg';
import icon22 from '@/assets/profile-icon/icon22.svg';
import icon23 from '@/assets/profile-icon/icon23.svg';
import icon24 from '@/assets/profile-icon/icon24.svg';
import icon25 from '@/assets/profile-icon/icon25.svg';
import icon26 from '@/assets/profile-icon/icon26.svg';
import icon27 from '@/assets/profile-icon/icon27.svg';
import icon28 from '@/assets/profile-icon/icon28.svg';
import icon29 from '@/assets/profile-icon/icon29.svg';
import icon30 from '@/assets/profile-icon/icon30.svg';

import icon31 from '@/assets/profile-icon/icon31.svg';
import icon32 from '@/assets/profile-icon/icon32.svg';
import icon33 from '@/assets/profile-icon/icon33.svg';
import icon34 from '@/assets/profile-icon/icon34.svg';
import icon35 from '@/assets/profile-icon/icon35.svg';
import icon36 from '@/assets/profile-icon/icon36.svg';
import icon37 from '@/assets/profile-icon/icon37.svg';
import icon38 from '@/assets/profile-icon/icon38.svg';
import icon39 from '@/assets/profile-icon/icon39.svg';
import icon40 from '@/assets/profile-icon/icon40.svg';

import icon41 from '@/assets/profile-icon/icon41.svg';
import icon42 from '@/assets/profile-icon/icon42.svg';
import icon43 from '@/assets/profile-icon/icon43.svg';
import icon44 from '@/assets/profile-icon/icon44.svg';
import icon45 from '@/assets/profile-icon/icon45.svg';
import icon46 from '@/assets/profile-icon/icon46.svg';
import icon47 from '@/assets/profile-icon/icon47.svg';
import icon48 from '@/assets/profile-icon/icon48.svg';
import icon49 from '@/assets/profile-icon/icon49.svg';
import icon50 from '@/assets/profile-icon/icon50.svg';

import icon51 from '@/assets/profile-icon/icon51.svg';
import icon52 from '@/assets/profile-icon/icon52.svg';
import icon53 from '@/assets/profile-icon/icon53.svg';
import icon54 from '@/assets/profile-icon/icon54.svg';
import icon55 from '@/assets/profile-icon/icon55.svg';
import icon56 from '@/assets/profile-icon/icon56.svg';

interface ProfileIconSelectorProps {
  onSelectIcon: (iconSrc: string, iconIndex: number) => void;
}

const ProfileIconSelector: React.FC<ProfileIconSelectorProps> = ({
  onSelectIcon,
}) => {
  const profileIcons = [
    icon1,
    icon2,
    icon3,
    icon4,
    icon5,
    icon6,
    icon7,
    icon8,
    icon9,
    icon10,
    icon11,
    icon12,
    icon13,
    icon14,
    icon15,
    icon16,
    icon17,
    icon18,
    icon19,
    icon20,
    icon21,
    icon22,
    icon23,
    icon24,
    icon25,
    icon26,
    icon27,
    icon28,
    icon29,
    icon30,
    icon31,
    icon32,
    icon33,
    icon34,
    icon35,
    icon36,
    icon37,
    icon38,
    icon39,
    icon40,
    icon41,
    icon42,
    icon43,
    icon44,
    icon45,
    icon46,
    icon47,
    icon48,
    icon49,
    icon50,
    icon51,
    icon52,
    icon53,
    icon54,
    icon55,
    icon56,
  ];

  // 현재 선택된 아이콘 인덱스
  const [currentIconIndex, setCurrentIconIndex] = useState<number>(0);

  // 랜덤 아이콘 선택 함수
  const selectRandomIcon = (): void => {
    const randomIndex = Math.floor(Math.random() * profileIcons.length);
    setCurrentIconIndex(randomIndex);

    // 부모 컴포넌트에 선택된 아이콘 정보 전달
    onSelectIcon(profileIcons[randomIndex], randomIndex);
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        gap: '20px',
      }}>
      {/* 이미지와 버튼을 감싸는 컨테이너 */}
      <div style={{ position: 'relative', width: '65px', height: '65px' }}>
        <img
          src={profileIcons[currentIconIndex]}
          alt="프로필 아이콘"
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'contain',
            padding: '4px',
            borderRadius: '50%',
            border: '1px solid white',
          }}
        />
        <button
          type="button"
          style={{
            position: 'absolute',
            top: '34px',
            left: '45px',
            zIndex: 5,
            background: 'none',
            border: 'none',
            padding: 0,
          }}
          onClick={selectRandomIcon}>
          <GrPowerCycle
            className="text-white/90 bg-blue-700 rounded-full cursor-pointer border border-white hover:brightness-80 transition-all duration-300"
            size={22}
            style={{
              background: 'linear-gradient(145deg, #1e3a8a, #3b82f6)',
              borderRadius: '50%',
              border: '1px solid white',
              padding: '3px',
            }}
          />
        </button>
      </div>

      {/* 안내 문구 */}
      <p
        className="dung-font"
        style={{
          color: '#d1d5db',
          fontSize: '0.75rem',
        }}>
        프로필 아이콘을 선택해주세요
      </p>
    </div>
  );
};

export default ProfileIconSelector;
