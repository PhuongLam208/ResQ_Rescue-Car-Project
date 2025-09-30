import React, { useState, useEffect } from "react";
import { getUserStatus } from "../../../../utils/StatusStyle";
import { customerAPI } from "../../../../../manager";
import { Url } from "../../../../../admin";

const Information = ({ customer }) => {
  const [dashboard, setDashboard] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchDashboard = async () => {
    try {
      const response = await customerAPI.dashboard(customer.userId);
      setDashboard(response.data);
      setIsLoading(false);
    } catch (err) {
      console.error("Cannot get dashboard data: " + err);
      setIsLoading(false);
    }
  }

  useEffect(() => {
    fetchDashboard();
  }, [])

  return <div>
    <div className="flex flex-row  mx-8 mt-10">
      {/* AVATAR */}
      <div className="border border-[#68A2F0] w-[300px] h-full">
        <img className="w-72" alt="Avatar"
          src={customer.avatar ?
            `${Url}/${customer.avatar}` :
            `${Url}/uploads/avatar/user.png`
          }
        />
      </div>
      <div className="ml-16 mt-5">
        <div className="flex flex-row gap-8">
          <div className="flex flex-row box-dashboard">
            <div className="font-lexend mt-[1px]">
              <p className="text-xl text-center font-semibold">
                {customer.totalRescues}
              </p>
              <p className="text-[15px] text-center font-medium">
                Total Rescue
              </p>
            </div>
            <div className="ml-3">
              <img src="../../../../../public/images/icon-web/history.png" width="55px" />
            </div>
          </div>
          <div className="flex flex-row box-dashboard">
            <div className="font-lexend mt-[1px]">
              <p className="text-xl text-center font-semibold">
                {dashboard.totalSuccess ? dashboard.totalSuccess : 0}
              </p>
              <p className="text-[15px] text-center font-medium">
                Total Success
              </p>
            </div>
            <div className="ml-3">
              <img src="../../../../../public/images/icon-web/bill.png" width="55px" />
            </div>
          </div>
          <div className="flex flex-row box-dashboard">
            <div className="font-lexend mt-[1px]">
              <p className="text-xl text-center font-semibold">
                {dashboard.totalCancel ? dashboard.totalCancel : 0}
              </p>
              <p className="text-[15px] text-center font-medium">
                Total Cancel
              </p>
            </div>
            <div className="ml-7 mt-[0.1rem]">
              <img src="../../../../../public/images/icon-web/fail.png" width="45px" />
            </div>
          </div>
        </div>
        <div className="flex flex-row gap-8 mt-6">
          <div className="flex flex-row box-dashboard">
            <div className="font-lexend mt-[1px]">
              <p className="text-xl text-center font-semibold">
                {(dashboard.percentSuccess ?? 0).toFixed(2)}
              </p>
              <p className="text-[15px] text-center font-medium">
                Percent Success
              </p>
            </div>
            <div className="ml-1">
              <img src="../../../../../public/images/icon-web/done_percentage.png" width="50px" />
            </div>
          </div>
          <div className="flex flex-row  box-dashboard">
            <div className="font-lexend mt-[1px]">
              <p className="text-xl text-center font-semibold">
                {dashboard.totalAmount ? new Intl.NumberFormat('vi-VN').format(dashboard.totalAmount) : 0}
              </p>
              <p className="text-[15px] text-center font-medium">
                Total Paid
              </p>
            </div>
            <div className="ml-3">
              <img src="../../../../../public/images/icon-web/paid_history.png" width="55px" />
            </div>
          </div>
          <div className="flex flex-row align-item-center box-dashboard">
            <div className="font-lexend mt-[1px]">
              <p className="text-xl text-center font-semibold">
                {customer.loyaltyPoint}
              </p>
              <p className="text-[15px] text-center font-medium">
                Total Point
              </p>
            </div>
            <div className="ml-8">
              <img src="../../../../../public/images/icon-web/bill_point.png" width="52px" />
            </div>
          </div>
        </div>
      </div>
    </div>
    {/* INFORMATION */}
    <div className="mt-14 mx-48">
      <table className="w-full text-[#013171]">
        <tbody>
          <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
            <td className="py-2 px-10 font-semibold w-[35%]">Full Name</td>
            <td className="p-2 font-semibold w-[10%]">:</td>
            <td className="py-2 px-16 text-right">{customer.fullName}</td>
          </tr>
          <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
            <td className="py-2 px-10 font-semibold">Phone No.</td>
            <td className="p-2 font-semibold">:</td>
            <td className="py-2 px-16 text-right">{customer.sdt}</td>
          </tr>
          <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
            <td className="py-2 px-10 font-semibold">Joined Date</td>
            <td className="p-2 font-semibold">:</td>
            <td className="py-2 px-16 text-right">{new Date(customer.createdAt).toLocaleString("vi-VN")}</td>
          </tr>
          <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
            <td className="py-2 px-10 font-semibold">Status</td>
            <td className="p-2 font-semibold">:</td>
            <td className="py-2 px-16">
              <div
                className={`text-xs py-1 w-[8vh] h-6 rounded-3xl text-center float-right ${getUserStatus(
                  customer.status
                )}`}
              >
                {customer.status}
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>;
};

export default Information;
