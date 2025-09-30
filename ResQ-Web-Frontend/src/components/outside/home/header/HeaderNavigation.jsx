import React, { useState } from "react";
import { NavLink } from "react-router-dom";

const HeaderNavigation = () => {
  const [menuOpen, setMenuOpen] = useState(false);

  const navLinkClass =
    "relative px-2 py-1 transition-all font-medium text-blue-013171";

  const hoverUnderlineStyle = `
    before:content-[''] before:absolute before:-bottom-1 before:left-0 
    before:w-0 before:h-[2px] before:bg-blue-013171 
    before:transition-all before:duration-300 
    hover:before:w-full
  `;

  const activeStyle = "font-semibold text-blue-900";

  return (
    <nav>
      {/* Mobile menu button */}
      <div className="flex justify-end lg:hidden">
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="p-2 focus:outline-none"
        >
          <img
            src="/images/icon-web/menu.png"
            alt="Menu"
            width={28}
            height={28}
          />
        </button>
      </div>

      {/* Desktop menu */}
      <ul className="hidden lg:flex items-center justify-between">
        <li className="text-xl w-[650px]">
          <div className="flex items-center justify-between font-lexend">
            {[
              { path: "/", label: "Home" },
              { path: "/service", label: "Services" },
              { path: "/aboutus", label: "About Us" },
              { path: "/faq", label: "FAQ's" },
              { path: "/partner", label: "Partners" },
            ].map(({ path, label }) => (
              <NavLink
                key={path}
                to={path}
                className={({ isActive }) =>
                  `${navLinkClass} ${hoverUnderlineStyle} ${
                    isActive ? `before:w-full ${activeStyle}` : ""
                  }`
                }
              >
                {label}
              </NavLink>
            ))}
          </div>
        </li>
        <li className="w-[375px]">
          <div className="flex items-center justify-between font-lexend font-medium">
            <div className="flex items-center gap-3 bg-blue-013171 p-3 rounded-full h-[40px]">
              <img
                src="/images/icon-web/Ringer Volume.png"
                alt="phone"
                width={20}
                height={22}
              />
              <p className="text-sm text-white">(+84) 656 5565 7777</p>
            </div>
            <div className="flex items-center gap-3">
              <img
                src="/images/icon-web/Person.png"
                alt="avatar"
                width={27}
                height={28}
              />
              <NavLink
                to="/login"
                className={({ isActive }) =>
                  `text-xl ${
                    isActive
                      ? "text-blue-900 font-semibold"
                      : "text-blue-013171"
                  }`
                }
              >
                Login
              </NavLink>
            </div>
          </div>
        </li>
      </ul>

      {/* Mobile dropdown */}
      {menuOpen && (
        <div className="lg:hidden mt-3 p-4 bg-white shadow-md rounded-md space-y-2">
          {[
            { path: "/", label: "Home" },
            { path: "/service", label: "Services" },
            { path: "/aboutus", label: "About Us" },
            { path: "/faq", label: "FAQ's" },
            { path: "/partner", label: "Partners" },
            { path: "/login", label: "Login" },
          ].map(({ path, label }) => (
            <NavLink
              key={path}
              to={path}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) =>
                `block py-2 ${
                  isActive ? "text-blue-900 font-semibold" : "text-blue-013171"
                }`
              }
            >
              {label}
            </NavLink>
          ))}
        </div>
      )}
    </nav>
  );
};

export default HeaderNavigation;
