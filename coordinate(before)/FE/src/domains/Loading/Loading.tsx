import React from 'react';
import './themes/Loading.css';
import StarField from '../mainpage/components/universe/StarField';

const Loading = () => {
  return (
    <div className="hacker-loader-container bg-black">
      <div className="hacker-loader">
        <div className="loader-text">
          <span
            data-text="Loading..."
            className="text-glitch">
            Loading...
          </span>
        </div>
        <div className="loader-bar">
          <div className="bar-fill"></div>
          <div className="bar-glitch"></div>
        </div>
        <div className="particles">
          <div className="particle"></div>
          <div className="particle"></div>
          <div className="particle"></div>
          <div className="particle"></div>
          <div className="particle"></div>
        </div>
      </div>
    </div>
  );
};

export default Loading;
