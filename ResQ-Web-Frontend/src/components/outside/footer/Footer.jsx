import React from "react";
import { FaPhone, FaEnvelope } from "react-icons/fa";

const Footer = () => {
  return (
    <footer className="mt-20 w-100 bg-[#7D7D7D] text-white py-10 px-8">
      <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
        {/* Contact */}
        <div>
          <h4 className="font-semibold mb-3 border-b border-white w-fit">Contact</h4>
          <p className="flex items-center gap-2">
            <FaPhone /> (+84) 656 5565 7777
          </p>
          <p className="flex items-center gap-2 mt-2">
            <FaEnvelope /> resqcompany@gmail.com
          </p>
        </div>

        {/* Services */}
        <div>
          <h4 className="font-semibold mb-3 border-b border-white w-fit">Services</h4>
          <p>On-site Rescue</p>
          <p>Towing Assistance</p>
          <p>Replacement Driver</p>
        </div>

        {/* Pages */}
        <div>
          <h4 className="font-semibold mb-3 border-b border-white w-fit">Pages</h4>
          <p>Partners</p>
          <p>Services</p>
          <p>About Us</p>
          <p>FAQs</p>
        </div>

        {/* App */}
        <div>
          <h4 className="font-semibold mb-3 border-b border-white w-fit">App</h4>
          <a href="#" className="inline-block mt-2">
            <img
              src="https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg"
              alt="Google Play"
              className="h-12"
            />
          </a>
        </div>
      </div>

      <div className="text-center text-sm text-gray-300 mt-8">
        © Copyright 2025 ResQ Company by LiveWithoutThinking
      </div>
    </footer>
  );
};

export default Footer;
