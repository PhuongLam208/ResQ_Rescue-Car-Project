import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';

class NewVehiclePage extends StatefulWidget {
  const NewVehiclePage({super.key});

  @override
  State<NewVehiclePage> createState() => _NewVehiclePageState();
}

class _NewVehiclePageState extends State<NewVehiclePage> {
  int? userId = loginResponse?.userId;
  final TextEditingController plateController = TextEditingController();
  final TextEditingController brandController = TextEditingController();
  final TextEditingController modelController = TextEditingController();
  final TextEditingController yearController = TextEditingController();

  File? _image1;
  File? _image2;
  final ImagePicker _picker = ImagePicker();
  Map<String, String> _errors = {};

  Future<void> _pickImage(int index) async {
    showModalBottomSheet(
      context: context,
      builder: (_) {
        return SafeArea(
          child: Wrap(
            children: [
              ListTile(
                leading: const Icon(Icons.camera_alt),
                title: const Text("Take a photo"),
                onTap: () async {
                  Navigator.pop(context);
                  final pickedFile = await _picker.pickImage(
                    source: ImageSource.camera,
                  );
                  _setPickedFile(index, pickedFile);
                },
              ),
              ListTile(
                leading: const Icon(Icons.photo_library),
                title: const Text("Choose from album"),
                onTap: () async {
                  Navigator.pop(context);
                  final pickedFile = await _picker.pickImage(
                    source: ImageSource.gallery,
                  );
                  _setPickedFile(index, pickedFile);
                },
              ),
            ],
          ),
        );
      },
    );
  }

  void _setPickedFile(int index, XFile? pickedFile) {
    if (pickedFile != null) {
      setState(() {
        if (index == 1) {
          _image1 = File(pickedFile.path);
        } else {
          _image2 = File(pickedFile.path);
        }
      });
    }
  }

  void _handleSave() async {
    final plate = plateController.text.trim();
    final brand = brandController.text.trim();
    final model = modelController.text.trim();
    final year = int.tryParse(yearController.text.trim());

    final result = await CustomerService.createVehicle(
      customerId: userId!,
      plateNo: plate,
      brand: brand,
      model: model,
      year: year ?? 0,
      frontImage: _image1,
      backImage: _image2,
    );

    final bool success = result["success"] == true;

    if (success) {
      setState(() {
        _errors.clear();
      });
      _showDialog("Add New Success", "Your vehicle is added!");
    } else {
      final dynamic errors = result["errors"];
      setState(() {
        _errors =
            (errors is Map)
                ? errors.map((k, v) => MapEntry(k.toString(), v.toString()))
                : {};
      });
    }
  }

  void _showDialog(String title, String message) {
    final dialogContext = context; // giữ lại đúng context gốc

    showDialog(
      context: dialogContext,
      builder:
          (_) => AlertDialog(
            title: Center(
              child: Text(
                title,
                style: TextStyle(
                  color: Colors.green[900],
                  fontWeight: FontWeight.bold,
                  fontSize: 23,
                ),
              ),
            ),
            content: Text(message),
          ),
    );

    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        Navigator.of(dialogContext, rootNavigator: true).pop(); // đóng dialog
        Navigator.pop(dialogContext, true); // đóng màn và trả result
      }
    });
  }

  Widget _buildInput(
    TextEditingController controller, {
    required String fieldName,
    TextInputType keyboardType = TextInputType.text,
    List<TextInputFormatter>? inputFormatters,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          height: 56,
          child: TextField(
            controller: controller,
            keyboardType: keyboardType,
            inputFormatters: inputFormatters,
            decoration: InputDecoration(
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              errorText: _errors[fieldName],
            ),
          ),
        ),
        const SizedBox(height: 4),
      ],
    );
  }

  Widget _buildImagePicker({required int index, File? imageFile}) {
    return GestureDetector(
      onTap: () => _pickImage(index),
      child: AspectRatio(
        aspectRatio: 1.6,
        child: Container(
          decoration: BoxDecoration(
            border: Border.all(color: Color(0xFF013171)!),
            borderRadius: BorderRadius.circular(8),
          ),
          child:
              imageFile != null
                  ? ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: Image.file(imageFile, fit: BoxFit.cover),
                  )
                  : Center(
                    child: Icon(
                      Icons.add_circle_outline,
                      color: Color(0xFF013171),
                      size: 40,
                    ),
                  ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.blue[900],
        centerTitle: true,
        title: const Text(
          "New Vehicle",
          style: TextStyle(
            fontFamily: 'Raleway',
            fontWeight: FontWeight.w700,
            color: Colors.white,
            fontSize: 24,
          ),
        ),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text("Plate No:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(plateController, fieldName: "plateNo"),

            const SizedBox(height: 20),
            const Text("Brand:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(brandController, fieldName: "brand"),

            const SizedBox(height: 20),
            const Text("Model:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(modelController, fieldName: "model"),

            const SizedBox(height: 20),
            const Text("Year:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              yearController,
              fieldName: "year",
              keyboardType: TextInputType.number,
              inputFormatters: [
                FilteringTextInputFormatter.digitsOnly,
                LengthLimitingTextInputFormatter(4),
              ],
            ),

            const SizedBox(height: 30),
            const Text("Vehicle Image :", style: _labelStyle),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildImagePicker(index: 1, imageFile: _image1),
                      if (_errors["frontImage"] != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 4.0),
                          child: Text(
                            _errors["frontImage"]!,
                            style: const TextStyle(
                              color: Colors.red,
                              fontSize: 12,
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildImagePicker(index: 2, imageFile: _image2),
                      if (_errors["backImage"] != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 4.0),
                          child: Text(
                            _errors["backImage"]!,
                            style: const TextStyle(
                              color: Colors.red,
                              fontSize: 12,
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 30),
            Center(
              child: ElevatedButton(
                onPressed: _handleSave,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue[900],
                  padding: const EdgeInsets.symmetric(
                    horizontal: 40,
                    vertical: 12,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: const Text(
                  "Save",
                  style: TextStyle(
                    fontFamily: 'Lexend',
                    fontSize: 17,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  static const TextStyle _labelStyle = TextStyle(
    fontFamily: "Raleway",
    fontWeight: FontWeight.bold,
    fontSize: 16,
  );
}
