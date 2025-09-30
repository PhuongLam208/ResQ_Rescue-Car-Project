class PartnerRegistrationData {
  final int userId;

  int resFix;
  int resTow;
  int resDrive;

  String? licenseNumber;
  String? licenseExpiryDate;
  String? documentFrontImagePath;
  String? documentBackImagePath;

  List<int>? selectedServices;

  // Cứu hộ kéo xe
  String? towLicenseNumber;
  String? towLicenseExpiryDate;
  String? towInspectionNumber;
  String? towInspectionExpiryDate;
  String? towSpecialPermitNumber;
  String? towSpecialPermitExpiryDate;

  String? towLicenseFrontImagePath;
  String? towLicenseBackImagePath;

  String? towInspectionFrontImagePath;
  String? towInspectionBackImagePath;

  String? towSpecialPermitFrontImagePath;
  String? towSpecialPermitBackImagePath;
  String? driveVehicleImagePath;
  String? driveLicensePlateImagePath;

  // Cứu hộ lái thay


  String? driveLicenseNumber;
  String? driveLicenseExpiryDate;
  String? driveLicenseFrontImagePath;
  String? driveLicenseBackImagePath;

  PartnerRegistrationData({
    required this.userId,
    this.resFix = 0,
    this.resTow = 0,
    this.resDrive = 0,
  });

  String getServiceType() {
    if (resFix == 2) return "ResFix";
    if (resTow == 2) return "ResTow";
    if (resDrive == 2) return "ResDrive";
    return "";
  }

  Map<String, dynamic> toJson() => {
    "userId": userId,
    "resFix": resFix,
    "resTow": resTow,
    "resDrive": resDrive,
    "licenseNumber": licenseNumber,
    "licenseExpiryDate": licenseExpiryDate,
    "documentFrontImagePath": documentFrontImagePath,
    "documentBackImagePath": documentBackImagePath,
    "selectedServices": selectedServices,
    "towLicenseNumber": towLicenseNumber,
    "towLicenseExpiryDate": towLicenseExpiryDate,
    "towLicenseFrontImagePath": towLicenseFrontImagePath,
    "towLicenseBackImagePath": towLicenseBackImagePath,
    "towInspectionNumber": towInspectionNumber,
    "towInspectionExpiryDate": towInspectionExpiryDate,
    "towInspectionFrontImagePath": towInspectionFrontImagePath,
    "towInspectionBackImagePath": towInspectionBackImagePath,
    "towSpecialPermitNumber": towSpecialPermitNumber,
    "towSpecialPermitExpiryDate": towSpecialPermitExpiryDate,
    "towSpecialPermitFrontImagePath": towSpecialPermitFrontImagePath,
    "towSpecialPermitBackImagePath": towSpecialPermitBackImagePath,
    "driveLicenseNumber": driveLicenseNumber,
    "driveLicenseExpiryDate": driveLicenseExpiryDate,
    "driveLicenseFrontImagePath": driveLicenseFrontImagePath,
    "driveLicenseBackImagePath": driveLicenseBackImagePath,
  };
}
