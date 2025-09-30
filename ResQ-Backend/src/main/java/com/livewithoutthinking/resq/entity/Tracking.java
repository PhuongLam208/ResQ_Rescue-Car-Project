package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;  // Sử dụng JTS Geometry library
import java.util.Date;

@Entity
@Data
@Table(name = "tracking")
public class Tracking {

    @Id
    @Column(name = "TrackingID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int trackingId; // TrackingID là INT và tự tăng

    @ManyToOne
    @JoinColumn(name = "RRID", referencedColumnName = "RRID", nullable = false)
    private RequestRescue requestRescue; // Liên kết với bảng RequestRescue

    @Column(name = "UserLocation")
//    @Type(type = "org.hibernate.spatial.GeometryType") // Ánh xạ Point sử dụng GeometryType
    private Point userLocation; // Tọa độ người dùng (POINT)

    @Column(name = "PartnerLocation")
//    @Type(type = "org.hibernate.spatial.GeometryType") // Ánh xạ Point sử dụng GeometryType
    private Point partnerLocation; // Tọa độ đối tác (POINT)

    @Column(name = "RealtimeUpdate")
    private boolean realtimeUpdate; // Cập nhật thời gian thực (BIT)

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
