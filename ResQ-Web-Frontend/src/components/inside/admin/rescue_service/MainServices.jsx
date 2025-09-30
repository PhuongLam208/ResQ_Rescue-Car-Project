import React, { useEffect, useRef, useState } from "react";
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";
import {
  getAllService,
  getServiceById,
  updateServicePrice,
  searchService,
  filterServiceByType
} from "../../../../../admin";
import "../../../../styles/admin/general.css";

const MainServices = () => {
  const [services, setServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);
  const [selectedOption, setSelectedOption] = useState('Options');


  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const fetchServices = async () => {
    try {
      const response = await getAllService();
      setServices(response.data.data);
      
    } catch (error) {
      console.error("Failed to fetch services:", error);
      Swal.fire("Error", "Failed to load services.", "error");
    }
  };

  const handleSearch = async () => {
    try {
      if (!searchTerm.trim()) {
        fetchServices();
      } else {
        const results = await searchService(searchTerm);
        setServices(results.data.data);
        
      }
    } catch (error) {
      console.error("Search failed:", error);
      Swal.fire("Error", "Search failed.", "error");
    }
  };

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      handleSearch();
    }, 500);
    return () => clearTimeout(delayDebounce);
  }, [searchTerm]);

  useEffect(() => {
    fetchServices();
  }, []);

  const handleInputChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSave = async (id, updatedPrices) => {
    try {
      const result = await updateServicePrice(id, updatedPrices);
      if (result) {
        Swal.fire("Success", "Service price updated successfully!", "success");
        fetchServices();
      } else {
        Swal.fire("Error", "Failed to update service price.", "error");
      }
    } catch (error) {
      console.error("Update failed:", error);
      Swal.fire("Error", "Failed to update service price.", "error");
    }
  };

  const openServiceForm = async (service, editable) => {
    let serviceData = service;
    if (!editable) {
      try {
        const fetched = await getServiceById(service.serviceId);
        if (!fetched) return;
        serviceData = fetched.data.data;
        
      } catch (error) {
        console.error("Failed to fetch service detail:", error);
        Swal.fire("Error", "Failed to load service details.", "error");
        return;
      }
    }

    Swal.fire({
  title: editable ? "Edit Service Price" : "Service Details",
  html: `
    <div class="p-4">
      <div class="mb-4 flex items-center">
        <label class="block text-sm font-bold mr-2 w-1/4">Service Name:</label>
        <input id="service_name" class="swal2-input w-full text-xs p-2" value="${
          serviceData.serviceName || ""
        }" disabled />
      </div>
      <div class="mb-4 flex items-center">
        <label class="block text-sm font-bold mr-2 w-1/4">Service Type:</label>
        <input id="service_type" class="swal2-input w-full text-xs p-2" value="${
          serviceData.serviceType || ""
        }" disabled />
      </div>
      <div class="mb-4 flex items-center">
        <label class="block text-sm font-bold mr-2 w-1/4">Fixed Price:</label>
        <input id="fixed_price" type="number" class="swal2-input w-full text-xs p-2" value="${
          serviceData.fixedPrice ?? 0
        }" ${!editable ? "disabled" : ""} />
      </div>
      <div class="mb-4 flex items-center">
        <label class="block text-sm font-bold mr-2 w-1/4">Price per km:</label>
        <input id="price_per_km" type="number" class="swal2-input w-full text-xs p-2" value="${
          serviceData.pricePerKm ?? 0
        }" ${!editable ? "disabled" : ""} />
      </div>
      <div class="mb-4 flex items-center">
        <label class="block text-sm font-bold mr-2 w-1/4">Created At:</label>
        <input id="created_at" class="swal2-input w-full text-xs p-2" value="${
          formatDate(serviceData.createdAt)
        }" disabled />
      </div>
    </div>
  `,
      focusConfirm: false,
      showCancelButton: true,
      cancelButtonText: "Back",
      confirmButtonText: editable ? "Save" : "Close",
      showConfirmButton: editable,
      customClass: { popup: "w-[600px]" },
      preConfirm: () => {
        if (!editable) return;
        const fixedPriceVal = parseFloat(
          document.getElementById("fixed_price").value
        );
        const pricePerKmVal = parseFloat(
          document.getElementById("price_per_km").value
        );

        if (isNaN(fixedPriceVal) || fixedPriceVal < 0) {
          Swal.showValidationMessage("Invalid fixed price");
          return false;
        }
        if (isNaN(pricePerKmVal) || pricePerKmVal < 0) {
          Swal.showValidationMessage("Invalid price per km");
          return false;
        }

        return { fixedPrice: fixedPriceVal, pricePerKm: pricePerKmVal };
      },
    }).then((result) => {
      if (result.isConfirmed && editable && result.value) {
        handleSave(serviceData.serviceId, result.value);
      }
    });
  };
 const handleFilter = async (type) => {
  try {
    setSelectedOption(type); // Cập nhật text cho nút
    setDropdownOpen(false);  // Đóng dropdown
     const results = await filterServiceByType(type);
    setServices(results.data.data);
  } catch (error) {
    console.error('Filter failed:', error);
    Swal.fire('Error', 'Could not filter services.', 'error');
  }
};

function formatDate(isoString) {
  const date = new Date(isoString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
  return (
    <div className="p-4">
      <div className="container-function flex justify-between items-center">
        <div className="allsearch flex items-center">
          <input
            type="text"
            className="input-search"
            placeholder="Search..."
            value={searchTerm}
            onChange={handleInputChange}
          />
          <img
            src="/images/icon-web/Search.png"
            className="click-search cursor-pointer"
            onClick={handleSearch}
            alt="Search"
          />
        </div>

        <div className="allselect" ref={dropdownRef}>
          <div className="relative inline-block text-left">
            <div>
              <button
                type="button"
                className="inline-flex w-full justify-center gap-x-1.5 rounded-2xl bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-xs ring-1 ring-gray-300 ring-inset hover:bg-gray-50"
                id="menu-button"
                onClick={() => setDropdownOpen(!dropdownOpen)}
              >
                {selectedOption}
                <svg className="-mr-1 size-5 text-gray-400" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                  <path fillRule="evenodd" d="M5.22 8.22a.75.75 0 0 1 1.06 0L10 11.94l3.72-3.72a.75.75 0 1 1 1.06 1.06l-4.25 4.25a.75.75 0 0 1-1.06 0L5.22 9.28a.75.75 0 0 1 0-1.06Z" clipRule="evenodd" />
                </svg>
              </button>

            </div>

            {dropdownOpen && (
              <div className="absolute right-0 z-10 mt-2 w-56 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black/5 focus:outline-none">
                <div className="py-1">
                  <button
                    onClick={() => {
                      setSelectedOption('All');
                      setDropdownOpen(false);
                      fetchServices(); // gọi lại tất cả dịch vụ
                    }}
                    className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                  >
                    All
                  </button>
                    <button
                    onClick={() => handleFilter("ResTow")}
                    className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                  >
                    ResTow
                  </button>
                  <button
                    onClick={() => handleFilter("ResFix")}
                    className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                  >
                    ResFix
                  </button>
                  <button
                    onClick={() => handleFilter("ResDrive")}
                    className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                  >
                    ResDrive
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
<table className="min-w-full border border-gray-200 rounded overflow-hidden shadow-sm mt-4">
  <thead className="bg-blue-400 text-white text-sm">
    <tr>
      <th className="px-4 py-2 text-left whitespace-nowrap">ID</th>
      <th className="px-4 py-2 text-left whitespace-nowrap">Service Name</th>
      <th className="px-4 py-2 text-left whitespace-nowrap">Type</th>
      <th className="px-4 py-2 text-left whitespace-nowrap">Fixed Price</th>
      <th className="px-4 py-2 text-left whitespace-nowrap">Price / km</th>
      <th className="px-4 py-2 text-left whitespace-nowrap">Created At</th>
      <th className="px-4 py-2 text-left whitespace-nowrap">Actions</th>
    </tr>
  </thead>
  <tbody className="bg-white text-sm text-gray-800">
    {Array.isArray(services) && services.map((service, idx) => (
      <tr
        key={service.serviceId || idx}
        className="border-t border-gray-100 hover:bg-gray-50"
      >
        <td className="px-4 py-2">{idx + 1}</td>
        <td className="px-4 py-2">{service.serviceName}</td>
        <td className="px-4 py-2">{service.serviceType}</td>
        <td className="px-4 py-2">{service.fixedPrice}</td>
        <td className="px-4 py-2">{service.pricePerKm}</td>
        <td className="px-4 py-2">
          {formatDate(service.createdAt)}
        </td>
        <td className="px-4 py-2">
  <div className="flex space-x-2">
    <button
      onClick={() => openServiceForm(service, false)}
      className="flex-1 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded-md text-xs transition"
    >
      View
    </button>
    <button
      onClick={() => openServiceForm(service, true)}
      className="flex-1 border border-blue-500 text-blue-500 hover:bg-blue-50 px-3 py-1 rounded-md text-xs transition"
    >
      ✏️ Edit
    </button>
  </div>
</td>

      </tr>
    ))}
  </tbody>
</table>

    </div>
  );
};

export default MainServices;
