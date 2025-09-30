import React, { useEffect, useState } from "react";
import Swal from "sweetalert2";
import {
  getUserPersonalDocuments,
  getPartnerDocuments,
  Url,
} from "../../../../../admin";

const Documents = (partner) => {
  const [documents, setDocuments] = useState([]);

  const getStatusColor = (status) => {
    switch (status) {
      case "Pending":
        return "bg-blue-100 text-blue-800";
      case "Verified":
        return "bg-green-100 text-green-800";
      case "Requires Update":
        return "bg-orange-100 text-orange-800";
      case "Expired":
        return "bg-red-100 text-red-800";
      case "Rejected":
        return "bg-gray-200 text-gray-700";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

    const handleShowDetail = (doc) => {
    const frontImg = doc.frontImg
      ? `<img src="${Url}/api/resq/customer/${doc.frontImg}" alt="Front" class="w-32 h-auto rounded-xl shadow-md"/>`
      : `<span class="text-sm text-gray-500 italic">No front image</span>`;

    const backImg = doc.backImg
      ? `<img src="${Url}/api/resq/customer/${doc.backImg}" alt="Back" class="w-32 h-auto rounded-xl shadow-md"/>`
      : `<span class="text-sm text-gray-500 italic">No back image</span>`;

    const faceImg = doc.faceImg
      ? `<img src="${Url}/api/resq/customer/${doc.faceImg}" alt="Face" class="w-32 h-auto rounded-xl shadow-md"/>`
      : "";

    const htmlContent = `
      <div class="text-left text-sm space-y-2">
        <div><strong>Document Number:</strong> ${doc.documentNumber || doc.citizenNumber || ""}</div>
        <div><strong>Type:</strong> ${doc.documentType || doc.type || ""}</div>
        <div><strong>Expiration Date:</strong> ${doc.expirationDate || doc.expiration || ""}</div>
        <div><strong>Status:</strong> ${doc.documentStatus || doc.verificationStatus || ""}</div>
      </div>
      <div class="mt-4 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 text-center">
        <div><p class="mb-2 font-medium">Front</p>${frontImg}</div>
        <div><p class="mb-2 font-medium">Back</p>${backImg}</div>
        ${faceImg ? `<div><p class="mb-2 font-medium">Face</p>${faceImg}</div>` : ""}
      </div>
    `;

    const status = doc.documentStatus || doc.verificationStatus;

    let buttons = {
      confirmButtonText: "Close",
      showDenyButton: false,
      showCancelButton: false,
    };

    if (status === "Pending") {
      buttons = {
        confirmButtonText: "Verify",
        denyButtonText: "Reject",
        showDenyButton: true,
        showCancelButton: true,
        cancelButtonText: "Close",
      };
    } else if (status === "Expired") {
      buttons = {
        confirmButtonText: "Request Update",
        showDenyButton: true,
        denyButtonText: "Close",
        showCancelButton: false,
      };
    }

    Swal.fire({
      title: `<span class='text-xl font-semibold'>${doc.documentType || doc.type || "Document"}</span>`,
      width: 750,
      html: htmlContent,
      customClass: {
        popup: 'rounded-2xl px-6 py-4 text-left',
      },
      showClass: {
        popup: 'animate__animated animate__fadeInDown',
      },
      ...buttons,
    }).then((result) => {
      if (status === "Pending") {
        if (result.isConfirmed) {
          Swal.fire("✅ Document verified!", "", "success");
        } else if (result.isDenied) {
          Swal.fire({
            title: "Reason for Rejection",
            input: "textarea",
            inputLabel: "Please provide a reason",
            showCancelButton: true,
          }).then(({ value }) => {
            if (value) {
              Swal.fire("❌ Rejected with reason:", value, "info");
            }
          });
        }
      } else if (status === "Expired" && result.isConfirmed) {
        Swal.fire({
          title: "Request Update",
          input: "textarea",
          inputLabel: "Explain why update is needed",
          showCancelButton: true,
        }).then(({ value }) => {
          if (value) {
            Swal.fire("📨 Update requested:", value, "success");
          }
        });
      }
    });
  };

  useEffect(() => {
    const fetchDocuments = async () => {
      const personalDocs = await getUserPersonalDocuments(partner.partner.userId);
      const partnerDocs = await getPartnerDocuments(partner.partner.userId);

      const combined = [...personalDocs.data.data, ...partnerDocs.data.data];

      
const docsWithImages = await Promise.all(
        combined.map(async (doc) => {
          return {
            ...doc,
            frontImg: doc.frontImageUrl.split("/admin/personaldoc/")[1],
            backImg: doc.backImageUrl.split("/admin/personaldoc/")[1],
            faceImg: doc.faceImageUrl.split("/admin/personaldoc/")[1],
          };
        })
      );
      setDocuments(docsWithImages);
    };
    fetchDocuments();
  }, []);

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-300 shadow-lg">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-100">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 uppercase tracking-wide">#</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 uppercase tracking-wide">Type</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 uppercase tracking-wide">Expiration</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 uppercase tracking-wide">Status</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 uppercase tracking-wide">Action</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {documents.length === 0 ? (
            <tr>
              <td colSpan={5} className="text-center py-6 text-gray-500">
                No documents found.
              </td>
            </tr>
          ) : (
            documents.map((doc, index) => (
              <tr key={doc.pdId || doc.documentId || index} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 text-sm text-gray-900">{index + 1}</td>
                <td className="px-6 py-4 text-sm text-gray-900">{doc.documentType || doc.type}</td>
                <td className="px-6 py-4 text-sm text-gray-900">{doc.expirationDate || doc.expiration}</td>
                <td className={`px-6 py-4 text-sm font-medium rounded ${getStatusColor(doc.documentStatus || doc.verificationStatus)}`}>{doc.documentStatus || doc.verificationStatus}</td>
                <td className="px-6 py-4 text-sm font-medium">
                  <button
                    onClick={() => handleShowDetail(doc)}
                    className="text-indigo-600 hover:text-indigo-900 font-semibold"
                  >
                    View
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default Documents;
