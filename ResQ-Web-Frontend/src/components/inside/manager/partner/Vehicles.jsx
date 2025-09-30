import React, { useEffect, useState } from "react";
import Swal from "sweetalert2";
import {Url} from "../../../../../admin";
import {getPartnerVehicle} from "../../../../../manager";

const Vehicles = ({ partner }) => {
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      const data = await getPartnerVehicle(partner.partnerId);

     const vehicle = data.data;
      let vehicleWithImages = {};

      if (vehicle) {
        const extractPath = (imgPath) =>
          imgPath ? `${Url}/api/resq/customer/${imgPath.split("/admin/vehicle/")[1]}` : null;

        vehicleWithImages = {
          ...vehicle,
          imageFront: extractPath(vehicle.frontImage),
          imageBack: extractPath(vehicle.backImage),
          imageTem: extractPath(vehicle.imgTem),
          imageTool: extractPath(vehicle.imgTool),
          imageDevice: extractPath(vehicle.imgDevice),
        };
      }
      console.log(vehicleWithImages)
      console.log(vehicleWithImages.length === 0)
      setVehicles(vehicleWithImages);
    };
    console.log(partner)
    fetchData();
  }, [partner]);


  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case "ACTIVE":
        return "bg-green-100 text-green-800";
      case "INACTIVE":
        return "bg-yellow-100 text-yellow-800";
      case "MAINTENANCE":
        return "bg-blue-100 text-blue-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="p-6 bg-white rounded-xl border border-gray-200 shadow-md">
      {vehicles.length === 0 ? (
        <p className="text-center py-6 text-gray-500">
          No vehicle found.
        </p>
      ) : (
        <>
          <p className="text-2xl font-semibold text-gray-700">Vehicle #{vehicles.vehicleId}</p>
          <div class="text-sm text-gray-700 mt-5">
            <div class="grid grid-cols-2 gap-10 border-b pb-2">
              <div><b>User ID:</b> {vehicles.userId ? vehicles.userId : "No data"}</div>
              <div><b>Status:</b> <span class={`${getStatusColor(vehicles.vehicleStatus)} px-2 py-0.5 rounded`}>{vehicles.vehicleStatus ? vehicles.vehicleStatus : "No data"}</span></div>
              <div><b>Brand:</b> {vehicles.brand ? vehicles.brand : "No data"}</div>
              <div><b>Model:</b> {vehicles.model ? vehicles.model : "No data"}</div>
              <div><b>Year:</b> {vehicles.year ? vehicles.year : "No data"}</div>
            </div>
            <div class="grid grid-cols-2 gap-5 mt-3">
              <div>
                <b>Front Image: </b>
                {
                  vehicles.imageFront ?
                    <img
                      src={vehicles.imageFront}
                      className="w-full border rounded shadow mt-3"
                      alt="Front Image"
                    /> :
                    <span className="text-sm text-gray-500 italic mt-3">No front image</span>
                }
              </div>
              <div>
                <b>Back Image: </b>
                {
                  vehicles.imageBack ?
                    <img
                      src={vehicles.imageBack}
                      className="w-full border rounded shadow mt-3"
                      alt="Back Image"
                    /> :
                    <span className="text-sm text-gray-500 italic mt-3">No back image</span>
                }
              </div>
              <div>
                <b>Inspection Stamp: </b>
                {
                  vehicles.imageTem ?
                    <img
                      src={vehicles.imageTem}
                      className="w-full border rounded shadow mt-3"
                      alt="Inspection Stamp Image"
                    /> :
                    <span className="text-sm text-gray-500 italic mt-3">No inspection stamp image</span>
                }
              </div>
              <div>
                <b>Tools: </b>
                {
                  vehicles.imageTool ?
                    <img
                      src={vehicles.imageTool}
                      className="w-full border rounded shadow mt-3"
                      alt="Tool Image"
                    /> :
                    <span className="text-sm text-gray-500 italic mt-3">No tool image</span>
                }
              </div>
              <div>
                <b>Device: </b>
                {
                  vehicles.imageDevice ?
                    <img
                      src={vehicles.imageDevice}
                      className="w-full border rounded shadow mt-3"
                      alt="Device Image"
                    /> :
                    <span className="text-sm text-gray-500 italic mt-3">No device image</span>
                }
              </div>
            </div>
          </div>
        </>
      )

      }
    </div>
  );
};

export default Vehicles;
