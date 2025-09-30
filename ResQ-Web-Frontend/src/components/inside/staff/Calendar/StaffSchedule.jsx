import React, { useEffect, useState } from "react";
//import { useState, useEffect } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from '@fullcalendar/interaction'; 
import listPlugin from "@fullcalendar/list";
import { formatDate } from "@fullcalendar/core";
import * as staffApi from "../../../../../staff.js"; 
import { Modal } from "react-bootstrap";

import bootstrap5Plugin from "@fullcalendar/bootstrap5";

const Schedule = () => {

    const [schedule, setSchedule] = useState([]);
    const [events, setEvents] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [statusFilter, setStatusFilter] = useState("All");
    
    const fetchSchedule = async () => {
        try {
            const response = await staffApi.getAllSchedule(); 
            setSchedule(response.data);
            console.log("Fetched schedule:", response.data);           
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
                    dayMaxEvents={3}
                />
            </div>

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
                            <p><strong>Managers : </strong> {selectedEvent.extendedProps?.manager}</p>
                        </div>
                    )}
                </Modal.Body>               
            </Modal>
        </div>
    );
};

export default Schedule;