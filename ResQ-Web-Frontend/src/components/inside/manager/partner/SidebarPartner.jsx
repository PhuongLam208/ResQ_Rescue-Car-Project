import { useEffect, useState } from "react";
import { partnerAPI, documentAPI } from "../../../../../manager";
import { Url } from "../../../../../admin";

const SidebarPartner = ({ onSelect, activeKey, selectedPartner, setSelectedPartner, onBack, onReload }) => {
  const buttons = [
    { label: "Performance", key: "performance" },
    { label: "Rescues", key: "rescues" },
    { label: "Violations", key: "violations" },
    { label: "Documents", key: "documents" },
    { label: "Vehicles", key: "vehicles" },
  ];

  const [confirm, setConfirm] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [reject, setReject] = useState(false);
  const [result, setResult] = useState(false);
  const [isApprove, setIsApprove] = useState(false);

  const [documents, setDocuments] = useState([]);
  const [reasonText, setReasonText] = useState('');
  const [selectedDocuments, setSelectedDocuments] = useState([]);
  const [errors, setErrors] = useState({});

  const getCheckedDocuments = async () => {
    try {
      const response = await documentAPI.getUnverifiedPartnerDoc(selectedPartner.partnerId);
      setDocuments(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCheckboxChange = (e, docType) => {
    const isChecked = e.target.checked;
    let updated = isChecked
      ? [...selectedDocuments, docType]
      : selectedDocuments.filter(name => name !== docType);
    setSelectedDocuments(updated);
    setReasonText(updated.length > 0 ? `Please check your ${updated.join(', ')}.` : '');
  };

  const approvePartner = async (partner) => {
    try {
      await partnerAPI.approveParnter(partner.partnerId);
      setResult(true);
      setIsSuccess(true);
      setIsApprove(true);
      setConfirm(false);

      const updatedPartner = await partnerAPI.findById(partner.partnerId);
      setSelectedPartner(updatedPartner.data);
      setTimeout(() => {
        setResult(false);
        setIsApprove(false);
        onReload();
      }, 3000);
    } catch (error) {
      console.error('Error approve partner', error);
      setResult(true);
      setIsSuccess(false);
      setTimeout(() => setResult(false), 3000);
    }
  };

  const submitReject = async () => {
    try {
      const payload = {
        documentTypes: selectedDocuments,
        reason: reasonText
      };
      await documentAPI.updatePartnerDoc(selectedPartner.partnerId, payload);
      await getCheckedDocuments();
      setResult(true);
      setIsSuccess(true);
      setReject(false);
      setConfirm(false);
      setErrors({});
      const updatedPartner = await partnerAPI.findById(selectedPartner.partnerId);
      setSelectedPartner(updatedPartner.data);
      setTimeout(() => {
        setResult(false);
        setIsSuccess(false);
        onReload();
      }, 3000);
    } catch (error) {
      if (error.response?.status === 400) {
        setErrors(error.response.data.errors || {});
      } else {
        console.error("Reject partner failed:", error);
        setResult(true);
        setIsSuccess(false);
        setTimeout(() => setResult(false), 3000);
      }
    }
  };

  useEffect(() => {
    getCheckedDocuments();
  }, [selectedPartner]);

  return (
    <div>
      <div className="w-60 bg-white rounded-2xl shadow p-4 flex flex-col items-center space-y-3">
        <div className="w-full flex justify-between items-center">
          <button onClick={onBack} className="border border-blue-400 rounded-full w-10 h-8">
            <img alt="Back" src="/images/icon-web/Reply Arrow1.png" className="w-5 mx-auto" />
          </button>
        </div>

        <img
          className="rounded-lg object-cover w-full h-32"
          alt="Avatar"
          src={
            selectedPartner?.avatar
              ? `${Url}/${selectedPartner.avatar}`
              : `${Url}/uploads/avatar/partner.png`
          }
        />
        <h2 className="text-lg font-semibold text-center text-blue-900 mt-2">
          {selectedPartner?.fullName}
        </h2>

        <div className="w-full mt-2 text-[14px] space-y-1 font-roboto">
          <p className="info-partner">Rescue Service: <br />
            <span className="text-black">
              {[selectedPartner?.resTow == 1 && "Tow", selectedPartner?.resFix == 1 && "Fix", selectedPartner?.resDrive == 1 && "Drive"]
                .filter(Boolean)
                .join(" || ")}
            </span>
          </p>
          <p className="info-partner">Phone No: <span className="text-black">{selectedPartner?.sdt}</span></p>
          <p className="info-partner">Email: <span className="text-black">{selectedPartner?.email}</span></p>
          <p className="info-partner">Status: <span className="text-black">{selectedPartner?.status}</span></p>
          <p className="info-partner">Joined Date: <span className="text-black">{new Date(selectedPartner?.createdAt).toLocaleString("vi-VN")}</span></p>
        </div>

        <div className="w-full mt-4 space-y-2 flex flex-col justify-center items-center">
          {buttons.map((btn) => (
            <button
              key={btn.key}
              onClick={() => onSelect(btn.key)}
              className={`w-btn-partner py-1.5 transition font-roboto ${activeKey === btn.key ? "bg-blue-900 text-white" : "btn-active-partner"}`}
            >
              {btn.label}
            </button>
          ))}
        </div>

        {(selectedPartner?.resTow === 2 || selectedPartner?.resFix === 2 || selectedPartner?.resDrive === 2) &&
          documents.length > 0 && (
            <button
              onClick={() => setConfirm(true)}
              className="fixed bottom-20 right-6 bg-blue-600 hover:bg-blue-700 text-white font-semibold px-5 py-2 rounded-full shadow-lg"
            >
              Verify Partner
            </button>
          )}
      </div>

      {/* CONFIRM APPROVE */}
      {confirm && (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
          <div className="bg-white text-black px-10 py-6 rounded-xl shadow-lg text-center w-[400px] relative">
            <button className="absolute top-4 right-4 text-xl font-bold" onClick={() => setConfirm(false)}>✖</button>
            <p className="font-semibold text-lg text-gray-800">
              Do you want to approve partner <span className="text-blue-600">{selectedPartner?.fullName}</span>?
            </p>
            <p className="text-gray-600 mt-2">
              The user registered: <br />
              <span className="font-medium text-gray-900">
                {
                  ["Tow", "Fix", "Drive"]
                    .filter((_, i) => [selectedPartner?.resTow, selectedPartner?.resFix, selectedPartner?.resDrive][i] === 2)
                    .join(" & ")
                }
              </span>
            </p>
            <div className="mt-6 flex justify-center gap-4">
              <button onClick={() => approvePartner(selectedPartner)} className="bg-green-600 text-white px-4 py-2 rounded-full hover:bg-green-700">Approve</button>
              <button onClick={() => setReject(true)} className="bg-red-600 text-white px-4 py-2 rounded-full hover:bg-red-700">Reject</button>
            </div>
          </div>
        </div>
      )}

      {/* REJECT MODAL */}
      {reject && (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
          <div className="relative bg-white text-black px-10 py-6 rounded-xl shadow-lg w-[40vw]">
            <button
              className="absolute top-4 right-4 text-xl font-bold"
              onClick={() => {
                setReject(false);
                setReasonText('');
                setSelectedDocuments([]);
                setErrors({});
              }}
            >
              ✖
            </button>
            <h2 className="text-xl font-semibold mb-2 text-center text-red-600">REJECT PARTNER</h2>
            <h4 className="font-bold mt-5">Partner's Documents</h4>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                submitReject();
              }}
            >
              <div className="border border-gray-300 rounded-xl p-4 h-60 overflow-y-auto space-y-2">
                {documents.map((doc) => (
                  <label key={doc.documentID} className="block mb-1">
                    <input
                      type="checkbox"
                      onChange={(e) => handleCheckboxChange(e, doc.documentType)}
                    />
                    <span className="ml-2">{doc.documentType}</span>
                  </label>
                ))}
              </div>
              {errors.selectedDocuments && <p className="text-red-500 text-sm mt-1">{errors.selectedDocuments}</p>}

              <textarea
                className="border rounded-lg p-2 w-full my-2 h-36"
                placeholder="Reject partner reasons"
                value={reasonText}
                onChange={(e) => setReasonText(e.target.value)}
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

      {/* RESULT NOTIFICATION */}
      {result && (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-600 bg-opacity-50 z-50">
          <div className="bg-white text-black px-10 py-6 rounded-xl shadow-lg text-center max-w-md">
            <img
              src={isSuccess ? "/images/icon-web/success.png" : "/images/icon-web/fail.png"}
              alt={isSuccess ? "success" : "fail"}
              className="w-24 mx-auto mb-4"
            />
            <h2 className={`text-xl font-semibold mb-2 ${isSuccess ? 'text-green-700' : 'text-red-700'}`}>
              {isSuccess
                ? (isApprove ? "Approve Partner Success!" : "Reject Partner Success!")
                : "Verify Partner Failed!"}
            </h2>
            {isSuccess && (
              <p className="text-gray-600">
                {isApprove
                  ? <>You have approved partner <strong>{selectedPartner?.fullName}</strong>.</>
                  : <>You have rejected partner <strong>{selectedPartner?.fullName}</strong>.</>}
                <br />The partner will receive a notification.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SidebarPartner;
