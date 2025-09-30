import React, { useState, useEffect } from "react";
import {
  FaClock,
  FaHandshake,
  FaCheckCircle,
  FaChevronLeft,
  FaChevronRight,
} from "react-icons/fa";
import AOS from "aos";
import "aos/dist/aos.css";

const slides = [
  {
    image: "/images/Carbroken.jpg",
    caption: "No matter what issues you face on the road",
    subtext: "",
  },
  {
    image: "/images/cartire.jpg",
    caption: "On-site Rescue",
    subtext:
      "Quick fix for battery failures, flat tires, out-of-fuel, lockouts, or startup issues.",
  },
  {
    image: "/images/towtruck.jpg",
    caption: "Towing Assistance",
    subtext:
      "Transport your vehicle to a garage or destination when facing serious problems.",
  },
  {
    image: "/images/idea4_resq.jpg",
    caption: "Replacement Driver",
    subtext:
      "Support when you're unable to drive: fatigue, alcohol, or emergency situations.",
  },
];

const Home = () => {
  const [current, setCurrent] = useState(0);

  const nextSlide = () => {
    setCurrent((prev) => (prev + 1) % slides.length);
  };

  const prevSlide = () => {
    setCurrent((prev) => (prev - 1 + slides.length) % slides.length);
  };

  useEffect(() => {
    AOS.init({ duration: 800, once: true });
  }, []);

  return (
    <div className="font-sans">
      {/* Banner */}
      <section className="flex flex-col md:flex-row items-center justify-center gap-10 px-6 py-10">
  {/* Text */}
  <div className="text-center max-w-xl">
    <h2 className="text-3xl font-manrope mb-2 leading-snug">
      SAFETY ON EVERY <br /> JOURNEY
    </h2>
    <p className="text-[#E25757] font-semibold mb-6 text-sm tracking-wide">
      24/7 RESCUE SUPPORT
    </p>
    <div className="flex justify-center gap-4 flex-wrap">
      <button className="bg-[#8EADEB] text-white px-6 py-2 rounded-full text-sm hover:opacity-90 transition">
        About Us
      </button>
      <button className="bg-black text-white px-6 py-2 rounded-full text-sm hover:opacity-90 transition">
        Download App
      </button>
    </div>
  </div>

  {/* Images */}
  <div className="flex gap-6 items-end overflow-x-auto md:overflow-visible">
    <img
      src="/images/idea4_resq.jpg"
      className="w-[242px] h-[331px] object-cover rounded-lg shadow relative -translate-y-14"
      alt="step 1"
    />
    <img
      src="/images/cartire.jpg"
      className="w-[292px] h-[456px] object-cover rounded-lg shadow"
      alt="step 2"
    />
    <img
      src="/images/towtruck1.jpg"
      className="w-[303px] h-[451px] object-cover rounded-lg shadow relative -translate-y-6"
      alt="step 3"
    />
  </div>
</section>



      {/* Slider */}
      <section className="text-center py-20 bg-white">
  <h3 className="text-xl mb-8 font-lexend font-bold">Our Services</h3>
  <div className="relative max-w-[1100px] mx-auto">
    <img
      src={slides[current].image}
      className="rounded-3xl w-full h-[250px] md:h-[530px] object-cover"
      alt="slide"
    />

    <div className="absolute inset-0 bg-black/10 flex flex-col justify-center items-center text-white rounded-3xl px-4">
      <p className="text-lg md:text-[48px] font-lexend font-medium mb-2 text-center">
        {slides[current].caption}
      </p>
      {slides[current].subtext && (
        <p className="text-base md:text-[25px] font-lexend text-center max-w-xl leading-relaxed">
          {slides[current].subtext}
        </p>
      )}
      {current === 0 && (
        <button className="mt-4 bg-blue-900 px-6 py-2 rounded-full font-semibold text-sm md:text-base">
          Call now: (+84) 656 5565 7777
        </button>
      )}
    </div>

    {/* Navigation buttons */}
    <button
      onClick={prevSlide}
      className="absolute top-1/2 left-2 md:-left-20 transform -translate-y-1/2 bg-white border border-[#013171] text-[#013171] p-2 md:p-3 rounded-full shadow"
    >
      <FaChevronLeft />
    </button>
    <button
      onClick={nextSlide}
      className="absolute top-1/2 right-2 md:-right-20 transform -translate-y-1/2 bg-white border border-[#013171] text-[#013171] p-2 md:p-3 rounded-full shadow"
    >
      <FaChevronRight />
    </button>
  </div>
</section>


      {/* Why choose ResQ */}
      <section className="text-center py-14">
        <h3 className="text-xl font-manrope font-medium mb-10">
          Why Choose <span className="text-[#013171]">ResQ</span>?
        </h3>

        <div className="flex justify-center gap-8 px-4 flex-wrap">
          {/* Card 1 */}
          <div className="relative w-[386px] h-[495px] rounded-2xl overflow-hidden shadow-lg">
            <img
              src="/images/pexels-photo-1135738.jpeg"
              alt="24/7 Support"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex flex-col justify-center items-center text-white p-6">
              <h4 className="text-2xl font-lexend font-bold mb-2">24/7 Support</h4>
              <p className="text-sm font-manrope leading-relaxed text-center">
                Always available when you need us, day or night.
              </p>
            </div>
          </div>

          {/* Card 2 */}
          <div className="relative w-[386px] h-[495px] rounded-2xl overflow-hidden shadow-lg">
            <img
              src="/images/pexels-photo-2244746.jpeg"
              alt="Professional & Dedicated"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex flex-col justify-center items-center text-white p-6">
              <h4 className="text-2xl font-lexend font-bold mb-2">
                Professional & Dedicated
              </h4>
              <p className="text-sm font-manrope leading-relaxed text-center">
                Experienced team with modern tools for safe service.
              </p>
            </div>
          </div>

          {/* Card 3 */}
          <div className="relative w-[386px] h-[495px] rounded-2xl overflow-hidden shadow-lg">
            <img
              src="/images/pexels-photo-123457.png"
              alt="Transparent Pricing"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex flex-col justify-center items-center text-white p-6">
              <h4 className="text-2xl font-lexend font-bold mb-2">
                Transparent Pricing
              </h4>
              <p className="text-sm font-manrope leading-relaxed text-center">
                No hidden fees, clear pricing before confirmation.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Partner Section */}
      <section className="text-center py-14">
  <h3 className="text-xl mb-8 font-manrope font-medium">For ResQ Partners</h3>

  <div className="flex flex-col md:flex-row w-full min-h-[650px]">
    <div className="w-full md:w-7/12 relative h-[300px] md:h-[650px]">
      <img
        src="/images/pexel-photo-1.jpg"
        alt="Partner"
        className="absolute inset-0 w-full h-full object-cover"
      />
      <div className="absolute inset-0 bg-black/10"></div>
    </div>

    <div className="w-full md:w-5/12 bg-[#B3B3B3] flex flex-col justify-center items-center text-white px-6 py-10">
      <div className="text-left max-w-[90%]">
        <h3 className="text-3xl md:text-[60px] font-ledger leading-snug mb-10">
          Become a<br />
          Vehicle Rescue<br />
          Partner with<br />
          ResQ
        </h3>
        <button className="bg-black text-white font-semibold text-sm px-6 py-3 rounded-full flex items-center gap-2 hover:bg-gray-900 transition">
          Learn More
          <span className="text-lg">→</span>
        </button>
      </div>
    </div>
  </div>
</section>


      {/* Rescue process */}
      <section className="text-center py-20">
        <h3 className="text-[20px] font-semibold text-gray-700 font-manrope mb-10">
          How to Request Rescue via ResQ App
        </h3>
        <img
          src="/images/HomeWeb.png"
          alt="Rescue Process"
          className="mx-auto w-full max-w-[750px] px-4"
        />
      </section>

      {/* App download */}
      <div className="bg-[#FAFAFA] rounded-2xl px-6 md:px-10 py-14 mt-10 flex flex-col md:flex-row items-center justify-between max-w-[1072px] mx-auto shadow-sm">
  <div className="flex flex-col items-center md:items-start text-center md:text-left gap-4">
    <h3 className="text-xl md:text-2xl font-lexend font-bold text-gray-800">
      Download ResQ App Now!
    </h3>
    <img src="/images/GGPlay_processed.jpg" alt="Google Play" className="w-36 md:w-44" />
    <img src="/images/scanqr.jpg" alt="QR Code" className="w-24 md:w-28" />
  </div>

  <div className="mt-10 md:mt-0 flex justify-center">
    <img src="/images/iphone.jpg" alt="Phone" className="w-[200px] md:w-[300px]" />
  </div>
</div>

    </div>
  );
};

export default Home;
