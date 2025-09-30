import React from "react";

const PartnerJoinSection = () => {
  const benefits = [
    {
      title: "Attractive income\nClear revenue tracking",
      img: "/images/234163.jpg",
    },
    {
      title: "Stable customer flow\nvia app/web",
      img: "/images/6155818.jpg",
    },
    {
      title: "Marketing support\nManagement software included",
      img: "/images/5149654.jpg",
    },
  ];

  const requirements = [
    {
      title: "Rescue vehicles\nGarage\nSpecialized equipment",
      img: "/images/crane-truck.png",
    },
    {
      title: "Complete legal documents\nfor each type of service",
      img: "/images/nobg.png",
    },
    {
      title: "Commitment to\nservice quality standards",
      img: "/images/5995357.jpg",
    },
  ];

  return (
    <div className="bg-white text-center pt-10">
      {/* Top Banner */}
      <div className="relative w-full mb-10">
        <img
          src="/images/partner1.jpg"
          alt="Banner"
          className="w-full h-[470px] object-cover"
        />
        <div className="absolute font-semibold font-lexend inset-0 bg-black/10 flex flex-col justify-center items-center text-white text-2xl sm:text-3xl md:text-4xl space-y-2">
          <p>Join us in delivering</p>
          <p>safe – fast – dedicated</p>
          <p>rescue services!</p>
        </div>
      </div>

      {/* Main Content */}
      <div className="px-4">
        {/* Benefits */}
        <h2 className="text-2xl font-semibold font-lexend mb-8 text-[#013171]">Benefits of Partnering</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 justify-items-center mb-16">
          {benefits.map((item, index) => (
            <div
              key={index}
              className="border border-blue-900 rounded-2xl p-6 w-[400px] h-[520px] flex flex-col items-center justify-start shadow-md hover:shadow-lg transition"
            >
              <img
                src={item.img}
                alt=""
                className="w-full h-[250px] object-contain mb-6"
              />
              <p className="whitespace-pre-line text-lg font-lexend font-medium text-center leading-relaxed text-[#013171]">
                {item.title}
              </p>
            </div>
          ))}
        </div>

        {/* Requirements */}
        <h2 className="text-2xl font-semibold font-lexend mb-8 text-[#013171]">Partner Requirements</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 justify-items-center mb-16">
          {requirements.map((item, index) => (
            <div
              key={index}
              className="border border-blue-900 rounded-2xl p-6 w-[400px] h-[520px] flex flex-col items-center justify-start shadow-md hover:shadow-lg transition"
            >
              <img
                src={item.img}
                alt=""
                className="w-full h-[250px] object-contain mb-6"
              />
              <p className="whitespace-pre-line text-lg font-lexend font-medium text-center leading-relaxed text-[#013171]">
                {item.title}
              </p>
            </div>
          ))}
        </div>

        {/* Process */}
        <h2 className="text-2xl font-bold mb-4">How to Become a ResQ Partner</h2>

        {/* Download App Section */}
        <div className="bg-[#FAFAFA] rounded-2xl px-10 py-14 mt-10 flex flex-col md:flex-row items-center justify-between max-w-[1072px] mx-auto shadow-sm">
          {/* Left Side */}
          <div className="flex flex-col items-center md:items-start text-center md:text-left gap-4">
            <h3 className="text-2xl font-lexend font-bold text-gray-800 pe-20">
              Download the app and contact<br />our customer service team
            </h3>

            <img
              src="/images/GGPlay_processed.jpg"
              alt="Google Play"
              className="w-44"
            />

            <div className="flex flex-col items-center md:items-start">
              <img
                src="/images/scanqr.jpg"
                alt="QR Code"
                className="w-28 mt-0"
              />
            </div>
          </div>

          {/* Right Side */}
          <div className="mt-10 md:mt-0 flex justify-center">
            <img
              src="/images/iphone.jpg"
              alt="Phone"
              className="w-[300px]"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default PartnerJoinSection;
