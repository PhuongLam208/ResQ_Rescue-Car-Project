import React from "react";
import HeaderNavigation from "./HeaderNavigation";

const Header = () => {
  return (
    <header className="flex flex-col lg:flex-row items-start lg:items-center justify-between px-4">
      {/* Logo */}
      <div className="mb-3 lg:mb-0">
        <img src="/images/logo.png" alt="logo" width={100} height={100} />
      </div>

      {/* Navigation */}
      <div className="w-full lg:w-[1100px]">
        <HeaderNavigation />
      </div>
    </header>
  );
};

export default Header;
