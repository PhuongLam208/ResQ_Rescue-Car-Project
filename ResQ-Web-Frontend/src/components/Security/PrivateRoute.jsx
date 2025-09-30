import { Navigate } from "react-router-dom";
import { useLocation } from "react-router-dom";


let hasAlerted = false;

const PrivateRoute = ({ children, allowedRoles }) => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    const location = useLocation();

    if (!token || !allowedRoles.includes(role)) {
        if (!hasAlerted) {
            hasAlerted = true;
            alert("You need to log in first");
          }
        
        return <Navigate to="/login" replace state={{ from: location }} />;
    }
    return children;
}

export default PrivateRoute;


