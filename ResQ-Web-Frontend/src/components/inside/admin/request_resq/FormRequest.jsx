import React, { useState, useEffect } from "react";
import NewCustomer from "./NewCustomer";
import { customerAPI, reqResQsAPI, serviceAPI, requestSrvAPI } from "../../../../../admin";
import Select from "react-select";

const FormRequest = ({ onBack, isEdit, req }) => {
  const [newCustomer, setNewCustomer] = useState(false);
  const getVNLocalDateTime = () => {
    const now = new Date();
    now.setHours(now.getHours() + 7);
    return now.toISOString().slice(0, 16);
  };

  const [formData, setFormData] = useState({
    customerId: isEdit && req ? req.customerId : 0,
    ulocation: isEdit && req ? req.ulocation : '',
    destination: isEdit && req ? req.destination : '',
    description: isEdit && req ? req.description : '',
    rescueType: isEdit && req ? req.rescueType : '',
    createdAt: isEdit && req && req.createdAt
      ? new Date(req.createdAt).toISOString().slice(0, 16)
      : getVNLocalDateTime(),
    per_km: '',
    km: '1',
    total: isEdit && req ? req.total : 0,
    paymentMethod: isEdit && req ? req.paymentMethod || '' : '',
  });

  const [customers, setCustomers] = useState([]);
  const [selectedSrv, setSelectedSrv] = useState([]);
  const [services, setServices] = useState([]);

  const fetchCustomers = async () => {
    try {
      const res = await customerAPI.getAllCustomers();
      setCustomers(res.data || []);
    } catch (err) {
      console.error("Failed to fetch customers", err);
    }
  };

  

  const handleCustomerCreated = async (newCustomer) => {
    console.log(newCustomer)
    setNewCustomer(false);
    setFormData((prev) => ({
      ...prev,
      customerId: newCustomer.userId,
      ulocation: newCustomer.address,
      createdAt: getVNLocalDateTime(),
    }));
    setCustomers((prev) => [...prev, newCustomer]);
   

  };

  const handleSelectMainSrv = async (e) => {
    const selectedValue = e.target.value;
    setFormData(prev => ({
      ...prev,
      rescueType: selectedValue,
      selectedService: '',
      per_km: '',
      total: ''
    }));
    setSelectedSrv([]);
    if (selectedValue) {
      try {
        const response = await serviceAPI.findBySrvType(selectedValue);
        setServices(response.data);
      } catch (err) {
        console.log("Cannot get services: " + err);
      }
    }
  };

  const calculatePrice = (per_km, km) => {
    const p = parseFloat(per_km);
    const k = parseFloat(km);
    return !isNaN(p) && !isNaN(k) ? (p * k) : '';
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => {
      const updated = { ...prev, [name]: value };

      if (name === 'km' || name === 'per_km') {
        updated.total = calculatePrice(
          name === 'per_km' ? value : updated.per_km,
          name === 'km' ? value : updated.km
        );
      }

      return updated;
    });
  };

  const [errors, setErrors] = useState({});
  const [isRun, setIsRun] = useState(false);
  const [message, setMessage] = useState("");
  const [isSuccess, setIsSuccess] = useState(false);
  const handleSubmit = async (e) => {
    e.preventDefault();
    const submissionData = new FormData();

    submissionData.append("requestDtoString", JSON.stringify({
      customerId: formData.customerId,
      ulocation: formData.ulocation,
      destination: formData.destination,
      description: formData.description,
      rescueType: formData.rescueType,
      createdAt: getVNLocalDateTime(),
      total: formData.total,
      paymentMethod: formData.paymentMethod,
      note: formData.note || '',
    }));
    submissionData.append("selectedServices", JSON.stringify(formData.selectedServices));

    try {
      let response;
      if (isEdit) {
        response = await reqResQsAPI.updateRequest(req.rrid, submissionData);
      } else {
        response = await reqResQsAPI.createNew(submissionData);
      }
      if (response?.data) {
        setIsRun(true);
        setIsSuccess(true);
        if (isEdit) {
          setMessage("Update Request Success!");
        } else {
          setMessage("Create New Request Success!");
        }
        setTimeout(() => {
          onBack();
        }, 3000);
      } else {
        setIsRun(true);
        setIsSuccess(false);
        if (isEdit) {
          setMessage("Update Request Fail!");
        } else {
          setMessage("Create New Request Fail!");
        }

      }
    } catch (error) {
      if (error.response && error.response.status === 400) {
        const { message, errors } = error.response.data;
        setMessage(message);
        setErrors(errors);
      } else {
        setIsRun(true);
        setIsSuccess(false);
        if (isEdit) {
          setMessage("Update Request Fail!");
        } else {
          setMessage("Create New Request Fail!");
          setIsSuccess(false); setTimeout(() => {
            setIsRun(false);
            setIsSuccess(false);
          }, 3000);
          console.log(error);
        }
      };
    };
  }

  const handleBackCreate = () => {
    setNewCustomer(false);
    fetchCustomers();
  };

  useEffect(() => {
    fetchCustomers();
    
    if (isEdit) {
      const init = async () => {
        try {
          const selectedResponse = await requestSrvAPI.getRequestServices(req.rrid);
          const selectedIds = selectedResponse.data.map(s => s.service.serviceId.toString());
          setSelectedSrv(selectedIds);
          setFormData(prev => ({ ...prev, selectedServices: selectedIds }));

          const srvRes = await serviceAPI.findBySrvType(formData.rescueType);
          setServices(srvRes.data);

          // ✅ Tìm dịch vụ đã chọn đầu tiên (với ResTow/ResDrive chỉ có 1)
          const selectedService = srvRes.data.find(s => selectedIds.includes(s.srvId.toString()));

          if (formData.rescueType === "ResFix") {
            const total = srvRes.data
              .filter(s => selectedIds.includes(s.srvId.toString()))
              .reduce((sum, s) => sum + (s.srvPrice || 0), 0);

            setFormData(prev => ({
              ...prev,
              per_km: total,
              total: calculatePrice(total, prev.km),
            }));
          } else if (selectedService) {
            setFormData(prev => ({
              ...prev,
              per_km: selectedService.srvPrice || '',
              total: calculatePrice(selectedService.srvPrice, prev.km),
            }));
          }
        } catch (err) {
          console.log("Lỗi khi khởi tạo dữ liệu edit:", err);
        }
      };


      init();
    }
  }, [formData.customerId]);



  const inputClass = "w-full border border-gray-300 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#68A2F0] transition";
  const labelClass = "font-medium text-gray-700";

  return (
    <div>
      {newCustomer ? (
        <NewCustomer onBack={handleBackCreate} onCustomerCreated={handleCustomerCreated} />
      ) : (
        <div className="min-h-screen px-4 py-10">
          <div className="ml-5">
            <button onClick={onBack} className="border border-[#68A2F0] rounded-full w-16 h-10">
              <img alt="Back" src="/images/icon-web/Reply Arrow1.png" className="w-7 m-auto" />
            </button>
          </div>
          <div className="flex flex-col items-center justify-center">
            <div className="w-full max-w-3xl rounded-2xl">
              <h1 className="text-3xl font-bold text-center text-[#013171] mb-10">
                {isEdit ? "Edit Rescue Request" : "Create New Request"}
              </h1>
              <form className="space-y-4" onSubmit={handleSubmit}>
                {/* Customer */}
                <div>
                  <label className={labelClass}>Customer</label>
                  <Select
                    options={customers.map(cus => ({
                      value: cus.userId,
                      label: cus.fullName
                    }))}
                    value={customers
                      .map(cus => ({ value: cus.userId, label: cus.fullName }))
                      .find(option => option.value === formData.customerId)}
                    onChange={(selected) => {
                      const selectedCustomer = customers.find(cus => cus.userId === selected?.value);
                      setFormData(prev => ({
                        ...prev,
                        customerId: selected?.value || '',
                        ulocation: selectedCustomer?.address || ''
                      }));
                    }}
                    placeholder="Select or search name..."
                    isClearable
                    isDisabled={isEdit}
                  />
                  {errors.customerId && <p className="text-red-500 text-sm mt-1">{errors.customerId}</p>}
                  {!isEdit &&
                    <button
                      type="button"
                      className="bg-[#68A2F0] text-white text-sm mt-2 px-4 py-2 rounded-full hover:bg-[#4e8ad6]"
                      onClick={() => setNewCustomer(true)}
                    >
                      New Customer
                    </button>
                  }
                </div>

                {/* Address */}
                <div>
                  <label className={labelClass}>Customer's Address</label>
                  <input
                    type="text"
                    name="ulocation"
                    value={formData.ulocation}
                    onChange={handleChange}
                    className={inputClass}
                    placeholder="Enter ulocation"
                  />
                  {errors.ulocation && <p className="text-red-500 text-sm mt-1">{errors.ulocation}</p>}
                </div>

                {/* Service type */}
                <div>
                  <label className={labelClass}>Service</label>
                  <select
                    name="service"
                    value={formData.rescueType}
                    onChange={handleSelectMainSrv}
                    className={inputClass}
                    disabled={isEdit}
                  >
                    <option value="">-- Select service --</option>
                    <option value="ResDrive">Replace Driver</option>
                    <option value="ResFix">On Site</option>
                    <option value="ResTow">Towing</option>
                  </select>
                  {errors.rescueType && <p className="text-red-500 text-sm mt-1">{errors.rescueType}</p>}
                </div>

                {/* Specific Service */}
                <div>
                  <label className={labelClass}>Specific Service</label>
                  {formData.rescueType === "ResFix" ? (
                    <div className="border border-gray-300 rounded-xl p-4 h-60 overflow-y-auto space-y-2">
                      {services.map((srv) => (
                        <label
                          key={srv.srvId}
                          className="flex items-start space-x-3 bg-gray-50 hover:bg-blue-50 transition p-2 rounded-lg"
                        >
                          <input
                            type="checkbox"
                            value={srv.srvId}
                            checked={selectedSrv.includes(srv.srvId.toString())}
                            onChange={(e) => {
                              const value = e.target.value;
                              let updated = [];

                              if (e.target.checked) {
                                updated = [...selectedSrv, value];
                              } else {
                                updated = selectedSrv.filter(id => id !== value);
                              }

                              setSelectedSrv(updated);

                              const total = services
                                .filter(s => updated.includes(s.srvId.toString()))
                                .reduce((sum, s) => sum + (s.srvPrice || 0), 0);

                              const newPrice = calculatePrice(total, formData.km);

                              setFormData(prev => ({
                                ...prev,
                                selectedServices: updated,
                                per_km: total,
                                total: newPrice
                              }));
                            }}
                            className="mt-1"
                            disabled={isEdit}
                          />
                          <div>
                            <div className="font-medium text-sm text-gray-700">{srv.srvName}</div>
                            <div className="text-xs text-gray-500">Price: {Number(srv.srvPrice).toLocaleString('vi-VN')}</div>
                          </div>
                        </label>
                      ))}
                    </div>
                  ) : (
                    <select
                      className={inputClass}
                      value={selectedSrv}
                      onChange={(e) => {
                        const selectedId = e.target.value;
                        setSelectedSrv(selectedId);

                        const selectedService = services.find(srv => srv.srvId.toString() === selectedId);
                        if (selectedService) {
                          const newPrice = calculatePrice(selectedService.srvPrice, formData.km);
                          setFormData(prev => ({
                            ...prev,
                            selectedServices: [selectedId],
                            per_km: selectedService.srvPrice,
                            total: newPrice
                          }));
                        }
                      }}
                      disabled={isEdit}
                    >
                      <option value="">--- Specific Service ---</option>
                      {services.map((srv) => (
                        <option key={srv.srvId} value={srv.srvId}>
                          {srv.srvName}
                        </option>
                      ))}
                    </select>
                  )}
                  {errors.specificSrv && <p className="text-red-500 text-sm mt-1">{errors.specificSrv}</p>}
                </div>

                {/* Only show when NOT ResFix */}
                {formData.rescueType !== "ResFix" && formData.rescueType !== "" &&
                  <>
                    <div>
                      <label className={labelClass}>Destination</label>
                      <input
                        type="text"
                        name="destination"
                        value={formData.destination}
                        onChange={handleChange}
                        className={inputClass}
                        placeholder="Enter address"
                      />
                      {errors.destination && <p className="text-red-500 text-sm mt-1">{errors.destination}</p>}
                    </div>
                    <div>
                      <label className={labelClass}>Per Km</label>
                      <input
                        type="text"
                        name="per_km"
                        value={Number(formData.per_km).toLocaleString('vi-VN')}
                        onChange={handleChange}
                        className={inputClass}
                        disabled
                      />
                    </div>
                    <div>
                      <label className={labelClass}>Km</label>
                      <input
                        type="number"
                        name="km"
                        value={formData.km}
                        onChange={handleChange}
                        className={inputClass}
                        disabled
                      />
                    </div>
                  </>
                }

                {/* Price */}
                <div>
                  <label className={labelClass}>Total Price</label>
                  <input
                    type="number"
                    name="total"
                    value={Number(formData.total).toLocaleString('vi-VN')}
                    readOnly
                    className={`${inputClass} bg-gray-100`}
                    placeholder="Auto-calculated"
                  />
                </div>

                {/* Others */}
              
                <div>
                  <label className={labelClass}>Request Time</label>
                  <input
                    type="datetime-local"
                    name="createdAt"
                    value={formData.createdAt}
                    onChange={handleChange}
                    className={inputClass}
                    disabled
                  />
                </div>
                <div>
                  <label className={labelClass}>Description</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={4}
                    className={`${inputClass} resize-none`}
                    placeholder="Describe the issue or request details..."
                  />
                </div>
                <div className="text-center pt-4">
                  <button
                    type="submit"
                    className="bg-[#68A2F0] text-white font-semibold px-6 py-3 rounded-full hover:bg-[#6094d7] transition duration-300 shadow-md"
                  >
                    {isEdit ? "Update Request" : "Create New"}
                  </button>
                </div>
              </form>
            </div>
          </div>{/* Popup */}
          {isRun && (
            <div className="fixed inset-0 z-70 flex items-center pl-[42vw] bg-gray-600 bg-opacity-40">
              <div className="bg-white px-20 py-10 rounded-2xl shadow-xl text-center">
                {isSuccess ?
                  <div className="pl-28 pb-5">
                    <img src="/images/icon-web/success.png" alt="success" className="w-[8vw]" />
                  </div>
                  :
                  <div className="pl-28 pb-5">
                    <img src="/images/icon-web/fail.png" alt="fail" className="w-[8vw]" />
                  </div>
                }
                <h2 className="text-2xl text-gray-600 w-[20vw]">
                  {message}
                </h2>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default FormRequest;
