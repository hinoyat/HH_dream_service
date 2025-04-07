// 일기조회에서의 꿈해몽 컴포넌트

import { DreamSolveResponse } from '@/domains/diary/api/dreamApi';

interface DreamMeaningViewProps {
  resultContent: string;
  isGood?: string;
  inputContent?: string;
}

const DreamMeaningView: React.FC<DreamMeaningViewProps> = ({
  resultContent,
  isGood,
  inputContent,
}) => {
  return (
    <div className="text-white tracking-wide text-sm inset-0">
      <div className="mb-2">{resultContent}</div>
    </div>
  );
};

export default DreamMeaningView;
