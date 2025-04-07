import React from 'react';
import './doubleClickAnimation.css';
import { BiPointer } from "react-icons/bi";

const DoubleClickMouse = () => {
  return (
    <div className="w-full flex justify-center items-center py-8 relative">
      {/* Ripple animation circle */}
      <div className="ripple-circle" />

      {/* Mouse pointer icon */}
      <div className="text-3xl text-white z-10">
        <BiPointer />
      </div>
    </div>
  );
};

export default DoubleClickMouse;