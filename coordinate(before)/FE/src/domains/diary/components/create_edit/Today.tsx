import React from 'react';

const Today = () => {
  const today = new Date();
  const formattedDate = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
  return <div className="text-[#FFFFFF]70 text-sm">{formattedDate}</div>;
};

export default Today;
