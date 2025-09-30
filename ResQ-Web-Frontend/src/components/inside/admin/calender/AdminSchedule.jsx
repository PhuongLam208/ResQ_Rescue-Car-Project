import React, { useEffect, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from '@fullcalendar/interaction'; 
import listPlugin from "@fullcalendar/list";
import { formatDate } from "@fullcalendar/core";
import * as adminApi from "../../../../../admin.js";
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
    const [isRecurring, setIsRecurring] = useState(false);
    const [recurrenceType, setRecurrenceType] = useState('');
    const [recurrenceInterval, setRecurrenceInterval] = useState(0);
    const [recurrenceDays, setRecurrenceDays] = useState('');
    const [recurrenceEndDate, setRecurrenceEndDate] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [staff, setStaff] = useState([]);
    const [managers, setManagers] = useState();
    const [selectedManager, setSelectedManager] = useState("");
    const [selectedStaff, setSelectedStaff] = useState([]);
    const [eventIdToEdit, setEventIdToEdit] = useState(null);
    const [statusFilter, setStatusFilter] = useState("All");
    

    const fetchSchedule = async () => {
        try {
            
            const response = await adminApi.getAllSchedule(); 
            setSchedule(response.data);
            console.log("Fetched schedule:", response.data);
            const managerList = await adminApi.getAllManagers();
            setManagers(managerList.data);
            console.log("Fetched managers:", managerList.data);
            const staffList = await adminApi.getAllStaff();
            setStaff(staffList.data);
            console.log("Fetched staff:", staffList.data);
            
        } catch (error) {
            console.error("Error fetching schedule:", error);
        }
    }

    useEffect(() => {
        fetchSchedule();
    }, []);

    useEffect(() => {
        if (Array.isArray(schedule) && schedule.length > 0) {
            const mapped = schedule.map((e, index) => ({
                id: `${e.shiftId}_${index}`,
                title: e.title,
                start: e.startTime,
                end: e.endTime,
                backgroundColor: e.eventColor,
                extendedProps: {
                    shiftId: e.shiftId,
                    description: e.description,
                    creatorName: e.creatorName,
                    status: e.status,
                    managerId: e.managerId,
                    staffIds: e.staffIds
                    },
            }));
            setEvents(mapped);
        }
    }, [schedule]);

    const detailEvent = (info) => {
        setSelectedEvent(info.event);
        setShowModal(true);
        
    };

    const handleDateClick = (info) => {
        const clickedDate = new Date(info.dateStr);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (clickedDate < today) {
            return; 
        }

        clickedDate.setHours(8, 0, 0, 0);
        const endDate = new Date(clickedDate.getTime() + 6 * 60 * 60 * 1000); // 1 ca 6 tiếng +6 tiếng
        
        setStartTime(clickedDate.toISOString().slice(0, 16));
        setEndTime(endDate.toISOString().slice(0, 16));

        setShowForm(true);
        setIsRecurring(false); 
    }

    const CreateNewEvent = async (e) => {
        e.preventDefault();

        if (new Date(startTime) >= new Date(endTime)) {
            alert("Start time must be before end time.");
            return;
        }  

        const scheduleData = {
            title,
            description,
            startTime,
            endTime,
            eventColor,
            status: "PENDING",
            isRecurring,
            recurrenceType,
            recurrenceInterval,
            recurrenceDays,
            recurrenceEndDate,
            managerId: selectedManager,
            staffIds: selectedStaff.map(id => parseInt(id))
        };
        console.log("Schedule Data:", scheduleData);
        try {
            if (eventIdToEdit) {
            const response = await adminApi.updateSchedule(eventIdToEdit, scheduleData);
            setSchedule(response.data);
            alert("Schedule updated successfully!");
            } else {
            await adminApi.createSchedule(scheduleData);
            alert("Schedule created successfully!");
            }
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
        setIsRecurring(false);
        setRecurrenceType('');
        setRecurrenceInterval(1);
        setRecurrenceDays('');
        setRecurrenceEndDate('');
        setSelectedManager(null);
    };

    const handleDelete = async (mode) => {
        if (!selectedEvent) return;

        const shiftId = selectedEvent.extendedProps?.shiftId;
        const date = new Date(selectedEvent.start).toISOString().split("T")[0]; // yyyy-MM-dd
        console.log(date);
        console.log(selectedEvent);
        try {
            let response;

            if (mode === "single") {
                // Xoá 1 ngày cụ thể
                console.log("Deleting ID:", shiftId);
                response = await adminApi.deleteScheduleByDate(shiftId, date);
                setEvents(prev =>
                    prev.filter(ev =>
                        !(
                        ev.extendedProps?.shiftId === shiftId &&
                        new Date(ev.start).toISOString().startsWith(date)
                        )
                    )
                );

                
                alert(`Deleted schedule for ${date}`);
            } else if (mode === "all") {
                // Xoá toàn bộ lịch của shift
                console.log("Deleting ID:", shiftId);
                response = await adminApi.deleteSchedule(shiftId);
                setEvents(prev => prev.filter(ev => ev.extendedProps?.shiftId !== shiftId));
                alert("Deleted entire recurring schedule.");
            } else {
                alert("Invalid delete option.");
                return;
            }

            setSchedule(response.data);
            setShowModal(false);
        } catch (error) {
            console.error("Delete failed", error);
            alert("Failed to delete schedule.");
        }
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
                    dateClick={handleDateClick}
                    dayMaxEvents={3}
                />
            </div>

            {showForm && (
                <div className="modal d-block" tabIndex="-1" style={{ backgroundColor: "rgba(0,0,0,0.5)" }}>
                    <div className="modal-dialog modal-lg modal-dialog-centered">
                        <div className="modal-content">
                            <form onSubmit={CreateNewEvent}>
                            <div className="modal-header">
                                <h5 className="modal-title">{eventIdToEdit ? "Edit Schedule" : "Create New Schedule"}</h5>
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
                                type="datetime-local"
                                value={startTime}
                                onChange={e => setStartTime(e.target.value)}
                                className="form-control my-2"
                                required
                                />
                                <input
                                type="datetime-local"
                                value={endTime}
                                onChange={e => setEndTime(e.target.value)}
                                className="form-control my-2"
                                required
                                />
                                <input
                                type="color"
                                value={eventColor}
                                onChange={e => setEventColor(e.target.value)}
                                className="form-control form-control-color my-2"
                                />

                                <label>Select A Manager :</label>
                                <select
                                className="form-control my-2"
                                value={selectedManager}
                                onChange={(e) => {setSelectedManager(parseInt(e.target.value))}}
                                >
                                <option value="">-- Select Manager --</option>
                                {managers.map(st => (
                                    <option key={st.staffId} value={st.staffId}>
                                        {st.fullName}
                                    </option>
                                ))}
                                </select>

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
                                    {st.fullName}
                                    </option>
                                ))}
                                </select>

                                <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="recurringCheck"
                                    checked={isRecurring}
                                    onChange={e => setIsRecurring(e.target.checked)}
                                />
                                <label className="form-check-label" htmlFor="recurringCheck">Recurring?</label>
                                </div>

                                {isRecurring && (
                                <>
                                    <select
                                    value={recurrenceType}
                                    onChange={e => setRecurrenceType(e.target.value)}
                                    className="form-control my-2"
                                    >
                                    <option value="" disabled>Select Type</option>
                                    <option value="DAILY">Daily</option>
                                    <option value="WEEKLY">Weekly</option>
                                    <option value="MONTHLY">Monthly</option>
                                    </select>
                                    <label>Recurrence Interval: (e.g. every 2 days)</label>
                                    <input
                                    type="number"
                                    value={recurrenceInterval}
                                    onChange={e => setRecurrenceInterval(parseInt(e.target.value))}
                                    className="form-control my-2"
                                    placeholder="Interval (e.g. every 2 days)"
                                    />
                                    <label>Recurrence Days (for weekly recurrence):</label>
                                    <input
                                    value={recurrenceDays}
                                    onChange={e => setRecurrenceDays(e.target.value)}
                                    className="form-control my-2"
                                    placeholder="Days (e.g. MONDAY,WEDNESDAY)"
                                    />
                                    <label>Recurrence End Date:</label>
                                    <input
                                    type="datetime-local"
                                    value={recurrenceEndDate}
                                    onChange={e => setRecurrenceEndDate(e.target.value)}
                                    className="form-control my-2"
                                    placeholder="Recurrence End Date"
                                    />
                                </>
                                )}
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
                                {eventIdToEdit ? "Update Schedule" : "Create Schedule"}
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
                                    const id = selectedEvent.extendedProps?.managerId;
                                    const mgr = managers.find(m => m.staffId === id);
                                    return mgr ? mgr.fullName : `Manager ID ${id}`;
                                    })()
                                }
                            </ul>
                            <p><strong>Staff : </strong> </p>
                            <ul>
                                {selectedEvent.extendedProps?.staffIds?.map(id => {
                                    const stf = staff.find(s => s.staffId === parseInt(id));
                                    return (
                                    <li key={id}>{stf ? stf.fullName : `Staff ID ${id}`}</li>
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
                        setSelectedManager(selectedEvent.extendedProps?.managerIds || null);
                        setSelectedStaff(selectedEvent.extendedProps?.staffIds || []);
                        setEventIdToEdit(selectedEvent.extendedProps?.shiftId); 
                    }}> 
                        Edit 
                    </Button>
                    <Button variant="outline-danger" onClick={() => handleDelete("single")}>
                        Delete This Occurrence
                    </Button>
                    <Button variant="danger" onClick={() => handleDelete("all")}>
                        Delete Entire Series
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default Schedule;