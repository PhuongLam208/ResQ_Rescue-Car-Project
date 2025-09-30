import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class NewDocumentaryPage extends StatefulWidget {
  const NewDocumentaryPage({super.key});

  @override
  State<NewDocumentaryPage> createState() => _NewDocumentaryPageState();
}

class _NewDocumentaryPageState extends State<NewDocumentaryPage> {
  int? userId = loginResponse?.userId;
  List<dynamic> vehicles = [];
  int? selectedVehicleId;

  final TextEditingController typeController = TextEditingController();
  final TextEditingController documentNoController = TextEditingController();
  final TextEditingController expirationDateController =
      TextEditingController();

  File? _frontImage;
  File? _backImage;

  final ImagePicker _picker = ImagePicker();
  Map<String, String> _errors = {};

  @override
  void initState() {
    super.initState();
    expirationDateController.text = _formatDate(DateTime.now());
    fetchVehicles();
  }

  Future<void> fetchVehicles() async {
    try {
      final result = await CustomerService.getCustomerVehiclesNoDocs(userId!);
      setState(() {
        vehicles = result;
        if (vehicles.isNotEmpty && vehicles[0]['vehicleId'] != null) {
          selectedVehicleId = vehicles[0]['vehicleId'];
          typeController.text =
              "Vehicle Registration - ${vehicles[0]['plateNo'] ?? ''}";
        } else {
          typeController.text = "Vehicle Registration";
        }
      });
    } catch (e) {
      print("Error loading vehicles: $e");
    }
  }

  String _formatDate(DateTime date) {
    return DateFormat("dd/MM/yyyy").format(date);
  }

  Future<void> _selectDate(
    BuildContext context,
    TextEditingController controller,
  ) async {
    DateTime now = DateTime.now();
    DateTime tomorrow = DateTime(
      now.year,
      now.month,
      now.day,
    ).add(const Duration(days: 1));

    try {
      final parts = controller.text.split('/');
      if (parts.length == 3) {
        DateTime parsedDate = DateTime(
          int.parse(parts[2]),
          int.parse(parts[1]),
          int.parse(parts[0]),
        );
      }
    } catch (_) {
      // ignore invalid format
    }

    final picked = await showDatePicker(
      context: context,
      initialDate: tomorrow,
      firstDate: tomorrow,
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        controller.text = _formatDate(picked);
      });
    }
  }

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
          _frontImage = File(pickedFile.path);
        } else {
          _backImage = File(pickedFile.path);
        }
      });
    }
  }

  String _toBackendFormat(String input) {
    final inputFormat = DateFormat("dd/MM/yyyy");
    final outputFormat = DateFormat("yyyy-MM-dd");
    final parsedDate = inputFormat.parseStrict(input);
    return outputFormat.format(parsedDate);
  }

  void _handleSave() async {
    final type = typeController.text.trim();
    final documentNo = documentNoController.text.trim();
    final expirationDate = _toBackendFormat(
      expirationDateController.text.trim(),
    );

    final dto = {
      "documentType": type,
      "documentNumber": documentNo,
      "expiryDate": expirationDate,
      "vehicleId": selectedVehicleId,
    };

    final result = await CustomerService.createDocument(
      customerId: userId!,
      documentDto: dto,
      frontImage: _frontImage,
      backImage: _backImage,
    );

    final bool success = result["success"] == true;
    if (success) {
      setState(() => _errors.clear());
      _showDialog("Add Success", "Your document has been added!");
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
    String? hintText,
    TextInputType keyboardType = TextInputType.text,
    List<TextInputFormatter>? inputFormatters,
    VoidCallback? onTap,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          height: 56,
          child: TextField(
            controller: controller,
            readOnly: onTap != null,
            onTap: onTap,
            keyboardType: keyboardType,
            inputFormatters: inputFormatters,
            decoration: InputDecoration(
              hintText: hintText,
              contentPadding: EdgeInsets.symmetric(
                vertical: 12.0,
                horizontal: 10.0,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              errorText: null,
            ),
          ),
        ),
        if (_errors[fieldName] != null)
          Padding(
            padding: const EdgeInsets.only(top: 4.0, left: 8.0),
            child: Text(
              _errors[fieldName]!,
              style: TextStyle(color: Color(0xFFBB0000), fontSize: 12),
            ),
          ),
        const SizedBox(height: 4),
      ],
    );
  }

  Widget _buildImagePicker({
    required int index,
    required String fieldName,
    File? imageFile,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        GestureDetector(
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
        ),
        if (_errors[fieldName] != null)
          Padding(
            padding: const EdgeInsets.only(top: 4, left: 4),
            child: Text(
              _errors[fieldName]!,
              style: const TextStyle(color: Color(0xFFBB0000), fontSize: 12),
            ),
          ),
      ],
    );
  }

  static const TextStyle _labelStyle = TextStyle(
    fontFamily: "Raleway",
    fontWeight: FontWeight.bold,
    fontSize: 16,
  );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'New Document'),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text("Document Type:", style: _labelStyle),
            const SizedBox(height: 6),
            TextField(
              controller: typeController,
              readOnly: true,
              style: const TextStyle(
                color: Colors.black87,
                fontWeight: FontWeight.w500,
              ),
              decoration: InputDecoration(
                filled: true,
                fillColor: Colors.grey.shade200,
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                errorText: _errors['documentType'],
              ),
            ),
            const SizedBox(height: 20),
            const Text("Vehicle:", style: _labelStyle),
            const SizedBox(height: 6),
            vehicles.isEmpty
                ? const Text("No vehicle", style: TextStyle(color: Colors.grey))
                : DropdownButtonFormField<int>(
                  value:
                      vehicles.any((v) => v['vehicleId'] == selectedVehicleId)
                          ? selectedVehicleId
                          : null,
                  items:
                      vehicles.map((v) {
                        return DropdownMenuItem<int>(
                          value: v['vehicleId'],
                          child: Text(v['plateNo'] ?? 'Unknown'),
                        );
                      }).toList(),
                  onChanged: (value) {
                    final selected = vehicles.firstWhere(
                      (v) => v['vehicleId'] == value,
                    );
                    setState(() {
                      selectedVehicleId = value;
                      typeController.text =
                          "Vehicle Registration - ${selected['plateNo'] ?? ''}";
                    });
                  },
                  decoration: InputDecoration(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    errorText: _errors['vehicleId'],
                  ),
                  hint: const Text("Select a vehicle"),
                ),
            const SizedBox(height: 20),
            const Text("Document Number:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(documentNoController, fieldName: "documentNumber"),
            const SizedBox(height: 20),
            const Text("Expiration Date:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              expirationDateController,
              fieldName: "expiryDate",
              hintText: "DD/MM/YYYY",
              keyboardType: TextInputType.none,
              onTap: () => _selectDate(context, expirationDateController),
            ),
            const SizedBox(height: 20),
            const Text("Document Image:", style: _labelStyle),
            const SizedBox(height: 5),
            Row(
              children: [
                Expanded(
                  child: _buildImagePicker(
                    index: 1,
                    fieldName: 'frontImage',
                    imageFile: _frontImage,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: _buildImagePicker(
                    index: 2,
                    fieldName: 'backImage',
                    imageFile: _backImage,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 30),
            Center(
              child: ElevatedButton(
                onPressed: vehicles.isEmpty ? null : _handleSave,
                style: ElevatedButton.styleFrom(
                  backgroundColor:
                      vehicles.isEmpty
                          ? Colors.grey.shade400
                          : Color(0xFF013171),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 40,
                    vertical: 12,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: Text(
                  "Save",
                  style: TextStyle(
                    fontSize: 17,
                    color:
                        vehicles.isEmpty ? Colors.grey.shade600 : Colors.white,
                    fontFamily: 'Lexend',
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
