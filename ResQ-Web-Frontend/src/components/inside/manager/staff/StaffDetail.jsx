import React from "react";
import { getUserStatus } from "../../../../utils/StatusStyle";

const StaffDetail = ({ staff, onBack }) => {
    return (
        <div>
            <div className="pt-6 pl-20">
                <button onClick={onBack}
                    className="border border-[#68A2F0] rounded-full w-16 h-10"
                >
                    <img alt="Back" src="/images/icon-web/Reply Arrow1.png" className="w-7 m-auto" />
                </button>
            </div>
            <div className="mx-48">
                <div className="w-full flex justify-center">
                    <img src="/images/icon-web/avatar.jpg" className="w-72" />
                </div>
                <table className="w-full text-[#013171] mt-10">
                    <tbody>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold w-[35%]">Full Name</td>
                            <td className="p-2 font-semibold w-[10%]">:</td>
                            <td className="py-2 px-16 text-right">{staff.fullName}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Email</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{staff.email}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Address</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{staff.address}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Phone No.</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{staff.sdt}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Joined Date</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{new Date(staff.createdAt).toLocaleString("vi-VN")}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Status</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16">
                                <div
                                    className={`text-xs py-1 w-[8vh] h-6 rounded-3xl text-center float-right ${getUserStatus(
                                        staff.status
                                    )}`}
                                >
                                    {staff.status}
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default StaffDetail;
