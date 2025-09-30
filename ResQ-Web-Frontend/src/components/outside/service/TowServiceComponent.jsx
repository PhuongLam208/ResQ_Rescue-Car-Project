import React from 'react';

export default function TowServiceComponent() {
  return (
    <div className="flex flex-col items-center p-6">
      <h1 className="text-2xl font-bold font-manrope mb-6">Towing Assistance</h1>
      <div className="flex gap-6">
        {/* Left - image */}
        <div className="relative w-[400px] h-[400px]">
          <img
            src="/images/aboutus6.jpg"
            alt="Towing Assistance"
            className="w-full h-full object-cover rounded shadow"
          />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-40 text-white text-lg font-lexend font-medium rounded">
            Towing Assistance
          </div>
        </div>

        {/* Right - description */}
        <div className="w-[600px] h-[400px] border rounded shadow bg-white p-4">
          <p className="text-gray-700 font-manrope">
            When your vehicle experiences serious issues such as engine failure, an accident, or flooding and cannot continue moving,
            our towing service will safely transport your vehicle to a garage or destination of your choice.
            With a fleet of specialized tow trucks in various sizes and a team of experienced drivers, we ensure your vehicle is handled with care,
            avoiding any additional damage during transport. Our service is available 24/7 and responds quickly in emergencies.
            We are committed to fast support, clear pricing, and professional service in every situation.
          </p>
        </div>
      </div>
    </div>
  );
}
