import React, { useState } from "react";
import { managerAPI } from "../../../../../admin";

const FormManager = ({ onBack, manager, isEdit }) => {
    const [errors, setErrors] = useState({});
    const [formData, setFormData] = useState({
        fullName: isEdit && manager ? manager.fullName : '',
        username: isEdit && manager ? manager.userName : '',
        email: isEdit && manager ? manager.email : '',
        sdt: isEdit && manager ? manager.sdt : '',
        address: isEdit && manager ? manager.address : '',
        password: isEdit && manager ? '' : '2025@ResQ',
        avatar: isEdit && manager ? manager.avatar : '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const [fileName, setFileName] = useState("No file chosen");
    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setFormData({ ...formData, avatar: file });
            setFileName(file.name);
        }
    };

    const [isRun, setIsRun] = useState(false);
    const [message, setMessage] = useState("");
    const [isSuccess, setIsSuccess] = useState(false);
    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage(null);

        try {
            const newManager = new FormData();
            const { avatar, ...dto } = formData;
            if (isEdit) {
                dto.userId = manager.userId;
            }
            newManager.append("userDtoString", JSON.stringify(dto));
            if (avatar) newManager.append("avatar", avatar);
            let response;
            if (isEdit) {
                response = await managerAPI.updateManager(manager.userId, newManager);
            } else {
                response = await managerAPI.createNew(newManager);
            }
            if (response && response.data) {
                setIsRun(true);
                setIsSuccess(true);
                if (isEdit) {
                    setMessage("Update Manager Success!");
                } else {
                    setMessage("Create New Manager Success!");
                }
                setTimeout(() => {
                    if (isEdit) {
                        onBack();
                    } else {
                        setIsRun(false);
                        setIsSuccess(false);
                        setFormData({
                            fullName: '',
                            username: '',
                            email: '',
                            address: '',
                            sdt: '',
                            currentPassword: '',
                            password: '',
                            avatar: '',
                        });
                        setErrors({});
                        setFileName("No file chosen")
                    }
                }, 3000);
            } else {
                setIsRun(true);
                setIsSuccess(false);
                if (isEdit) {
                    setMessage("Update Manager Fail!");
                } else {
                    setMessage("Create New Manager Fail!");
                }

            }
        } catch (error) {
            if (error.response && error.response.status === 400) {
                const { message, errors } = error.response.data;
                setMessage(message);
                setErrors(errors);
            } else if (error.response && error.response.status === 409) {
                const { message } = error.response.data;
                setIsRun(true);
                setMessage(message);
                setIsSuccess(false); setTimeout(() => {
                    setIsRun(false);
                    setIsSuccess(false);
                    setErrors({});
                }, 3000);
            } else {
                setIsRun(true);
                setIsSuccess(false);
                if (isEdit) {
                    setMessage("Update Manager Fail!");
                } else {
                    setMessage("Create New Manager Fail!");
                }
                setIsSuccess(false); setTimeout(() => {
                    setIsRun(false);
                    setIsSuccess(false);
                }, 3000);
            };
        }
    }

    // const [showOTP, setShowOTP] = useState(false);
    // const [enteredOTP, setEnteredOTP] = useState("");
    // const DEFAULT_OTP = "171216";


    const inputClass =
        "w-full border border-gray-300 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#68A2F0] transition";
    const labelClass = "font-medium text-gray-700";
    return (
        <div>
            <div className="pt-10 pl-20">
                <button onClick={onBack}
                    className="border border-[#68A2F0] rounded-full w-16 h-10"
                >
                    <img alt="Back" src="/images/icon-web/Reply Arrow1.png" className="w-7 m-auto" />
                </button>
            </div>
            <div className="flex flex-col items-center justify-center px-4 py-3">
                <div className="w-full max-w-xl rounded-2xl">
                    <h1 className="text-3xl font-bold text-center text-[#013171] mb-10">
                        {isEdit ? "Edit Manager" : "Create New Manager"}
                    </h1>
                    <form className="space-y-8" onSubmit={handleSubmit}>
                        <div>
                            <label className={labelClass}>Full Name</label>
                            <input
                                type="text"
                                name="fullName"
                                value={formData.fullName}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.fullName && <p className="text-red-500 text-sm mt-1">{errors.fullName}</p>}
                        </div>
                        <div>
                            <label className={labelClass}>Username</label>
                            <input
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.userName && <p className="text-red-500 text-sm mt-1">{errors.userName}</p>}
                        </div>
                        <div>
                            <label className={labelClass}>Address</label>
                            <input
                                type="text"
                                name="address"
                                value={formData.address}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.address && <p className="text-red-500 text-sm mt-1">{errors.address}</p>}
                        </div>
                        <div>
                            <label className={labelClass}>Phone No.</label>
                            <input
                                type="text"
                                name="sdt"
                                value={formData.sdt}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.sdt && <p className="text-red-500 text-sm mt-1">{errors.sdt}</p>}
                        </div>
                        <div>
                            <label className={labelClass}>Email</label>
                            <input
                                type="text"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
                        </div>
                        <div>
                            <label className={labelClass}>Password</label>
                            <input
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
                        </div>
                        {/*<div>
                            <label className={labelClass}>Avatar</label>
                            <div className="flex items-center gap-3">
                                <label
                                    htmlFor="imageUpload"
                                    className="bg-[#68A2F0] text-white text-sm px-4 py-2 rounded-full cursor-pointer hover:bg-[#4e8ad6] transition"
                                >
                                    Choose File
                                </label>
                                <input
                                    type="file"
                                    id="imageUpload"
                                    name="avatar"
                                    className="hidden"
                                    onChange={handleFileChange}
                                />
                                <span className="text-sm text-gray-600">{fileName}</span>
                            </div>
                        </div>*/}
                        <div className="text-center pt-4">
                            <button
                                type="submit"
                                className="bg-[#68A2F0] text-white font-semibold px-6 py-3 rounded-full hover:bg-[#6094d7] transition duration-300 shadow-md"
                            >
                                {isEdit ? "Update Manager" : "Create New"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            {/*OTP*/}
            {/* <div className="fixed inset-0 z-70 flex items-center pl-[42vw] bg-black bg-opacity-40">
                <div class="bg-white shadow-xl rounded-2xl p-8 w-full max-w-md">
                    <h2 class="text-2xl font-bold text-center text-gray-800 mb-6">Input OTP</h2>

                    <p class="text-center text-gray-500 mb-4">Please enter the 6-digit code sent to your phone</p>

                    <div class="flex justify-center space-x-2 mb-6" id="otp-container">
                        <input type="text" maxlength="1" value="1" class="otp-input w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        <input type="text" maxlength="1" value="6" class="otp-input w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        <input type="text" maxlength="1" value="4" class="otp-input w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        <input type="text" maxlength="1" value="6" class="otp-input w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        <input type="text" maxlength="1" value="2" class="otp-input w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        <input type="text" maxlength="1" value="3" class="otp-input w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>
                    <div className="float-right italic text-red-600 py-2 text-xs">
                        *Default OTP for create through call is 164623
                    </div>
                    <button class="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition">Verify</button>
                </div>
            </div> */}
            {/* Popup */}
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
    );
};

export default FormManager;
