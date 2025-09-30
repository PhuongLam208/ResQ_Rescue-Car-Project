import React, { useState } from "react";
import { staffAPI } from "../../../../../admin";

const FormStaff = ({ onBack, staff, isEdit }) => {
    const [errors, setErrors] = useState({});
    const [formData, setFormData] = useState({
        fullName: isEdit && staff ? staff.fullName : '',
        username: isEdit && staff ? staff.userName : '',
        email: isEdit && staff ? staff.email : '',
        sdt: isEdit && staff ? staff.sdt : '',
        address: isEdit && staff ? staff.address : '',
        password: isEdit && staff ? '' : '2025@ResQ',
        avatar: isEdit && staff ? staff.avatar : '',
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
            let response;
            const newStaff = new FormData();
            const { avatar, ...dto } = formData;
            if (isEdit) {
                dto.userId = staff.userId;
            }
            newStaff.append("userDtoString", JSON.stringify(dto));
            if (avatar) newStaff.append("avatar", avatar);
            if (isEdit) {
                response = await staffAPI.updateStaff(staff.userId, newStaff);
                console.log(newStaff);
            } else {
                response = await staffAPI.createNew(newStaff);
            }
            if (response?.data) {
                setIsRun(true);
                setIsSuccess(true);
                if (isEdit) {
                    setMessage("Update Staff Success!");
                } else {
                    setMessage("Create New Staff Success!");
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
                    setMessage("Update Staff Fail!");
                } else {
                    setMessage("Create New Staff Fail!");
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
                    setErrors({});
                    setIsRun(false);
                    setIsSuccess(false);
                }, 3000);
            } else {
                setIsRun(true);
                setIsSuccess(false);
                if (isEdit) {
                    setMessage("Update Staff Fail!");
                } else {
                    setMessage("Create New Staff Fail!");
                    setIsSuccess(false); setTimeout(() => {
                        setIsRun(false);
                        setIsSuccess(false);
                    }, 3000);
                    console.log(error);
                }
            };
        }
    }

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
                        {isEdit ? "Edit Staff" : "Create New Staff"}
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
                            <label className={labelClass}>username</label>
                            <input
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                className={inputClass}
                            />
                            {errors?.userName && <p className="text-red-500 text-sm mt-1">{errors?.userName}</p>}
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
                                {isEdit ? "Update Staff" : "Create New"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

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

export default FormStaff;
