import React from 'react'
import { Link } from 'react-router-dom'

export const Service = () => {
  return (
    <div>
      <h3 className="text-xl font-bold font-manrope text-center mt-10">ResQ Services</h3>

      <div className="mt-6 flex justify-center">
        <div className="w-[1072px] grid md:grid-cols-3 gap-6">
          {/* Towing Assistance */}
          <Link to="/towtruck" className="relative h-[400px] w-full overflow-hidden shadow-md block">
            <img
              src="/images/aboutus6.jpg"
              alt="Towing Service"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
              <p className="font-lexend font-medium text-white text-center">Towing Assistance</p>
            </div>
          </Link>

          {/* On-site Rescue */}
          <Link to="/onsiterescue" className="relative h-[400px] w-full overflow-hidden shadow-md block">
            <img
              src="/images/abouts1.jpg"
              alt="On-site Rescue"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
              <p className="font-lexend font-medium text-white text-center">On-site Rescue</p>
            </div>
          </Link>

          {/* Replacement Driver */}
          <Link to="/driverservice" className="relative h-[400px] w-full overflow-hidden shadow-md block">
            <img
              src="/images/aboutus7.png"
              alt="Replacement Driver"
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
              <p className="font-lexend font-medium text-white text-center">Replacement Driver</p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
