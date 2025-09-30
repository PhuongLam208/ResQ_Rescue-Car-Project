import React, { createContext, useState, useEffect } from "react";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = {
      token: localStorage.getItem("token"),
      fullName: localStorage.getItem("fullName"),
      role: localStorage.getItem("role"),
      onShift: localStorage.getItem("onShift") === "true",
      userId: localStorage.getItem("userId"),
    };
    if (storedUser.token) {
      setUser(storedUser);
    }
  }, []);

  const login = (data) => {
    localStorage.setItem("token", data.token);
    localStorage.setItem("fullName", data.fullName);
    localStorage.setItem("role", data.role);
    localStorage.setItem("onShift", data.onShift);
    localStorage.setItem("userId", data.userId);

    setUser({
      ...data,
      onShift: data.onShift === true || data.onShift === "true",
    });
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    window.location.href = "/";
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
