import React, { useState } from 'react';

const Checkbox = () => {
  const [isChecked, setIsChecked] = useState(false);

  const handleChange = () => {
    setIsChecked(!isChecked);
  };

  return (
    <div>
      <label className="cont">
        <input type="checkbox" checked={isChecked} onChange={handleChange} />
        <span></span>
      </label>
    </div>
  );
};

export default Checkbox;
