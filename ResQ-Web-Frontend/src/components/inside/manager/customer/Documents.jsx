import React, { useEffect, useState } from "react";
import { getUserPersonalDocuments, getImgDocuments } from "../../../../../manager";
import { Url } from "../../../../../admin";

const Documents = ({ customer }) => {
  const [activeTab, setActiveTab] = useState("cccd");
  const [cccdData, setCccdData] = useState(null);
  const [frontUrl, setFrontUrl] = useState(null);
  const [backUrl, setBackUrl] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!customer?.userId) return;

    const fetchDocuments = async () => {
      setLoading(true);
      try {
        const docs = await getUserPersonalDocuments(customer.userId);
        console.log("Fetched documents:", docs);

        if (docs.data.data.length > 0) {
          const cccd = docs.data.data.find((doc) => doc.type === "Identity Card");
          console.log(cccd)
          setCccdData(cccd || null);

          if (cccd) {
            // Lấy path từ URL
            const frontPath = new URLSearchParams(cccd.frontImageUrl.split("?")[1]).get("path");
            const backPath = new URLSearchParams(cccd.backImageUrl.split("?")[1]).get("path");
            console.log(frontPath)

            const extractPath =  `${Url}/api/resq/customer/image?path=`
            setFrontUrl(`${extractPath}${frontPath}`)
            setBackUrl(`${extractPath}${backPath}`)

          } else {
            setFrontUrl(null);
            setBackUrl(null);
          }
        } else {
          setCccdData(null);
          setFrontUrl(null);
          setBackUrl(null);
        }
      } catch (error) {
        console.error("Error fetching documents:", error);
        setCccdData(null);
        setFrontUrl(null);
        setBackUrl(null);
      }
      setLoading(false);
    };

    fetchDocuments();
  }, [customer]);

  const renderCCCD = () => (
    <>
      <div className="border p-4 rounded-lg shadow-sm mb-4">
        <p><strong>Citizen Number:</strong> {cccdData?.citizenNumber || "N/A"}</p>
        <p><strong>Issue Place:</strong> {cccdData?.issuePlace || "N/A"}</p>
        <p><strong>Issue Date:</strong> {cccdData?.issueDate || "N/A"}</p>
        <p><strong>Expiration Date:</strong> {cccdData?.expirationDate || "N/A"}</p>
        <p><strong>Verification Status:</strong> {cccdData?.verificationStatus || "N/A"}</p>
      </div>

      <div className="flex justify-center gap-10 mb-4">
        <div className="w-40 h-52 border rounded overflow-hidden">
          {frontUrl ? (
            <img src={frontUrl} alt="Front side" className="w-full h-full object-cover" />
          ) : (
            <div className="flex items-center justify-center h-full text-gray-400">No image</div>
          )}
        </div>
        <div className="w-40 h-52 border rounded overflow-hidden">
          {backUrl ? (
            <img src={backUrl} alt="Back side" className="w-full h-full object-cover" />
          ) : (
            <div className="flex items-center justify-center h-full text-gray-400">No image</div>
          )}
        </div>
      </div>

      <div className="flex justify-center gap-4">
        {cccdData?.verificationStatus === "Pending" && (
          <div><button className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded">
            Verify
          </button>
          <button className="bg-red-500 hover:bg-red-600 text-white px-6 py-2 rounded ms-3">
            Request Update
          </button>
          </div>
        )}
        {cccdData?.verificationStatus === "Expired" && (
          <button className="bg-red-500 hover:bg-red-600 text-white px-6 py-2 rounded">
            Request Update
          </button>
        )}
        {/* Nếu Verified thì không hiển thị nút */}
      </div>
      
    </>
  );

  return (
    <div className="max-w-3xl mx-auto p-4">
      <div className="flex justify-center gap-4 mb-4">
        <button
          onClick={() => setActiveTab("cccd")}
          className={`px-4 py-2 rounded-full border ${
            activeTab === "cccd"
              ? "bg-blue-800 text-white"
              : "text-blue-800 border-blue-800"
          }`}
        >
          ID Card
        </button>
      </div>

      {loading && <div className="text-center text-gray-500">Loading...</div>}

      {activeTab === "cccd" && !loading && cccdData && renderCCCD()}
      {activeTab === "cccd" && !loading && !cccdData && (
        <div className="text-center text-gray-500">No ID card data available.</div>
      )}
    </div>
  );
};

export default Documents;
