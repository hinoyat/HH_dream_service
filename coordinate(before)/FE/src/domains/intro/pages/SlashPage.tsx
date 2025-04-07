import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '@/domains/Loading/themes/Loading.css';
import { selectUser } from '@/stores/auth/authSelectors';
import { useSelector } from 'react-redux';

const SlashPage = () => {
  const nav = useNavigate();
  const user = useSelector(selectUser);
  
  useEffect(() => {
    user ? nav(`/${user.username}`) : nav('/intro');
    
  }, [user, nav]);
  
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

export default SlashPage;