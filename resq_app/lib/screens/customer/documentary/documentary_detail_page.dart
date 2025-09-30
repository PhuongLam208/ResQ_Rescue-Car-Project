import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class DocumentaryDetailPage extends StatefulWidget {
  final Map<String, dynamic> document;

  const DocumentaryDetailPage({super.key, required this.document});

  @override
  State<DocumentaryDetailPage> createState() => _DocumentaryDetailPageState();
}

class _DocumentaryDetailPageState extends State<DocumentaryDetailPage> {
  int? userId = loginResponse?.userId;
  List<dynamic> vehicles = [];
  int? selectedVehicleId;

  final TextEditingController typeController = TextEditingController();
  final TextEditingController documentNoController = TextEditingController();
  final TextEditingController expirationDateController =
      TextEditingController();

  File? _frontImageFile;
  File? _backImageFile;

  String? frontImageUrl;
  String? backImageUrl;

  final ImagePicker _picker = ImagePicker();
  Map<String, String> _errors = {};
  bool isEditing = false;

  @override
  void initState() {
    super.initState();
    _loadInitialData();
  }

  void _loadInitialData() {
    final doc = widget.document;
    print(widget.document);
    typeController.text = doc['documentType'] ?? '';
    documentNoController.text = doc['documentNumber'] ?? '';

    if (doc['expiryDate'] != null) {
      try {
        final parsed = DateTime.parse(doc['expiryDate']);
        expirationDateController.text = _formatDate(parsed);
      } catch (_) {}
    }
    selectedVehicleId = doc['vehicleId'];

    String stripAdminPrefix(String url) {
      final uriParts = url.split('?');
      final path = uriParts[0].replaceFirst('/admin/documentary', '');
      final query = uriParts.length > 1 ? '?${uriParts[1]}' : '';
      return '$path$query';
    }

    final String? frontImagePath = doc['frontImageUrl'];
    final String? backImagePath = doc['backImageUrl'];

    frontImageUrl =
        frontImagePath != null
            ? '$customerUrl${stripAdminPrefix(frontImagePath)}'
            : null;

    backImageUrl =
        backImagePath != null
            ? '$customerUrl${stripAdminPrefix(backImagePath)}'
            : null;
    fetchVehicles();
  }

  Future<void> fetchVehicles() async {
    try {
      final result = await CustomerService.getCustomerVehiclesNoDocs(userId!);
      setState(() {
        vehicles = result;
        final match = vehicles.firstWhere(
          (v) => v['vehicleId'] == selectedVehicleId,
          orElse: () => vehicles.isNotEmpty ? vehicles[0] : null,
        );
        if (match != null) {
          selectedVehicleId = match['vehicleId'];
          if (typeController.text.isEmpty ||
              typeController.text.startsWith("Vehicle Registration")) {
            typeController.text =
                "Vehicle Registration - ${match['plateNo'] ?? ''}";
          }
        }
      });
    } catch (e) {
      print("Error loading vehicles: $e");
    }
  }

  String _formatDate(DateTime date) => DateFormat("dd/MM/yyyy").format(date);

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
    if (!isEditing) return;

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
          _frontImageFile = File(pickedFile.path);
        } else {
          _backImageFile = File(pickedFile.path);
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

  Future<void> _handleUpdate() async {
    final type = typeController.text.trim();
    final documentNo = documentNoController.text.trim();
    final expirationDate = _toBackendFormat(
      expirationDateController.text.trim(),
    );

    final dto = {
      "documentType": type,
      "documentNumber": documentNo,
      "expiryDate": expirationDate,
    };

    final result = await CustomerService.updateDocument(
      documentId: widget.document['documentId'],
      documentDto: dto,
      frontImage: _frontImageFile ?? frontImageUrl,
      backImage: _backImageFile ?? backImageUrl,
    );

    final bool success = result["success"] == true;
    if (success) {
      setState(() {
        _errors.clear();
        isEditing = false;
      });
      _showDialog("Update Success", "Document updated successfully!");
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
    showDialog(
      context: context,
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
        Navigator.of(context, rootNavigator: true).pop();
        Navigator.pop(context, true);
      }
    });
  }

  Widget _buildInput(
    TextEditingController controller, {
    required String fieldName,
    String? hintText,
    bool readOnly = false,
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
            readOnly: onTap != null || readOnly,
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

  Widget _buildImagePicker(int index, String? fieldName, File? imageFile) {
    String? imageUrl = index == 1 ? frontImageUrl : backImageUrl;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        GestureDetector(
          onTap: () => _pickImage(index),
          child: AspectRatio(
            aspectRatio: 1.6,
            child: Container(
              decoration: BoxDecoration(
                border: Border.all(color: Color(0xFF013171)),
                borderRadius: BorderRadius.circular(8),
              ),
              child:
                  imageFile != null
                      ? ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.file(imageFile, fit: BoxFit.cover),
                      )
                      : imageUrl != null
                      ? ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.network(imageUrl, fit: BoxFit.cover),
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
      appBar: const CommonAppBar(title: 'Document Detail'),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text("Document Type:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              typeController,
              fieldName: 'documentType',
              readOnly: true,
            ),
            const SizedBox(height: 20),
            const Text("Vehicle:", style: _labelStyle),
            const SizedBox(height: 6),
            vehicles.isEmpty
                ? const Text(
                  "Loading vehicles...",
                  style: TextStyle(color: Colors.grey),
                )
                : DropdownButtonFormField<int>(
                  value: selectedVehicleId,
                  items:
                      vehicles.map((v) {
                        return DropdownMenuItem<int>(
                          value: v['vehicleId'],
                          child: Text(v['plateNo'] ?? 'Unknown'),
                        );
                      }).toList(),
                  onChanged:
                      isEditing
                          ? (value) {
                            final selected = vehicles.firstWhere(
                              (v) => v['vehicleId'] == value,
                            );
                            setState(() {
                              selectedVehicleId = value;
                              typeController.text =
                                  "Vehicle Registration - ${selected['plateNo'] ?? ''}";
                            });
                          }
                          : null,
                  decoration: InputDecoration(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    errorText: _errors['vehicleId'],
                    filled: !isEditing,
                    fillColor: !isEditing ? Colors.grey.shade200 : null,
                  ),
                  hint: const Text("Select a vehicle"),
                ),
            const SizedBox(height: 20),
            const Text("Document Number:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              documentNoController,
              fieldName: "documentNumber",
              readOnly: !isEditing,
            ),
            const SizedBox(height: 20),
            const Text("Expiration Date:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(
              expirationDateController,
              fieldName: "expiryDate",
              hintText: "DD/MM/YYYY",
              keyboardType: TextInputType.none,
              onTap: () => _selectDate(context, expirationDateController),
              readOnly: !isEditing,
            ),
            const SizedBox(height: 20),
            const Text("Document Image:", style: _labelStyle),
            const SizedBox(height: 5),
            Row(
              children: [
                Expanded(
                  child: _buildImagePicker(1, 'frontImage', _frontImageFile),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: _buildImagePicker(2, 'backImage', _backImageFile),
                ),
              ],
            ),
            const SizedBox(height: 30),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: () async {
                    if (isEditing) {
                      await _handleUpdate();
                    } else {
                      setState(() => isEditing = true);
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Color(0xFF013171),
                  ),
                  child: Text(
                    isEditing ? "Save" : "Edit",
                    style: const TextStyle(color: Colors.white),
                  ),
                ),
                const SizedBox(width: 16),
                if (isEditing)
                  ElevatedButton(
                    onPressed: () => setState(() => isEditing = false),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Color(0xFFBB0000),
                    ),
                    child: const Text(
                      "Cancel",
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
