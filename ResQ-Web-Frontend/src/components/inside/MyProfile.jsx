import React, { useEffect, useState } from 'react';
import axios from 'axios';

const MyProfile = () => {
  const [user, setUser] = useState(null);
  const userId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (userId) {
      axios.get(`http://localhost:9090/api/resq/users/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        if (res.data && res.data.data) {
          setUser(res.data.data);
        }
      })
      .catch((err) => {
        console.error("Failed to fetch user profile:", err);
      });
    }
  }, [userId]);

  if (!user) return <div className="p-6 text-center text-gray-500 font-lexend">Loading profile...</div>;

  return (
    <div className="max-w-3xl mx-auto p-8 bg-white shadow-xl rounded-2xl mt-10 font-lexend">
      <div className="flex flex-col items-center mb-8">
        <img
          alt="Avatar"
          className="w-32 h-32 rounded-full border-4 border-gray-200 shadow-md"
          src={
            user?.avatar
              ? `http://localhost:9090/uploads/avatar/${user.avatar}`
              : 'http://localhost:9090/uploads/avatar/partner.png'
          }
        />
        <h2 className="text-3xl font-semibold mt-4 text-gray-800">My Profile</h2>
        <p className="text-sm text-gray-500">{user.roleName || "N/A"}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-700 text-base">
        <div className="space-y-2">
          <p><span className="font-medium text-gray-600">Username:</span> {user.username}</p>
          <p><span className="font-medium text-gray-600">Full Name:</span> {user.fullName}</p>
          <p><span className="font-medium text-gray-600">Email:</span> {user.email || 'N/A'}</p>
          <p><span className="font-medium text-gray-600">Phone:</span> {user.sdt || 'N/A'}</p>
        </div>
        <div className="space-y-2">
          <p><span className="font-medium text-gray-600">Status:</span> {user.status || 'N/A'}</p>
          <p><span className="font-medium text-gray-600">Date of Birth:</span> {user.dob ? new Date(user.dob).toLocaleDateString() : 'N/A'}</p>
          <p><span className="font-medium text-gray-600">Gender:</span> {user.gender || 'N/A'}</p>
          <p><span className="font-medium text-gray-600">Address:</span> {user.address || 'N/A'}</p>
        </div>
      </div>
    </div>
  );
};

export default MyProfile;
