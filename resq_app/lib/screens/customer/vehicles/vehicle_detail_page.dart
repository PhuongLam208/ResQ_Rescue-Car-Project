import 'package:flutter/material.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';
import 'package:resq_app/config/app_config.dart';

class VehicleDetailPage extends StatefulWidget {
  final Map<String, dynamic> vehicle;

  const VehicleDetailPage({super.key, required this.vehicle});

  @override
  State<VehicleDetailPage> createState() => _VehicleDetailPageState();
}

class _VehicleDetailPageState extends State<VehicleDetailPage> {
  bool isEditing = false;
  bool hasUpdated = false;
  late TextEditingController plateController;
  late TextEditingController brandController;
  late TextEditingController modelController;
  late TextEditingController yearController;

  File? _frontImageFile;
  File? _backImageFile;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    final v = widget.vehicle;
    plateController = TextEditingController(text: v['plateNo'] ?? '');
    brandController = TextEditingController(text: v['brand'] ?? '');
    modelController = TextEditingController(text: v['model'] ?? '');
    yearController = TextEditingController(text: v['year']?.toString() ?? '');
  }

  @override
  void dispose() {
    plateController.dispose();
    brandController.dispose();
    modelController.dispose();
    yearController.dispose();
    super.dispose();
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
                title: const Text("Choose from gallery"),
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

  Future<void> _handleUpdate() async {
    final plate = plateController.text.trim();
    final brand = brandController.text.trim();
    final model = modelController.text.trim();
    final year = int.tryParse(yearController.text.trim()) ?? 0;
    final int vehicleId =
        int.tryParse(widget.vehicle['vehicleId'].toString()) ?? 0;
    final int userId = int.tryParse(widget.vehicle['userId'].toString()) ?? 0;

    final result = await CustomerService.updateVehicle(
      vehicleId: vehicleId,
      userId: userId,
      plateNo: plate,
      brand: brand,
      model: model,
      year: year,
      frontImage: _frontImageFile,
      backImage: _backImageFile,
    );

    if (!mounted) return;
    if (result['success'] == true) {
      setState(() {
        isEditing = false;
        hasUpdated = true;
      });
      _showDialog("Update Success", "Your vehicle is updated!");
    } else {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Error: ${result['errors']}")));
    }
  }

  @override
  Widget build(BuildContext context) {
    final v = widget.vehicle;

    String stripAdminPrefix(String url) {
      final uriParts = url.split('?');
      final path = uriParts[0].replaceFirst('/admin/vehicle', '');
      final query = uriParts.length > 1 ? '?${uriParts[1]}' : '';
      return '$path$query';
    }

    final String? frontImagePath = v['frontImage'];
    final String? backImagePath = v['backImage'];

    final String? frontImageUrl =
        frontImagePath != null
            ? '$customerUrl${stripAdminPrefix(frontImagePath)}'
            : null;
    print(frontImageUrl);
    final String? backImageUrl =
        backImagePath != null
            ? '$customerUrl${stripAdminPrefix(backImagePath)}'
            : null;

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Color(0xFF013171),
        centerTitle: true,
        title: const Text(
          "Vehicle Detail",
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white),
          onPressed: () => Navigator.pop(context, hasUpdated),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildField("Plate No:", plateController, enabled: isEditing),
            _buildField("Brand:", brandController, enabled: isEditing),
            _buildField("Model:", modelController, enabled: isEditing),
            _buildField(
              "Year:",
              yearController,
              enabled: isEditing,
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 30),
            const Text("Vehicle Image:", style: _labelStyle),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _buildImagePicker(
                    1,
                    _frontImageFile,
                    frontImageUrl,
                    "Front Image",
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: _buildImagePicker(
                    2,
                    _backImageFile,
                    backImageUrl,
                    "Back Image",
                  ),
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
                      backgroundColor: Colors.red,
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

  void _showDialog(String title, String message) {
    showDialog(
      context: context,
      builder:
          (_) => AlertDialog(
            title: Center(
              child: Text(
                title,
                style: TextStyle(
                  fontFamily: 'Raleway',
                  fontSize: 23,
                  color: Colors.green[900],
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            content: Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(fontFamily: 'Lexend', fontSize: 17),
            ),
          ),
    );

    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) {
        Navigator.of(context, rootNavigator: true).pop();
      }
    });
  }

  Widget _buildField(
    String label,
    TextEditingController controller, {
    bool enabled = false,
    TextInputType? keyboardType,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: _labelStyle),
          const SizedBox(height: 6),
          TextField(
            controller: controller,
            enabled: enabled,
            keyboardType: keyboardType,
            style: TextStyle(color: !enabled ? Colors.grey[700] : Colors.black),
            decoration: InputDecoration(
              filled: !enabled,
              fillColor: enabled ? null : Colors.grey.shade100,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 12,
                vertical: 14,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildImagePicker(
    int index,
    File? imageFile,
    String? imageUrl,
    String label,
  ) {
    return GestureDetector(
      onTap: () => isEditing ? _pickImage(index) : null,
      child: AspectRatio(
        aspectRatio: 1.6,
        child: Container(
          decoration: BoxDecoration(
            border: Border.all(color: Color(0xFF013171)!),
            borderRadius: BorderRadius.circular(8),
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child:
                imageFile != null
                    ? Image.file(imageFile, fit: BoxFit.cover)
                    : imageUrl != null
                    ? Image.network(imageUrl, fit: BoxFit.cover)
                    : Center(
                      child: Text(
                        label,
                        style: TextStyle(color: Colors.grey[600]),
                      ),
                    ),
          ),
        ),
      ),
    );
  }

  static const TextStyle _labelStyle = TextStyle(
    fontWeight: FontWeight.bold,
    color: Colors.black,
    fontSize: 16,
  );
}
