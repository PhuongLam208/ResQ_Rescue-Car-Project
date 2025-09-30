import React from "react";

const AboutUs = () => {
  return (
    <div className="container mx-auto p-6 text-[17px]">
      <div className="container mx-auto p-6 flex flex-col items-center text-center font-manrope">
        {/* Title */}
        <h2
          className="text-4xl font-bold mb-4 leading-tight max-w-3xl text-center"
          style={{ fontSize: "34px" }}
        >
          ResQ – Fast & Professional Car Rescue Service
        </h2>

        <div className="mb-12"></div>

        {/* Subtitle */}
        <h3 className="font-semibold text-xl mb-2">Our Story</h3>

        <div className="w-[1072px] mx-auto mt-6">
          <p
            className="text-gray-700 leading-relaxed mb-4 text-justify"
            style={{ textIndent: '30px' }}
          >
            ResQ was established with the goal of helping drivers feel at ease on every journey.
            We understand that when problems arise on the road, the most important thing is fast,
            safe, and reasonably priced rescue service.
          </p>

          <p
            className="text-gray-700 leading-relaxed text-justify"
            style={{ textIndent: '30px' }}
          >
            With a wide network of partners and modern technology, ResQ helps users connect to the nearest
            rescue teams, provides transparent pricing before service confirmation, and ensures fair costs
            with no hidden fees. Whether it's a breakdown, flat tire, dead battery, or unexpected incident,
            you can trust ResQ to be timely, reliable, and honest.
          </p>
        </div>
      </div>

      {/* Image */}
      <div className="flex justify-center">
        <div className="relative rounded-lg shadow-lg" style={{ width: "1072px", height: "441px" }}>
          <img
            src="/images/aboutus2.jpg"
            alt="Car Assistance"
            className="w-full h-full object-cover rounded-lg"
          />
          <div className="absolute inset-0 bg-black/10 rounded-lg"></div>
        </div>
      </div>

      {/* Mission & Vision */}
      <div className="container mx-auto p-6 flex flex-col items-center text-center">
        <div className="mb-12"></div>
        <h3 className="font-semibold font-manrope text-xl mb-2">Mission & Vision</h3>
      </div>

      <div className="mt-10 flex flex-col md:flex-row w-full justify-center">
        <div className="w-[1072px] flex flex-col md:flex-row">
          {/* Mission */}
          <div className="relative h-[420px] w-full md:w-1/2 overflow-hidden rounded-tl-3xl rounded-bl-3xl">
            <img
              src="/images/aboutus4.jpeg"
              alt="Mission"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex flex-col items-center justify-center text-white text-center px-6">
              <h3 className="text-2xl font-bold font-manrope mb-4">Our Mission</h3>
              <p className="text-lg leading-relaxed font-manrope">
                To help drivers on every road get the fastest,<br />
                safest, and most affordable rescue assistance
              </p>
            </div>
          </div>

          {/* Vision */}
          <div className="relative h-[420px] w-full md:w-1/2 overflow-hidden rounded-tr-3xl rounded-br-3xl">
            <img
              src="/images/aboutus5.png"
              alt="Vision"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex flex-col items-center justify-center text-white text-center px-6">
              <h3 className="text-2xl font-bold font-manrope mb-4">Our Vision</h3>
              <p className="text-lg leading-relaxed font-manrope">
                To become the leading vehicle rescue platform in the region,<br />
                connecting millions of drivers with high-quality rescue partners
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Services */}
      <h3 className="text-xl font-bold font-manrope text-center mt-10">Our Services</h3>

      <div className="mt-6 flex justify-center">
        <div className="w-[1072px] grid md:grid-cols-3 gap-6">
          {/* Towing */}
          <div className="relative h-[400px] w-full overflow-hidden shadow-md">
            <img
              src="/images/aboutus6.jpg"
              alt="Towing"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
              <p className="font-lexend text-white text-lg text-center">Towing Assistance</p>
            </div>
          </div>

          {/* On-site Rescue */}
          <div className="relative h-[400px] w-full overflow-hidden shadow-md">
            <img
              src="/images/abouts1.jpg"
              alt="On-site Rescue"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
              <p className="font-lexend text-white text-lg text-center">On-site Rescue</p>
            </div>
          </div>

          {/* Replacement Driver */}
          <div className="relative h-[400px] w-full overflow-hidden shadow-md">
            <img
              src="/images/aboutus7.png"
              alt="Replacement Driver"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
              <p className="font-lexend text-white text-lg text-center">Replacement Driver</p>
            </div>
          </div>
        </div>
      </div>

      {/* Download App Section */}
      <div className="bg-gray-100 rounded-xl p-8 mt-10 flex flex-col md:flex-row items-center justify-between max-w-[1072px] mx-auto">
        {/* Left */}
        <div className="flex-1 text-center md:text-left">
          <h3 className="text-2xl font-lexend font-bold mb-4">Download the ResQ App Now!</h3>
          <img
            src="/images/GGPlay_processed.jpg"
            alt="Google Play"
            className="w-40 mb-4 mx-auto md:mx-0"
          />
          <div className="text-center md:text-left">
            <img
              src="/images/scanqr.jpg"
              alt="QR Code"
              className="w-28 mx-auto md:mx-0"
            />
          </div>
        </div>

        {/* Right */}
        <div className="flex-1 mt-8 md:mt-0 md:ml-8 flex justify-center">
          <img
            src="/images/iphone.jpg"
            alt="Phone"
            className="w-64"
          />
        </div>
      </div>
    </div>
  );
};

export default AboutUs;
