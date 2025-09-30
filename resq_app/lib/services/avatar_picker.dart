import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:http/http.dart' as http;
import '../../config/app_config.dart';

Future<void> changeAvatar(int userId, BuildContext context) async {
  final picker = ImagePicker();

  await Permission.photos.request();
  await Permission.camera.request();

  final pickedFile = await picker.pickImage(source: ImageSource.gallery);
  if (pickedFile == null) return;

  var request = http.MultipartRequest(
    'POST',
    Uri.parse('$customerUrl/$userId/avatar'),
  );
  request.files.add(await http.MultipartFile.fromPath('file', pickedFile.path));
  var response = await request.send();

  if (response.statusCode == 200) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Avatar updated successfully')),
    );
  } else {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Failed to upload avatar')),
    );
  }
}
