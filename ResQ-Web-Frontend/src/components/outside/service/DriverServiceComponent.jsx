import React from 'react';

export default function DriverService() {
  return (
    <div className="flex flex-col items-center p-6">
      <h1 className="text-2xl font-bold mb-6">Replacement Driver</h1>
      <div className="flex gap-6">
        {/* Left - image */}
        <div className="relative w-[400px] h-[400px]">
          <img
            src="/images/aboutus7.png"
            alt="Replacement Driver"
            className="w-full h-full object-cover rounded shadow"
          />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-40 text-white text-lg font-semibold rounded">
            Replacement Driver
          </div>
        </div>

        {/* Right - description */}
        <div className="w-[600px] h-[400px] border rounded shadow bg-white p-4">
          <p className="text-gray-700">
            Our replacement driver service is a safe and convenient solution when you're unable to drive due to personal reasons,
            health issues, or legal matters. We provide a team of professional drivers who are well-trained and knowledgeable in traffic laws.
            With flexible problem-solving skills, our drivers ensure that you and your vehicle are transported safely and efficiently.
            This service is especially useful after parties, during holidays, or anytime you don't feel fit to drive. We’re available 24/7 in supported areas.
          </p>
        </div>
      </div>
    </div>
  );
}
