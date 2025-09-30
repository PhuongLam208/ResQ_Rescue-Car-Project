import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:intl/intl.dart';
import 'package:resq_app/screens/partner/pending_approval_screen.dart';
import 'package:resq_app/services/partnerregister_service.dart';
import 'package:resq_app/models/PartnerRegistrationData.dart';
import 'package:http_parser/http_parser.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'LegalDocumentDriveScreen.dart';
import 'LegalDocumentFixScreen.dart';
import 'LegalDocumentTowScreen.dart';
import 'SelectServiceScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PartnerChecklistScreen extends StatefulWidget {
  final PartnerRegistrationData data;

  const PartnerChecklistScreen({super.key, required this.data});

  @override
  State<PartnerChecklistScreen> createState() => _PartnerChecklistScreenState();
}

class _PartnerChecklistScreenState extends State<PartnerChecklistScreen> {
  bool isSubmitting = false;

  Future<void> uploadPartnerData(PartnerRegistrationData data) async {
    var uri = Uri.parse('$partnerUrl/register');
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('token');

    final headers = {'Authorization': 'Bearer $token'};

    var request = http.MultipartRequest('POST', uri);
    request.headers.addAll(headers);

    request.fields['userId'] = data.userId.toString();
    request.fields['resFix'] = data.resFix.toString();
    request.fields['resTow'] = data.resTow.toString();
    request.fields['resDrive'] = data.resDrive.toString();

    if (data.selectedServices != null) {
      for (var id in data.selectedServices!) {
        request.fields['selectedServiceIds[]'] = id.toString();
      }
    }

    Future<void> addFileIfExists(String field, String? path) async {
      if (path != null) {
        request.files.add(
          await http.MultipartFile.fromPath(
            field,
            path,
            contentType: MediaType('image', 'jpeg'),
          ),
        );
      }
    }

    if (data.resFix == 2) {
      if (data.licenseNumber != null)
        request.fields['licenseNumber'] = data.licenseNumber!;
      if (data.licenseExpiryDate != null) {
        try {
          request.fields['licenseExpiryDate'] = DateFormat(
            'yyyy-MM-dd',
          ).format(DateFormat('dd/MM/yyyy').parse(data.licenseExpiryDate!));
        } catch (e) {
          print("Error licenseExpiryDate: $e");
        }
      }
      await addFileIfExists('documentFront', data.documentFrontImagePath);
      await addFileIfExists('documentBack', data.documentBackImagePath);
    }

    if (data.resTow == 2) {
      if (data.towLicenseNumber != null)
        request.fields['towLicenseNumber'] = data.towLicenseNumber!;
      if (data.towLicenseExpiryDate != null) {
        try {
          request.fields['towLicenseExpiryDate'] = DateFormat(
            'yyyy-MM-dd',
          ).format(DateFormat('dd/MM/yyyy').parse(data.towLicenseExpiryDate!));
        } catch (e) {
          print("Error towLicenseExpiryDate: $e");
        }
      }
      if (data.towInspectionExpiryDate != null) {
        try {
          request.fields['towInspectionExpiryDate'] = DateFormat(
            'yyyy-MM-dd',
          ).format(
            DateFormat('dd/MM/yyyy').parse(data.towInspectionExpiryDate!),
          );
        } catch (e) {
          print("Error towInspectionExpiryDate: $e");
        }
      }
      if (data.towSpecialPermitExpiryDate != null) {
        try {
          request.fields['towSpecialPermitExpiryDate'] = DateFormat(
            'yyyy-MM-dd',
          ).format(
            DateFormat('dd/MM/yyyy').parse(data.towSpecialPermitExpiryDate!),
          );
        } catch (e) {
          print("Error towSpecialPermitExpiryDate: $e");
        }
      }

      if (data.towInspectionNumber != null)
        request.fields['towInspectionNumber'] = data.towInspectionNumber!;
      if (data.towSpecialPermitNumber != null)
        request.fields['towSpecialPermitNumber'] = data.towSpecialPermitNumber!;

      await addFileIfExists('towLicenseFront', data.towLicenseFrontImagePath);
      await addFileIfExists('towLicenseBack', data.towLicenseBackImagePath);
      await addFileIfExists(
        'towInspectionFront',
        data.towInspectionFrontImagePath,
      );
      await addFileIfExists(
        'towInspectionBack',
        data.towInspectionBackImagePath,
      );
      await addFileIfExists(
        'towSpecialPermitFront',
        data.towSpecialPermitFrontImagePath,
      );
      await addFileIfExists(
        'towSpecialPermitBack',
        data.towSpecialPermitBackImagePath,
      );
      await addFileIfExists('driveVehicleImage', data.driveVehicleImagePath);
      await addFileIfExists(
        'driveLicensePlateImage',
        data.driveLicensePlateImagePath,
      );
    }

    if (data.resDrive == 2) {
      if (data.driveLicenseNumber != null)
        request.fields['driveLicenseNumber'] = data.driveLicenseNumber!;
      if (data.driveLicenseExpiryDate != null) {
        try {
          request.fields['driveLicenseExpiryDate'] = DateFormat(
            'yyyy-MM-dd',
          ).format(
            DateFormat('dd/MM/yyyy').parse(data.driveLicenseExpiryDate!),
          );
        } catch (e) {
          print("Error driveLicenseExpiryDate: $e");
        }
      }
      await addFileIfExists(
        'driveLicenseFront',
        data.driveLicenseFrontImagePath,
      );
      await addFileIfExists('driveLicenseBack', data.driveLicenseBackImagePath);
    }

    var response = await request.send();
    if (response.statusCode == 200) {
      print("✅ Successfully sent partner data!");
    } else {
      print("❌ Error : ${response.statusCode}");
      final responseBody = await response.stream.bytesToString();
      print("❌ Response body: $responseBody");
    }
  }
  // Hàm gửi dữ liệu lên server
  // void _submitRegistration(BuildContext context) async {
  //   final response = await http.post(
  //     Uri.parse('http://localhost:9090/api/partners/register'),
  //     headers: {'Content-Type': 'application/json'},
  //     body: json.encode(widget.data.toJson()),
  //   );
  //
  //   if (response.statusCode == 200 || response.statusCode == 201) {
  //     ScaffoldMessenger.of(context).showSnackBar(
  //       const SnackBar(content: Text('Đăng ký đối tác thành công')),
  //     );
  //     Navigator.popUntil(context, (route) => route.isFirst);
  //   } else {
  //     ScaffoldMessenger.of(context).showSnackBar(
  //       SnackBar(content: Text('Lỗi đăng ký: ${response.statusCode}')),
  //     );
  //   }
  // }

  bool _isLegalDocumentFixCompleted() {
    final d = widget.data;
    return d.licenseNumber?.isNotEmpty == true &&
        d.licenseExpiryDate?.isNotEmpty == true &&
        d.documentFrontImagePath != null &&
        d.documentBackImagePath != null;
  }

  bool _isLegalDocumentTowCompleted() {
    final d = widget.data;
    return d.towLicenseNumber?.isNotEmpty == true &&
        d.towLicenseExpiryDate?.isNotEmpty == true && // ✅
        d.towLicenseFrontImagePath != null &&
        d.towLicenseBackImagePath != null &&
        d.towInspectionNumber?.isNotEmpty == true &&
        d.towInspectionExpiryDate?.isNotEmpty == true && // ✅
        d.towInspectionFrontImagePath != null &&
        d.towInspectionBackImagePath != null &&
        d.towSpecialPermitNumber?.isNotEmpty == true &&
        d.towSpecialPermitExpiryDate?.isNotEmpty == true && // ✅
        d.towSpecialPermitFrontImagePath != null &&
        d.towSpecialPermitBackImagePath != null &&
        d.driveVehicleImagePath != null &&
        d.driveLicensePlateImagePath != null;
  }

  bool _isLegalDocumentDriveCompleted() {
    final d = widget.data;
    return d.driveLicenseNumber?.isNotEmpty == true &&
        d.driveLicenseExpiryDate?.isNotEmpty == true && // ✅
        d.driveLicenseFrontImagePath != null &&
        d.driveLicenseBackImagePath != null;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Partner Checklist'),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              IconButton(
                icon: const Icon(Icons.arrow_back, color: Color(0xFF013171)),
                onPressed: () => Navigator.pop(context),
              ),
              const SizedBox(height: 20),
              const Text(
                "Please complete the following steps\nto finish your registration review",
                style: TextStyle(fontSize: 18),
              ),
              const SizedBox(height: 32),

              const SizedBox(height: 16),
              GestureDetector(
                onTap: () async {
                  dynamic updatedData;

                  if (widget.data.resFix == 2) {
                    updatedData = await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder:
                            (_) => LegalDocumentFixScreen(data: widget.data),
                      ),
                    );
                  } else if (widget.data.resTow == 2) {
                    updatedData = await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder:
                            (_) => LegalDocumentTowScreen(data: widget.data),
                      ),
                    );
                  } else if (widget.data.resDrive == 2) {
                    updatedData = await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder:
                            (_) => LegalDocumentDriveScreen(data: widget.data),
                      ),
                    );
                  }

                  if (updatedData != null) {
                    setState(() {
                      // Giấy tờ FIX
                      widget.data.licenseNumber = updatedData.licenseNumber;
                      widget.data.licenseExpiryDate =
                          updatedData.licenseExpiryDate; // ✅

                      widget.data.documentFrontImagePath =
                          updatedData.documentFrontImagePath;
                      widget.data.documentBackImagePath =
                          updatedData.documentBackImagePath;

                      // Giấy tờ TOW
                      widget.data.towLicenseNumber =
                          updatedData.towLicenseNumber;
                      widget.data.towLicenseExpiryDate =
                          updatedData.towLicenseExpiryDate; // ✅
                      widget.data.towLicenseFrontImagePath =
                          updatedData.towLicenseFrontImagePath;
                      widget.data.towLicenseBackImagePath =
                          updatedData.towLicenseBackImagePath;

                      widget.data.towInspectionNumber =
                          updatedData.towInspectionNumber;
                      widget.data.towInspectionExpiryDate =
                          updatedData.towInspectionExpiryDate; // ✅
                      widget.data.towInspectionFrontImagePath =
                          updatedData.towInspectionFrontImagePath;
                      widget.data.towInspectionBackImagePath =
                          updatedData.towInspectionBackImagePath;

                      widget.data.towSpecialPermitNumber =
                          updatedData.towSpecialPermitNumber;
                      widget.data.towSpecialPermitExpiryDate =
                          updatedData.towSpecialPermitExpiryDate; // ✅
                      widget.data.towSpecialPermitFrontImagePath =
                          updatedData.towSpecialPermitFrontImagePath;
                      widget.data.towSpecialPermitBackImagePath =
                          updatedData.towSpecialPermitBackImagePath;

                      // Hình ảnh xe + biển số TOW
                      widget.data.driveVehicleImagePath =
                          updatedData.driveVehicleImagePath;
                      widget.data.driveLicensePlateImagePath =
                          updatedData.driveLicensePlateImagePath;

                      // Giấy tờ DRIVE
                      widget.data.driveLicenseNumber =
                          updatedData.driveLicenseNumber;
                      widget.data.driveLicenseExpiryDate =
                          updatedData.driveLicenseExpiryDate; // ✅
                      widget.data.driveLicenseFrontImagePath =
                          updatedData.driveLicenseFrontImagePath;
                      widget.data.driveLicenseBackImagePath =
                          updatedData.driveLicenseBackImagePath;
                    });
                  }
                },
                child: _checklistItem(
                  "Legal Documents",
                  widget.data.resTow == 2
                      ? _isLegalDocumentTowCompleted()
                      : widget.data.resDrive == 2
                      ? _isLegalDocumentDriveCompleted()
                      : _isLegalDocumentFixCompleted(),
                ),
              ),

              const SizedBox(height: 16),
              if (widget.data.resTow != 2)
                GestureDetector(
                  onTap: () async {
                    final updatedData = await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => SelectServiceScreen(data: widget.data),
                      ),
                    );

                    if (updatedData != null) {
                      setState(() {
                        widget.data.selectedServices =
                            updatedData.selectedServices;
                      });
                    }
                  },
                  child: _checklistItem(
                    "Service Type",
                    widget.data.selectedServices != null &&
                        widget.data.selectedServices!.isNotEmpty,
                  ),
                ),

              const Spacer(),

              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Color(0xFFBB0000),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  onPressed: () async {
                    if (isSubmitting) return;
                    setState(() => isSubmitting = true);
                    bool isValid = true;
                    String errorMessage = "";

                    if (widget.data.resFix == 2) {
                      if (!_isLegalDocumentFixCompleted()) {
                        isValid = false;
                        errorMessage =
                            "❌ You haven't completed legal documents for On-site Rescue.";
                      } else if (widget.data.selectedServices == null ||
                          widget.data.selectedServices!.isEmpty) {
                        isValid = false;
                        errorMessage =
                            "❌ You haven't selected service types for On-site Rescue.";
                      }
                    }

                    if (widget.data.resTow == 2) {
                      if (!_isLegalDocumentTowCompleted()) {
                        isValid = false;
                        errorMessage =
                            "❌ You haven't completed legal documents for Towing Rescue.";
                      }
                    }

                    if (widget.data.resDrive == 2) {
                      if (!_isLegalDocumentDriveCompleted()) {
                        isValid = false;
                        errorMessage =
                            "❌ You haven't completed legal documents for Driving Rescue.";
                      }
                      // Không kiểm tra dịch vụ cho resDrive
                    }

                    if (!isValid) {
                      setState(() => isSubmitting = false);
                      ScaffoldMessenger.of(
                        context,
                      ).showSnackBar(SnackBar(content: Text(errorMessage)));
                      return;
                    }

                    // ✅ Gửi dữ liệu nếu hợp lệ
                    await uploadPartnerData(widget.data);

                    if (mounted) {
                      setState(
                        () => isSubmitting = false,
                      ); // reset lại trạng thái
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(
                          builder:
                              (_) => PendingApprovalScreen(
                                userId: widget.data.userId,
                              ),
                        ),
                      );
                    }
                  },

                  child: const Text("Submit", style: TextStyle(fontSize: 16)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _checklistItem(String title, bool completed) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: const [
          BoxShadow(color: Colors.black12, blurRadius: 4, offset: Offset(0, 2)),
        ],
      ),
      child: Row(
        children: [
          Expanded(child: Text(title, style: const TextStyle(fontSize: 16))),
          Icon(
            completed ? Icons.check_circle : Icons.cancel,
            color: completed ? Colors.green : Color(0xFFBB0000),
          ),
        ],
      ),
    );
  }
}
