/*
  Component: Login
  Sử dụng ở trang chủ để người dùng đăng nhập
*/

import React from "react";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./login.css";
import { useContext } from "react";
import { login } from "../../../../api";
import { AuthContext } from "../../../../context/AuthContext";

const Login = () => {

  const [username, setUsername] = useState([]);
  const [password, setPassword] = useState([]);
  const navigate = useNavigate();
  const { login: authLogin } = useContext(AuthContext);

  useEffect(() => {
    document.title = "ResQ - Login";
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (token && role) {
      redirectByRole(role);
    }

  }, [navigate]);

  const redirectByRole = (role) => {
    switch (role) {
      case "ROLE_ADMIN":
        navigate("/admin");
        break;
      case "ROLE_MANAGER":
        navigate("/manager");
        break;
      case "ROLE_STAFF":
        navigate("/staff");
        break;
      default:
        alert("Không có quyền hợp lệ!");
    }
  };

const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await login({ 
        loginName: username, 
        password: password 
      });

      localStorage.setItem('token', data.token);
      localStorage.setItem('fullName', data.fullName);
      localStorage.setItem('role', data.role);
      localStorage.setItem('onShift', data.onShift);
      localStorage.setItem('userId', data.userId);

      authLogin(data);

      redirectByRole(data.role);
  
  } catch (error) {
      console.error("Login failed :", error);
      alert("Login failed! Please try again! ");
    }
  };

  return (
    <div className="mx-auto" style={{width: '600px', height: '555px'}}>
      <h2 className="font-lexend font-medium font-size-30 text-center p-12">Login</h2>
      <form onSubmit={handleSubmit} className="flex justify-center items-center flex-col gap-8">
        <div className="flex flex-col gap-2">
          <label htmlFor="username" className="block text-gray-900 font-lexend text-base font-normal">UserName</label>
          <input type="text" name="username" id="username" className="general-size border border-color-field rounded-full px-4" value={username} onChange={(e) => setUsername(e.target.value)} required/>
        </div>
        <div className="flex flex-col gap-2">
          <label htmlFor="password" className="block text-gray-900 font-lexend text-base font-normal">Password</label>
          <input type="password" name="password" id="password" className="general-size border border-color-field rounded-full px-4" value={password} onChange={(e) => setPassword(e.target.value)} required/>
        </div>
        <button type="submit" 
               className="general-size bg-blue-013171 text-white font-lexend text-xl font-semibold border rounded-full mt-6 hover:bg-blue-700" >
               Login
               </button>

      </form>
    </div>
  );
};

export default Login;