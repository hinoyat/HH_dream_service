// 꿈해몽 결과에 따라 이동하는 버튼

import ButtonBase from '@/domains/diary/components/details/button/ButtonBase';

interface DestinyButtonProps {
  isGood: string;
  onClick?: () => void;
}

const DestinyButton: React.FC<DestinyButtonProps> = ({ isGood, onClick }) => {
  const handleClick = () => {
    if (onClick) {
      onClick();
    }

    if (isGood === 'Y') {
      console.log('행운번호 추천 페이지로 이동');
    } else {
      console.log('오늘의 운세 페이지로 이동');
    }
  };

  const buttonText =
    isGood === 'Y' ? '행운번호 보러가기' : '오늘의 운세 보러가기';

  return (
    <ButtonBase
      onClick={handleClick}
      className="text-[15px]"
      width="200px">
      {buttonText}
    </ButtonBase>
  );
};

export default DestinyButton;
