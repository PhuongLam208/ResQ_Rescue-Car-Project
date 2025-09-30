import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';

import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class NewPersonalDataPage extends StatefulWidget {
  const NewPersonalDataPage({super.key});

  @override
  State<NewPersonalDataPage> createState() => _NewPersonalDataPageState();
}

class _NewPersonalDataPageState extends State<NewPersonalDataPage> {
  int? userId = loginResponse?.userId;
  late List<String> typeOptions;
  String? _selectedType;

  final TextEditingController citizenNoController = TextEditingController();
  final TextEditingController issuedPlaceController = TextEditingController();
  final TextEditingController issuedDateController = TextEditingController();
  final TextEditingController expirationDateController =
      TextEditingController();

  File? _frontImage;
  File? _backImage;
  File? _faceImage;

  final ImagePicker _picker = ImagePicker();
  Map<String, String> _errors = {};

  @override
  void initState() {
    super.initState();
    typeOptions = ["Identity Card", "Passport"];
    _selectedType = "Identity Card"; // Mặc định là "Identity Card"

    final now = DateTime.now();
    issuedDateController.text = _formatDate(now);
    expirationDateController.text = _formatDate(now);
  }

  String _formatDate(DateTime date) {
    return "${date.day.toString().padLeft(2, '0')}/"
        "${date.month.toString().padLeft(2, '0')}/"
        "${date.year}";
  }

  Future<void> _selectDateIssued(
    BuildContext context,
    TextEditingController controller,
  ) async {
    DateTime initialDate = DateTime.now();
    try {
      final parts = controller.text.split('/');
      if (parts.length == 3) {
        initialDate = DateTime(
          int.parse(parts[2]),
          int.parse(parts[1]),
          int.parse(parts[0]),
        );
      }
    } catch (_) {}

    final picked = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: DateTime(1800),
      lastDate: initialDate,
    );
    if (picked != null) {
      setState(() {
        controller.text = _formatDate(picked);
      });
    }
  }

  Future<void> _selectDateExpired(
    BuildContext context,
    TextEditingController controller,
  ) async {
    DateTime now = DateTime.now();
    DateTime tomorrow = DateTime(
      now.year,
      now.month,
      now.day,
    ).add(const Duration(days: 1));
    DateTime initialDate = tomorrow;

    try {
      final parts = controller.text.split('/');
      if (parts.length == 3) {
        DateTime parsedDate = DateTime(
          int.parse(parts[2]),
          int.parse(parts[1]),
          int.parse(parts[0]),
        );
        if (!parsedDate.isBefore(tomorrow)) {
          initialDate = parsedDate;
        }
      }
    } catch (_) {
      // ignore invalid format
    }

    final picked = await showDatePicker(
      context: context,
      initialDate: initialDate,
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
        switch (index) {
          case 1:
            _frontImage = File(pickedFile.path);
            break;
          case 2:
            _backImage = File(pickedFile.path);
            break;
          case 3:
            _faceImage = File(pickedFile.path);
            break;
        }
      });
    }
  }

  String _toBackendFormat(String input) {
    final inputFormat = DateFormat("dd/MM/yyyy");
    final outputFormat = DateFormat("yyyy-MM-dd");
    final parsedDate = inputFormat.parseStrict(input);
    return outputFormat.format(parsedDate); // "2025-07-13"
  }

  void _handleSave() async {
    print(userId);
    final citizenNo = citizenNoController.text.trim();
    final issuedPlace = issuedPlaceController.text.trim();
    final issuedDate = _toBackendFormat(issuedDateController.text.trim());
    final expirationDate = _toBackendFormat(
      expirationDateController.text.trim(),
    );

    final dto = {
      "type": _selectedType,
      "citizenNumber": citizenNo,
      "issuePlace": issuedPlace,
      "issueDate": issuedDate,
      "expirationDate": expirationDate,
    };

    final result = await CustomerService.createPersonalData(
      customerId: userId!,
      personalDataDto: dto,
      frontImage: _frontImage,
      backImage: _backImage,
      faceImage: _faceImage,
    );

    final bool success = result["success"] == true;
    if (success) {
      setState(() => _errors.clear());
      _showDialog("Add Success", "Your personal document has been added!");
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
        Navigator.of(dialogContext, rootNavigator: true).pop();
        Navigator.pop(dialogContext, true);
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Create Personal Document'),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text("Document Type:", style: _labelStyle),
            const SizedBox(height: 6),
            DropdownButtonFormField<String>(
              value: _selectedType,
              items:
                  typeOptions.map((type) {
                    return DropdownMenuItem(value: type, child: Text(type));
                  }).toList(),
              onChanged: (value) => setState(() => _selectedType = value),
              decoration: InputDecoration(
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 14,
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                errorText: _errors["type"],
              ),
            ),
            const SizedBox(height: 20),
            const Text("Citizen Number:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(citizenNoController, fieldName: "citizenNumber"),
            const SizedBox(height: 20),
            const Text("Issued Place:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(issuedPlaceController, fieldName: "issuePlace"),
            const SizedBox(height: 20),
            const Text("Issued Date:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              issuedDateController,
              fieldName: "issueDate",
              keyboardType: TextInputType.none,
              onTap: () => _selectDateIssued(context, issuedDateController),
            ),
            const SizedBox(height: 20),
            const Text("Expiration Date:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              expirationDateController,
              fieldName: "expirationDate",
              keyboardType: TextInputType.none,
              onTap:
                  () => _selectDateExpired(context, expirationDateController),
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
            const SizedBox(height: 20),
            const Text("Face Photo:", style: _labelStyle),
            const SizedBox(height: 5),
            SizedBox(
              height: 134,
              width: 180,
              child: _buildImagePicker(
                index: 3,
                fieldName: 'faceImage',
                imageFile: _faceImage,
              ),
            ),
            const SizedBox(height: 30),
            Center(
              child: ElevatedButton(
                onPressed: _handleSave,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Color(0xFF013171),
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
