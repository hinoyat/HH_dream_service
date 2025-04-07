import React from 'react';
interface DiaryDetailProps {
  content: string;
}
const DetailContent: React.FC<DiaryDetailProps> = ({ content }) => {
  return (
    <div className="text-white tracking-wide text-sm inset-0">{content}</div>
  );
};

export default DetailContent;
