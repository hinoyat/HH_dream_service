
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

// 아이콘 배열을 생성합니다
export const profileIcons = [
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
  icon56
];

// 아이콘 타입을 정의합니다
export interface IconWeight {
  id: number;       // 아이콘 ID (1-56)
  weight: number;   // 가중치 (등장 확률)
  isRare: boolean;  // 희귀 아이콘 여부
  isEpic: boolean;
  isUnique: boolean;
  isLengendary: boolean;
}

// 아이콘 가중치 설정 (기본값은 모두 1)
export const iconWeights: IconWeight[] = Array.from({ length: 56 }, (_, i) => ({
  id: i + 1,
  weight: 1,
  isRare: false,
  isEpic: false,
  isUnique: false,
  isLengendary: false
}));

// id는 1부터 시작하므로 배열 인덱스와 동일하게 설정하려면 -1을 해줍니다
// 매우 희귀한 아이콘 설정
iconWeights[14].weight = 5;  // 아이콘 15
iconWeights[14].isRare = true;

iconWeights[24].weight = 5;  // 아이콘 25
iconWeights[24].isRare = true;

iconWeights[34].weight = 1;  // 아이콘 35
iconWeights[34].isEpic = true;

iconWeights[44].weight = 0.1;  // 아이콘 45
iconWeights[44].isUnique = true;

iconWeights[55].weight = 0.01;  // 아이콘 56
iconWeights[55].isLengendary = true;



/**
 * 가중치를 기반으로 랜덤 아이콘 ID를 반환하는 함수
 * @returns 선택된 아이콘 ID (1-56)
 */
export const getRandomIconId = (): number => {
  // 가중치의 합 계산
  const totalWeight = iconWeights.reduce((sum, icon) => sum + icon.weight, 0);
  
  // 0부터 totalWeight 사이의 랜덤 값 생성
  let random = Math.random() * totalWeight;
  
  // 랜덤 값에 해당하는 아이콘 찾기
  for (const icon of iconWeights) {
    random -= icon.weight;
    if (random <= 0) {
      return icon.id;
    }
  }
  
  // 기본값 반환
  return 1;
};

/**
 * 아이콘 ID를 기반으로 아이콘 URL을 반환하는 함수
 * @param iconId 아이콘 ID (1-56)
 * @returns 아이콘 URL
 */
export const getIconById = (iconId: number): string => {
  // 아이콘 ID는 1부터 시작하므로 배열 인덱스는 -1을 해줍니다
  const index = iconId - 1;
  
  // 범위 체크
  if (index < 0 || index >= profileIcons.length) {
    return profileIcons[0]; // 기본 아이콘 반환
  }
  
  return profileIcons[index];
};

/**
 * 아이콘이 희귀한지 여부를 반환하는 함수
 * @param iconId 아이콘 ID (1-56)
 * @returns 희귀 여부 (true/false)
 */
export const isRareIcon = (iconId: number): boolean => {
  const index = iconId - 1;
  if (index < 0 || index >= iconWeights.length) {
    return false;
  }
  return iconWeights[index].isRare;
};

/** 
 * 아이콘이 에픽한지 여부를 반환하는 함수
 * @param iconId 아이콘 ID (1-56)
 * @returns 에픽 여부 (true/false)
 */
export const isEpicIcon = (iconId: number): boolean => {
  const index = iconId - 1;
  if (index < 0 || index >= iconWeights.length) {
    return false;
  }
  return iconWeights[index].isEpic;
};

/**
 * 아이콘이 유니크한지 여부를 반환하는 함수
 * @param iconId 아이콘 ID (1-56)
 * @returns 유니크 여부 (true/false)
 */
export const isUnique = (iconId: number): boolean => {
  const index = iconId - 1;
  if (index < 0 || index >= iconWeights.length) {
    return false;
  }
  return iconWeights[index].isUnique;
};

/**
 * 아이콘이 레전더리한지 여부를 반환하는 함수
 * @param iconId 아이콘 ID (1-56)
 * @returns 레전더리 여부 (true/false)
 */
export const isLegendary = (iconId: number): boolean => {
  const index = iconId - 1;
  if (index < 0 || index >= iconWeights.length) {
    return false;
  }
  return iconWeights[index].isLengendary;
};


/**
 * 랜덤 아이콘을 선택하고 아이콘 정보를 반환하는 함수
 * @returns 선택된 아이콘 정보 객체
 */
export const getRandomIcon = () => {
  const iconId = getRandomIconId();
  return {
    id: iconId,
    url: getIconById(iconId),
    isRare: isRareIcon(iconId),
    isEpic: isEpicIcon(iconId),
    isUnique: isUnique(iconId),
    isLegendary: isLegendary(iconId)
  };
};