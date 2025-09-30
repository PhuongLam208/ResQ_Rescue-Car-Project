import React from 'react';

export default function OnSiteRescueComponent() {
  return (
    <div className="flex flex-col items-center p-6">
      <h1 className="text-2xl font-bold mb-6">On-site Rescue</h1>
      <div className="flex gap-6">
        {/* Left - image */}
        <div className="relative w-[400px] h-[400px]">
          <img
            src="/images/abouts1.jpg"
            alt="On-site Rescue"
            className="w-full h-full object-cover rounded shadow"
          />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-40 text-white text-lg font-semibold rounded">
            On-site Rescue
          </div>
        </div>

        {/* Right - description */}
        <div className="w-[600px] h-[400px] border rounded shadow bg-white p-4">
          <p className="text-gray-700">
            Our on-site rescue service is designed to support you during emergencies such as battery failure, flat tires, jammed locks,
            engine won't start, or other minor technical issues. Our experienced technicians will arrive promptly at your location,
            helping resolve the issue without needing to tow the vehicle to a garage. We use modern equipment, offer friendly service, and maintain transparent pricing,
            ensuring you can continue your journey with peace of mind and minimal delays.
          </p>
        </div>
      </div>
    </div>
  );
}
