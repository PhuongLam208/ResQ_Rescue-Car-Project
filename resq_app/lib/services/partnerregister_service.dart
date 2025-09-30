// partner_service.dart
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';
import 'package:mime/mime.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/models/PartnerRegistrationData.dart';

Future<void> uploadPartnerData(PartnerRegistrationData data) async {
  final uri = Uri.parse('$baseUrl/partners/register-fix');

  final request = http.MultipartRequest('POST', uri);

  request.fields['userId'] = data.userId.toString();
  request.fields['resFix'] = data.resFix.toString();
  request.fields['resTow'] = data.resTow.toString();
  request.fields['resDrive'] = data.resDrive.toString();
  request.fields['licenseNumber'] = data.licenseNumber ?? '';

  for (var id in data.selectedServices ?? []) {
    request.fields['selectedServiceIds'] = id.toString();
  }

  if (data.documentFrontImagePath != null) {
    final mimeType = lookupMimeType(data.documentFrontImagePath!)?.split('/');
    final file = await http.MultipartFile.fromPath(
      'documentFront',
      data.documentFrontImagePath!,
      contentType:
          mimeType != null ? MediaType(mimeType[0], mimeType[1]) : null,
    );
    request.files.add(file);
  }

  if (data.documentBackImagePath != null) {
    final mimeType = lookupMimeType(data.documentBackImagePath!)?.split('/');
    final file = await http.MultipartFile.fromPath(
      'documentBack',
      data.documentBackImagePath!,
      contentType:
          mimeType != null ? MediaType(mimeType[0], mimeType[1]) : null,
    );
    request.files.add(file);
  }

  final response = await request.send();

  if (response.statusCode == 200) {
    print('Upload success!');
  } else {
    print('Upload failed: ${response.statusCode}');
  }
}
