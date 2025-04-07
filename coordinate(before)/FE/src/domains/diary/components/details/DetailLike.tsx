import '../../styles/DetailLike.css';
import React from 'react';
import Heartbox from '@/common/HeartBox';

interface DiaryDetailProps {
  likes_boolean: boolean;
  likes: number;
  diarySeq: number;
  isMyDiary: boolean; // 내 일기에만 좋아요 아니라서 필요없긴 한데 혹시 몰라서 일단 살려둘게여
  onLikeToggle: () => void;
}

const DetailLike: React.FC<DiaryDetailProps> = ({
  likes,
  likes_boolean,
  diarySeq,
  isMyDiary,
  onLikeToggle,
}) => {
  const handleClick = () => {
    onLikeToggle();
  };

  return (
    <div className="flex items-center justify-end pr-2 gap-1">
      <div
        onClick={handleClick}
        className="cursor-pointer">
        <Heartbox isLiked={likes_boolean} />
      </div>
      <p className="text-white">{likes}</p>
    </div>
  );
};

export default DetailLike;