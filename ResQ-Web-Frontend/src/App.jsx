import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import AdminDashboard from "./components/inside/admin/AdminDashboard";
import ManagerDashboard from "./components/inside/manager/ManagerDashboard";
import StaffDashboard from "./components/inside/staff/StaffDashboard";
import PrivateRoute from "./components/Security/PrivateRoute";
import "./index.css";
import HomeContainer from "./components/outside/home/HomeContainer";
import Home from "./components/outside/home/Home";
import Login from "./components/outside/home/login/Login";
import AboutUs from "./components/outside/aboutus/Aboutus";
import FaqSection from "./components/outside/faq/FaqSection";
import { Service } from "./components/outside/service/Service";
import PartnerJoinSection from "./components/outside/partner/PartnerJoinSection";
import TowServiceComponent from "./components/outside/service/TowServiceComponent";
import OnSiteRescueComponent from "./components/outside/service/OnSiteRescueComponent";
import DriverService from "./components/outside/service/DriverServiceComponent";
import MyProfile from "./components/inside/MyProfile";

function App() {
  return (
    <BrowserRouter >
      <Routes>
        {/* <Route path="/" element={<Home />} /> */}
        <Route path="/" element={< HomeContainer/>}>
          {/* Home route */}
          <Route index element={<Home />} />
          {/* <Route path="path" element={<Component />} /> */}
          <Route path="/login" element={< Login/>} />    
          <Route path="/aboutus" element={< AboutUs/>} />    
          <Route path="/faq" element={< FaqSection/>} />  
          <Route path="/service" element={< Service/>} />  
          <Route path="/partner" element={< PartnerJoinSection/>} />  
          <Route path="/towtruck" element={< TowServiceComponent/>} /> 
          <Route path="/onsiterescue" element={< OnSiteRescueComponent/>} />  
          <Route path="/driverservice" element={< DriverService/>} /> 
        </Route>
        <Route path="/admin" element={
          <PrivateRoute allowedRoles={['ROLE_ADMIN']}>
            <AdminDashboard />
            </PrivateRoute>
          } />
        <Route path="/manager" element={
          <PrivateRoute allowedRoles={['ROLE_MANAGER']}>
            <ManagerDashboard />
            </PrivateRoute>
          } />
        <Route path="/staff" element={
          <PrivateRoute allowedRoles={['ROLE_STAFF']}>
            <StaffDashboard />
            </PrivateRoute>
          } />
        <Route path="/my-profile" element={
          <PrivateRoute allowedRoles={['ROLE_STAFF', 'ROLE_MANAGER', 'ROLE_ADMIN']}>
            <MyProfile />
            </PrivateRoute>
          } />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
