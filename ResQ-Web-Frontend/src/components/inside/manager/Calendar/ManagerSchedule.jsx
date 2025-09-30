import React, { useEffect, useState } from "react";
//import { useState, useEffect } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from '@fullcalendar/interaction'; 
import listPlugin from "@fullcalendar/list";
import { formatDate } from "@fullcalendar/core";
import * as managerApi from "../../../../../manager.js";
import { Modal } from "react-bootstrap";
import { Button } from 'react-bootstrap';

import bootstrap5Plugin from "@fullcalendar/bootstrap5";

const Schedule = () => {

    const [schedule, setSchedule] = useState([]);
    const [events, setEvents] = useState([]);
    const [title, setTitle] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');
    const [description, setDescription] = useState('');
    const [eventColor, setEventColor] = useState('#000000');
    const [showModal, setShowModal] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [staff, setStaff] = useState([]);
    const [selectedStaff, setSelectedStaff] = useState([]);
    const [eventIdToEdit, setEventIdToEdit] = useState(null);
    const [statusFilter, setStatusFilter] = useState("All");
    
    const fetchSchedule = async () => {
        try {
            const response = await managerApi.getAllSchedule(); 
            setSchedule(response.data);
            console.log("Fetched schedule:", response.data);
            const staffList = await managerApi.getAllStaff();
            setStaff(staffList.data);
            
        } catch (error) {
            console.error("Error fetching schedule:", error);
        } 
    }

    useEffect(() => {
        fetchSchedule();
    }, []);

    useEffect(() => {
        if (schedule && schedule.length > 0) {
            const mapped = schedule.map((e, index) => ({
                id: e.shiftId ?? index,
                title: e.title,
                start: e.startTime,
                end: e.endTime,
                backgroundColor: e.eventColor,
                extendedProps: {
                    description: e.description,
                    creatorName: e.creatorName,
                    status: e.status,
                    managerId: e.managerId,
                    staffIds: e.staffIds
                },
                
            }));
            console.table(mapped.map(e => ({ id: e.id, title: e.title })));
            setEvents(mapped);
        }
    }, [schedule]);

    const detailEvent = (info) => {
        setSelectedEvent(info.event);
        setShowModal(true);       
    };

    const UpdateEvent = async (e) => {
        e.preventDefault();

        if (new Date(startTime) >= new Date(endTime)) {
            alert("Start time must be before end time.");
            return;
        }  

        const scheduleData = {
            title,
            description,
            eventColor,
            staffIds: selectedStaff.map(id => parseInt(id))
        };

        try {
            const response = await managerApi.updateSchedule(eventIdToEdit, scheduleData);
            setSchedule(response.data);
            alert("Schedule updated successfully!");
            fetchSchedule();

            resetForm();
            setShowForm(false);
            setEventIdToEdit(null);
            
        } catch (err) {
            console.error("Error creating/updating schedule:", err);
            alert("Failed to save schedule.");
        }
    };

    const handleCloseForm = () => {
        resetForm();
        setShowForm(false);
        setEventIdToEdit(null);
    };

    const resetForm = () => {
        setTitle('');
        setDescription('');
        setStartTime('');
        setEndTime('');
        setEventColor('#000000');
        setSelectedStaff([]);
    };

    const filteredEvents = events.filter(ev =>
        statusFilter === "All" || ev.extendedProps?.status === statusFilter   
    );

    return (
        <div className="container mt-4">
            <div className="calendar-wrapper" style={{ position: "relative" }}>
                <div style={{ position: "absolute", top: 10, right: 320, zIndex: 10 }}>
                    <label htmlFor="statusFilter" style={{ marginRight: 8 }}><strong>Filter</strong></label>
                    <select 
                        id="statusFilter"
                        value={statusFilter}
                        onChange={e => setStatusFilter(e.target.value)}
                        style={{
                            minWidth: "150px",
                            padding: "5px 10px",
                            borderRadius: "6px",
                            border: "1px solid #ccc",
                            fontSize: "14px",
                            textAlign: "center", 
                        }}
                    >
                        <option value="All">All</option>
                        <option value="PENDING">PENDING</option>
                        <option value="ON SHIFT">ON SHIFT</option>
                        <option value="COMPLETED">COMPLETED</option>
                    </select>
                </div>

                <FullCalendar
                    plugins={[dayGridPlugin, timeGridPlugin, listPlugin, bootstrap5Plugin, interactionPlugin]}
                    themeSystem="bootstrap5"
                    initialView="dayGridMonth"
                    headerToolbar={{
                        left: 'prev,next today',
                        center: 'title',
                        right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
                    }}
                    events={filteredEvents}
                    eventClick={detailEvent}
                    
                    height="auto"
                    dayCellDidMount={(info) => {
                        const today = new Date();
                        const cellDate = new Date(info.date);
                        if (cellDate < today.setHours(0, 0, 0, 0)) {
                            info.el.style.pointerEvents = 'none';
                            info.el.style.opacity = '0.5';
                        } else {
                            info.el.ondblclick = () => setShowForm(true);
                        }
                    }}
                    dayMaxEvents={3}
                />
            </div>

            {showForm && (
                <div className="modal d-block" tabIndex="-1" style={{ backgroundColor: "rgba(0,0,0,0.5)" }}>
                    <div className="modal-dialog modal-lg modal-dialog-centered">
                        <div className="modal-content">
                            <form onSubmit={UpdateEvent}>
                            <div className="modal-header">
                                <h5 className="modal-title">Edit Schedule</h5>
                                <button type="button" className="btn-close" onClick={handleCloseForm}></button>
                            </div>
                            <div className="modal-body">
                                <input
                                value={title}
                                onChange={e => setTitle(e.target.value)}
                                placeholder="Title"
                                className="form-control my-2"
                                required
                                />
                                <textarea
                                value={description}
                                onChange={e => setDescription(e.target.value)}
                                placeholder="Description"
                                className="form-control my-2"
                                />
                                <input
                                type="color"
                                value={eventColor}
                                onChange={e => setEventColor(e.target.value)}
                                className="form-control form-control-color my-2"
                                />
                                <label>Select Staff:</label>
                                <select
                                multiple
                                className="form-control my-2"
                                value={selectedStaff}
                                onChange={(e) => {
                                    const options = Array.from(e.target.selectedOptions);
                                    const values = options.map(opt => opt.value);
                                    setSelectedStaff(values);
                                }}
                                >
                                {staff.map(st => (
                                    <option key={st.staffId} value={st.staffId}>
                                    {st.user.fullName}
                                    </option>
                                ))}
                                </select>
                            </div>
                            <div className="modal-footer">
                                <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={handleCloseForm}
                                >
                                Cancel
                                </button>
                                <button
                                type="submit"
                                className="btn btn-primary"
                                >
                                Update
                                </button>
                            </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            <Modal show={showModal} onHide={() => setShowModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>{selectedEvent?.title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedEvent && (
                        <div>
                            <h5><strong>Title : </strong>{selectedEvent.title}</h5>
                            <p><strong>Creator : </strong> {selectedEvent.extendedProps?.creatorName}</p>
                            <p><strong>Status : </strong> {selectedEvent.extendedProps?.status}</p>
                            <p><strong>Color : </strong> 
                            <span
                                style={{
                                backgroundColor: selectedEvent.backgroundColor,
                                width: '20px',
                                height: '20px',
                                display: 'inline-block',
                                borderRadius: '4px',
                                border: '1px solid #ccc',
                                marginLeft: '5px',
                                verticalAlign: 'middle'
                                }}>
                            </span>
                            </p>
                            <p><strong>Start : </strong> {formatDate(selectedEvent.start, { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })}</p>
                            <p><strong>End : </strong> {formatDate(selectedEvent.end, { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })}</p>
                            <p><strong>Description : </strong> {selectedEvent.extendedProps?.description}</p>
                            <p><strong>Managers : </strong> </p>
                            <ul>
                                {
                                (() => {
                                    const id = selectedEvent.extendedProps?.managerIds;
                                    const mgr = staff.find(m => m.staffId === id);
                                    return mgr ? mgr.user.fullName : `Manager ID ${id}`;
                                    })()
                                }
                            </ul>
                            <p><strong>Staff : </strong> </p>
                            <ul>
                                {selectedEvent.extendedProps?.staffIds?.map(id => {
                                    const stf = staff.find(s => s.staffId === parseInt(id));
                                    return (
                                    <li key={id}>{stf ? stf.user.fullName : `Staff ID ${id}`}</li>
                                    );
                                })}
                            </ul>
                        </div>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="warning" onClick={() => {
                        setShowModal(false);
                        setShowForm(true);
                        setTitle(selectedEvent.title);
                        setDescription(selectedEvent.extendedProps?.description || '');
                        setStartTime(new Date(selectedEvent.start).toISOString().slice(0, 16));
                        setEndTime(new Date(selectedEvent.end).toISOString().slice(0, 16));
                        setEventColor(selectedEvent.backgroundColor || '#3788d8');
                        setSelectedStaff(selectedEvent.extendedProps?.staffIds || []);
                        setEventIdToEdit(selectedEvent.id); 
                    }}> 
                        Edit 
                    </Button>
                    
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default Schedule;