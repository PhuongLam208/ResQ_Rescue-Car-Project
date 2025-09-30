import { useState, useEffect } from "react";
import { customerAPI, personalDataAPI } from "../../../../../manager";

useState

const TopbarCustomer = ({ activeKey, onSelect, onBack, selectedCustomer, setSelectedCustomer, onReload }) => {
  const buttons = [
    { label: "Information", key: "information" },
    { label: "History", key: "history" },
    { label: "Violations", key: "violations" },
    { label: "Documents", key: "documents" },
  ];

  const [confirm, setConfirm] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [result, setResult] = useState(false);
  const [reject, setReject] = useState(false);
  const [isApprove, setIsApprove] = useState(false);
  const [reason, setReason] = useState('');
  const [errors, setErrors] = useState({});
  const [documents, setDocuments] = useState([]);

  const getCheckedDocuments = async () => {
    try {
      const response = await personalDataAPI.getUnverifiedUserData(selectedCustomer.userId);
    console.log(response.data)
      setDocuments(response.data);
    } catch (err) {
      console.log(err);
    }
  }

  {/*Approve Partner Function*/ }
  const approveCustomer = async (customer) => {
    try {
      await personalDataAPI.approveCustomer(customer.userId);
      setResult(true);
      setConfirm(false);
      setIsSuccess(true);
      setIsApprove(true);
      const updatedCustomer = await customerAPI.findCustomerById(customer.userId);
      console.log(updatedCustomer);
      setSelectedCustomer(updatedCustomer.data);
      setTimeout(() => {
        setResult(false);
        setIsApprove(false);
        onReload();
      }, 3000);
    } catch (error) {
      console.error('Error approve customer', error);
      setResult(true);
      setIsSuccess(false);
      setTimeout(() => {
        setResult(false);
      }, 3000);
    }
  };

  {/*Reject Partner Function*/ }
  const submitReject = async () => {
    try {
      const payload = {
        reason: reason
      };
      await personalDataAPI.rejectCustomer(selectedCustomer.userId, payload);
      await getCheckedDocuments();
      setResult(true);
      setIsSuccess(true);
      setReject(false);
      setConfirm(false);
      setErrors({});
      const updatedCustomer = await customerAPI.findCustomerById(selectedCustomer.userId);
      setSelectedCustomer(updatedCustomer.data);
      setTimeout(() => {
        setResult(false);
        setIsSuccess(false);
        onReload();
      }, 3000);
    } catch (error) {
      if (error.response && error.response.status === 400) {
        const { errors } = error.response.data;
        setErrors(errors);
      } else {
        console.error("Reject customer failed:", error);
        setResult(true);
        setIsSuccess(false);
        setTimeout(() => {
          setResult(false);
        }, 3000);
      }
    }
  };

  useEffect(() => {
    console.log(selectedCustomer)
    getCheckedDocuments();
  }, [])

  return (
    <div className="w-full mt-4 ">
      <div className="ml-5">
        <button onClick={onBack}
          className="border border-[#68A2F0] rounded-full w-16 h-10"
        >
          <img alt="Back" src="/images/icon-web/Reply Arrow1.png" className="w-7 m-auto" />
        </button>
      </div>
      <div className="flex flex-row space-x-10 justify-center items-center mt-2">
        {buttons.map((btn) => (
          <button
            key={btn.key}
            onClick={() => onSelect(btn.key)}
            className={`w-btn-customer py-1.5 transition font-roboto px-4
            ${activeKey === btn.key
                ? "bg-blue-900 text-white"
                : "btn-active-customer"
              }`}
          >
            {btn.label}
          </button>
        ))}
        {selectedCustomer?.pdStatus?.toLowerCase() === "pending" && documents &&
          <button onClick={() => setConfirm(true)}
            className="fixed bottom-20 right-9 bg-blue-600 hover:bg-blue-700 text-white font-semibold px-5 py-2 rounded-full shadow-lg">
            Verify Customer
          </button>
        }
      </div>
      {/*SELECT VERIFIED OPTION*/}
      {confirm &&
        <div className="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
          <div className="relative bg-white text-black px-10 py-4 rounded-lg shadow-lg text-lg">
            <button
              className="absolute top-4 right-4 text-xl font-bold"
              onClick={() => setConfirm(false)}
            >
              ✖
            </button>
            <p className="font-lexend">Do you want to approve customer {selectedCustomer?.fullName}?</p>
            <div className="relative flex justify-center gap-5 mt-5">
              <button onClick={() => approveCustomer(selectedCustomer)}
                className="bg-green-500 text-white px-4 py-2 rounded-3xl hover:bg-green-700 transition font-lexend">
                Approve
              </button>
              <button onClick={() => setReject(true)}
                className="bg-red-500 text-white px-4 py-2 rounded-3xl hover:bg-red-700 transition font-lexend">
                Reject
              </button>
            </div>

          </div>
        </div>
      }
      {/*REJECT + REASON*/}
      {reject && (
        <div class="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
          <div className="relative bg-white text-black px-10 py-6 rounded-xl shadow-lg w-[40vw]">
            <button
              className="absolute top-4 right-4 text-xl font-bold"
              onClick={() => {
                setReject(false);
                setReason('');
                setErrors({});
              }}
            >
              ✖
            </button>
            <h2 className="text-xl font-semibold mb-2 text-center text-red-600">REJECT CUSTOMER</h2>
            <form
              onSubmit={(e) => {
                e.preventDefault(); // Ngăn form reload trang
                submitReject();
              }}
            >
              <textarea
                className="border rounded-lg p-2 w-full my-2 h-36"
                placeholder="Reject customer reasons"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
              ></textarea>
              {errors.reason && <p className="text-red-500 text-sm mt-1">{errors.reason}</p>}

              <button
                type="submit"
                className="bg-red-600 text-white px-5 py-2 rounded-full hover:bg-red-700 float-right"
              >
                Confirm Reject
              </button>
            </form>
          </div>
        </div>
      )}
      {/*NOTI*/}
      {result && (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
          <div className="bg-white text-black px-10 py-6 rounded-xl shadow-lg text-center max-w-md">
            {isSuccess ?
              <img
                src="/images/icon-web/success.png"
                alt="success"
                className="w-24 mx-auto mb-4"
              /> :
              <img
                src="/images/icon-web/fail.png"
                alt="fail"
                className="w-24 mx-auto mb-4"
              />
            }
            <h2
              className={`text-xl font-semibold mb-2 ${isSuccess ? 'text-green-700' : 'text-red-700'
                }`}
            >
              {isSuccess ?
                (isApprove ? "Approve Customer Success!" : "Reject Customer Success!")
                : "Verify Customer Failed!"
              }
            </h2>
            {isSuccess && (
              <p className="text-gray-600">
                <span>
                  {isApprove
                    ? <>You have approved partner <strong>{selectedCustomer?.fullName}</strong>.</>
                    : <>You have rejected partner <strong>{selectedCustomer?.fullName}</strong>.</>
                  }
                </span>
                <br />
                The customer will receive a notification.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default TopbarCustomer;
