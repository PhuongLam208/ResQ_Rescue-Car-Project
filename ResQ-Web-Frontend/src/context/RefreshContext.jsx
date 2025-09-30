import { createContext, useContext, useState, useCallback } from "react";

const RefreshContext = createContext();

export const RefreshProvider = ({ children }) => {
  const [listeners, setListeners] = useState([]);

  const registerRefresh = useCallback((refreshFn) => {
    setListeners((prev) => [...prev, refreshFn]);
  }, []);

  const triggerRefresh = useCallback(() => {
    listeners.forEach((fn) => typeof fn === "function" && fn());
  }, [listeners]);

  return (
    <RefreshContext.Provider value={{ registerRefresh, triggerRefresh }}>
      {children}
    </RefreshContext.Provider>
  );
};

export const UseRefreshContext = () => useContext(RefreshContext);
