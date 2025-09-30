/*
  Component: HomeContainer
  Sử dụng làm container cho routers, ui
*/

import React from "react";
import { Outlet } from "react-router-dom";
import Header from "./header/Header";
import Footer from "../footer/Footer";

const HomeContainer = () => {
  return (
    <div>
      <Header />
      <div>
        {/* Re-render content tương ứng với nested routes HomeContainer ở bên App */}
        <Outlet />
      </div>
      <Footer/>
    </div>
  );
};

export default HomeContainer;