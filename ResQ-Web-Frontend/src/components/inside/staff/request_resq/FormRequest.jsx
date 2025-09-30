import React, { useState, useEffect } from "react";
import NewCustomer from "./NewCustomer";
import Select from "react-select";
import { createRequest, findServicesByType, getAllCustomers, getCusRequestForCancel, cancelRequest } from "../../../../../staff";

const FormRequest = () => {
  const [newCustomer, setNewCustomer] = useState(false);
  const getVNLocalDateTime = () => {
    const now = new Date();
    now.setHours(now.getHours() + 7);
    return now.toISOString().slice(0, 16);
  };
  const initialForm = {
    customerId: 0,
    ulocation: '',
    destination: '',
    description: '',
    rescueType: '',
    createdAt: getVNLocalDateTime(),
    per_km: '',
    km: '1',
    total: 0,
    paymentMethod: '',
    selectedServices: [],
    note: ''
  };
  const [formData, setFormData] = useState(initialForm);

  const [customers, setCustomers] = useState([]);
  const [selectedSrv, setSelectedSrv] = useState([]);
  const [services, setServices] = useState([]);


  const fetchCustomers = async () => {
    try {
      const res = await getAllCustomers();
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
        const response = await findServicesByType(selectedValue);
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
    }));
    submissionData.append("selectedServices", JSON.stringify(formData.selectedServices));

    try {
      let response = await createRequest(submissionData);
      if (response?.data) {
        setIsRun(true);
        setIsSuccess(true);
        setMessage("Create New Request Success!");
        setTimeout(() => {
          setIsRun(false);
          setMessage(null);
          setFormData({
            ...initialForm,
            createdAt: getVNLocalDateTime()
          });
          setSelectedSrv([]);
          setErrors({});
          setIsSuccess(false);
          fetchCustomers();
        }, 3000);
      } else {
        setIsRun(true);
        setIsSuccess(false);
        setMessage("Create New Request Fail!");
        setTimeout(() => {
          setIsRun(false);
          setMessage(null);
        }, 3000);
      }
    } catch (error) {
      if (error.response && error.response.status === 400) {
        const { message, errors } = error.response.data;
        setMessage(message);
        setErrors(errors);
      } else {
        setIsRun(true);
        setIsSuccess(false);
        setMessage("Create New Request Fail!");
        setIsSuccess(false); setTimeout(() => {
          setIsRun(false);
          setIsSuccess(false);
        }, 3000);
        console.log(error);
      };
    };
  }

  const handleBackCreate = () => {
    setNewCustomer(false);
    fetchCustomers();
  };

  const [isCancel, setIsCancel] = useState(false);
  const [cancelCustomerId, setCancelCustomerId] = useState(null);
  const [customerRequests, setCustomerRequests] = useState([]);
  const [selectedRequestId, setSelectedRequestId] = useState(null);
  const [reason, setReason] = useState("");

  const fetchRequestsByCustomerId = async (customerId) => {
    try {
      const res = await getCusRequestForCancel(customerId);
      console.log(res.data)
      setCustomerRequests(res.data || []);
    } catch (err) {
      console.error("Failed to fetch requests", err);
      setCustomerRequests([]);
    }
  };

  const submitCancel = async () => {
    const newErrors = {};

    if (!selectedRequestId) {
      newErrors.request = "Select request is required";
    }

    if (!reason.trim()) {
      newErrors.reason = "Reason for cancellation is required";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    try {
      await cancelRequest(selectedRequestId, reason);

      setIsCancel(false);
      setIsRun(true);
      setIsSuccess(true);
      setMessage("Cancel Request Success!")
      setTimeout(() => {
        setIsRun(false);
        setIsSuccess(false);
        setCancelCustomerId(null);
        setCustomerRequests([]);
        setSelectedRequestId(null);
        setReason("");
        setErrors({});
      }, 2000);
    } catch (error) {
      if (error.response && error.response.status === 400) {
        const { errors } = error.response.data;
        setErrors(errors);
      } else {
        console.error("Cancel request failed:", error);
        setIsSuccess(false);
        setTimeout(() => {
        }, 3000);
      }
    }
  };

  useEffect(() => {
    fetchCustomers();

  }, [formData.customerId, isCancel]);


  const inputClass = "w-full border border-gray-300 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#68A2F0] transition";
  const labelClass = "font-medium text-gray-700";
  const requestOptions = customerRequests.map(req => ({
    value: req.rrid,
    label: (
      <div>
        <div>{`${req.rescueType} - ${req.createdAt.slice(0, 10)}`}</div>
        {req.destination && <div className="text-gray-500 text-sm">{req.destination}</div>}
      </div>
    )
  }));

  return (
    <div>
      {newCustomer ? (
        <NewCustomer onBack={handleBackCreate} onCustomerCreated={handleCustomerCreated} />
      ) : (
        <div className="min-h-screen px-4 py-10">
          <div className="ml-5">
            <button
              onClick={() => setIsCancel(true)}
              className="border border-[#C30003] bg-[#C30003] text-white rounded-full w-36 h-10">
              Cancel Request
            </button>
          </div>
          <div className="flex flex-col items-center justify-center">
            <div className="w-full max-w-3xl rounded-2xl">
              <h1 className="text-3xl font-bold text-center text-[#013171] mb-10">
                Create New Request
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
                    value={
                      formData.customerId === 0
                        ? null
                        : customers
                          .map(cus => ({ value: cus.userId, label: cus.fullName }))
                          .find(option => option.value === formData.customerId)
                    }
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
                  />
                  {errors.customerId && <p className="text-red-500 text-sm mt-1">{errors.customerId}</p>}

                  <button
                    type="button"
                    className="bg-[#68A2F0] text-white text-sm mt-2 px-4 py-2 rounded-full hover:bg-[#4e8ad6]"
                    onClick={() => setNewCustomer(true)}
                  >
                    New Customer
                  </button>
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
                    Create New
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
          {isCancel &&
            <div className="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
              <div className="relative bg-white text-black px-10 py-6 rounded-xl shadow-lg w-[40vw]">
                <button
                  className="absolute top-4 right-4 text-xl font-bold"
                  onClick={() => {
                    setIsCancel(false);
                    setCancelCustomerId(null);
                    setCustomerRequests([]);
                    setSelectedRequestId(null);
                    setReason("");
                    setErrors({});
                  }}
                >
                  ✖
                </button>
                <h2 className="text-xl font-semibold mb-2 text-center text-red-600">REJECT REQUEST</h2>
                <form
                  onSubmit={(e) => {
                    e.preventDefault(); // Ngăn form reload trang
                    submitCancel();
                  }}
                >
                  {/* Select customer */}
                  <Select
                    options={customers.map(cus => ({
                      value: cus.userId,
                      label: cus.fullName
                    }))}
                    value={
                      cancelCustomerId === null
                        ? null
                        : customers
                          .map(cus => ({ value: cus.userId, label: cus.fullName }))
                          .find(option => option.value === cancelCustomerId)
                    }
                    onChange={(selected) => {
                      const selectedId = selected?.value || null;
                      setCancelCustomerId(selectedId);
                      if (selectedId) {
                        fetchRequestsByCustomerId(selectedId);
                      } else {
                        setCustomerRequests([]);
                        setSelectedRequestId(null);
                      }
                    }}
                    placeholder="Select or search name..."
                    isClearable
                  />

                  {/* Select request */}
                  <Select
                    className="mt-2"
                    placeholder="Select request to cancel"
                    options={requestOptions}
                    value={requestOptions.find(option => option.value === selectedRequestId) || null}
                    onChange={(selected) => setSelectedRequestId(selected?.value || null)}
                    isClearable
                  />
                  {errors.request && <p className="text-red-500 text-sm mt-1">{errors.request}</p>}

                  {/* Reason */}
                  <textarea
                    className="border rounded-lg p-2 w-full my-2 h-36"
                    placeholder="Cancel request reasons"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                  ></textarea>
                  {errors.reason && <p className="text-red-500 text-sm mt-1">{errors.reason}</p>}

                  <button
                    type="submit"
                    className="bg-red-600 text-white px-5 py-2 rounded-full hover:bg-red-700 float-right"
                  >
                    Cancel Request
                  </button>
                </form>
              </div>
            </div>
          }

        </div>
      )}
    </div>
  );
};

export default FormRequest;
