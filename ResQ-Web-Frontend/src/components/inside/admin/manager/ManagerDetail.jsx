import React from "react";
import { getUserStatus } from "../../../../utils/StatusStyle";
import { Url } from "../../../../../admin";

const ManagerDetail = ({ manager, onBack }) => {
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
                    <img className="w-72" alt="Avatar"
                        src={manager.avatar ?
                            `${Url}/${manager.avatar}` :
                            `${Url}/uploads/avatar/staff.jpg`
                        }
                    />
                </div>
                <table className="w-full text-[#013171] mt-10">
                    <tbody>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold w-[35%]">Full Name</td>
                            <td className="p-2 font-semibold w-[10%]">:</td>
                            <td className="py-2 px-16 text-right">{manager.fullName}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Email</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{manager.email}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Address</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{manager.address}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Phone No.</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{manager.sdt}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Joined Date</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16 text-right">{new Date(manager.createdAt).toLocaleString("vi-VN")}</td>
                        </tr>
                        <tr className="odd:bg-[#e4e4e4] even:bg-white h-[45px]">
                            <td className="py-2 px-10 font-semibold">Status</td>
                            <td className="p-2 font-semibold">:</td>
                            <td className="py-2 px-16">
                                <div
                                    className={`text-xs py-1 w-[8vh] h-6 rounded-3xl text-center float-right ${getUserStatus(
                                        manager.status
                                    )}`}
                                >
                                    {manager.status}
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ManagerDetail;
