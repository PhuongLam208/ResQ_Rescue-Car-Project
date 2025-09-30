import 'package:resq_app/models/bill.dart';
import 'package:resq_app/models/partner.dart';
import 'package:resq_app/models/service_request.dart';

class RescueRequest {
  final int? rrid;
  final int userId;
  final String rescueType;
  final String uLocation;
  final String destination;
  final String? description;
  final String? note;
  final String? discountCode;
  final List<ServiceRequest> services;
  final String paymentMethod;
  final double userLatitude;
  final double userLongitude;
  final double destLatitude;
  final double destLongitude;
  final String? status;
  final DateTime? startTime;
  final DateTime? endTime;
  final DateTime? createdAt;
  final Bill? bill;
  final Partner? partner;

  RescueRequest({
    this.rrid,
    required this.userId,
    required this.rescueType,
    required this.uLocation,
    required this.destination,
    this.description,
    this.note,
    this.discountCode,
    required this.services,
    required this.paymentMethod,
    required this.userLatitude,
    required this.userLongitude,
    required this.destLatitude,
    required this.destLongitude,
    this.status,
    this.startTime,
    this.endTime,
    this.createdAt,
    this.bill,
    this.partner,
  });

  factory RescueRequest.fromJson(Map<String, dynamic> json) {
    return RescueRequest(
      rrid: json['rrid'],
      userId: json['userId'],
      rescueType: json['rescueType'],
      uLocation: json['uLocation'],
      destination: json['destination'],
      description: json['description'],
      note: json['note'],
      discountCode: json['discountCode'],
      services:
          (json['services'] as List)
              .map((service) => ServiceRequest.fromJson(service))
              .toList(),
      paymentMethod: json['paymentMethod'],
      userLatitude: json['userLatitude'].toDouble(),
      userLongitude: json['userLongitude'].toDouble(),
      destLatitude: json['destLatitude'].toDouble(),
      destLongitude: json['destLongitude'].toDouble(),
      status: json['status'],
      startTime:
          json['startTime'] != null ? DateTime.parse(json['startTime']) : null,
      endTime: json['endTime'] != null ? DateTime.parse(json['endTime']) : null,
      createdAt:
          json['createdAt'] != null ? DateTime.parse(json['createdAt']) : null,
      bill: json['bill'] != null ? Bill.fromJson(json['bill']) : null,
      partner:
          json['partner'] != null ? Partner.fromJson(json['partner']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'rrid': rrid,
      'userId': userId,
      'rescueType': rescueType,
      'uLocation': uLocation,
      'destination': destination,
      'description': description,
      'note': note,
      'discountCode': discountCode,
      'services': services.map((service) => service.toJson()).toList(),
      'paymentMethod': paymentMethod,
      'userLatitude': userLatitude,
      'userLongitude': userLongitude,
      'destLatitude': destLatitude,
      'destLongitude': destLongitude,
      'status': status,
      'startTime': startTime?.toIso8601String(),
      'endTime': endTime?.toIso8601String(),
      'createdAt': createdAt?.toIso8601String(),
      'bill': bill?.toJson(),
      'partner': partner?.toJson(),
    };
  }
}
