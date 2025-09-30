import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:resq_app/models/PartnerRegistrationData.dart';

class LegalDocumentTowScreen extends StatefulWidget {
  final PartnerRegistrationData data;
  const LegalDocumentTowScreen({super.key, required this.data});

  @override
  State<LegalDocumentTowScreen> createState() => _LegalDocumentTowScreenState();
}

class _LegalDocumentTowScreenState extends State<LegalDocumentTowScreen> {
  final Map<String, TextEditingController> controllers = {};
  final Map<String, TextEditingController> expiryControllers = {};
  final Map<String, File?> frontImages = {};
  final Map<String, File?> backImages = {};
  Set<String> errorFields = {};

  File? driveVehicleImage;
  File? driveLicensePlateImage;

  final picker = ImagePicker();

  final List<String> documents = [
    "Driving License (Class C, FC) (required)",
    "Inspection Certificate (required)",
    "Special Operation Permit (required)",
  ];

  @override
  void initState() {
    super.initState();
    for (var doc in documents) {
      controllers[doc] = TextEditingController();
      expiryControllers[doc] = TextEditingController();
      frontImages[doc] = null;
      backImages[doc] = null;
    }
  }

  @override
  void dispose() {
    for (var controller in controllers.values) {
      controller.dispose();
    }
    for (var controller in expiryControllers.values) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _pickImage(String docKey, {required bool isFront}) async {
    final picked = await picker.pickImage(source: ImageSource.gallery);
    if (picked != null) {
      setState(() {
        if (isFront) {
          frontImages[docKey] = File(picked.path);
        } else {
          backImages[docKey] = File(picked.path);
        }
      });
    }
  }

  Future<void> _pickSingleImage({required bool isVehicle}) async {
    final picked = await picker.pickImage(source: ImageSource.gallery);
    if (picked != null) {
      setState(() {
        if (isVehicle) {
          driveVehicleImage = File(picked.path);
        } else {
          driveLicensePlateImage = File(picked.path);
        }
      });
    }
  }

  Future<void> _pickExpiryDate(String docKey) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
      locale: const Locale('en', 'US'),
    );
    if (picked != null) {
      setState(() {
        expiryControllers[docKey]?.text = DateFormat('dd/MM/yyyy').format(picked);
      });
    }
  }

  Widget _buildExpandableForm(String title) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: ExpansionTile(
        tilePadding: const EdgeInsets.symmetric(horizontal: 16),
        title: Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        childrenPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        children: [
          _buildLabeledText("Document Number:"),
          const SizedBox(height: 6),
          TextField(
            controller: controllers[title],
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderSide: BorderSide(
                  color: errorFields.contains('$title-text') ? Color(0xFFBB0000) : Colors.grey,
                ),
              ),
              hintText: 'Enter document number',
              contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
              errorText: errorFields.contains('$title-text') ? 'This field is required' : null,
            ),
          ),
          const SizedBox(height: 12),
          _buildLabeledText("Expiry Date:"),
          const SizedBox(height: 6),
          TextFormField(
            controller: expiryControllers[title],
            readOnly: true,
            onTap: () => _pickExpiryDate(title),
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              hintText: "Select expiry date...",
              suffixIcon: Icon(Icons.calendar_today),
            ),
          ),
          const SizedBox(height: 12),
          _buildLabeledText("Front Image:"),
          const SizedBox(height: 8),
          _buildImagePicker(title, isFront: true),
          const SizedBox(height: 12),
          _buildLabeledText("Back Image:"),
          const SizedBox(height: 8),
          _buildImagePicker(title, isFront: false),
          const SizedBox(height: 12),
        ],
      ),
    );
  }

  Widget _buildLabeledText(String text) {
    return Align(
      alignment: Alignment.centerLeft,
      child: Text(text, style: TextStyle(fontSize: 14, color: Colors.grey[700])),
    );
  }

  Widget _buildImagePicker(String docKey, {required bool isFront}) {
    final image = isFront ? frontImages[docKey] : backImages[docKey];
    final key = '$docKey-${isFront ? 'front' : 'back'}';

    return GestureDetector(
      onTap: () => _pickImage(docKey, isFront: isFront),
      child: Container(
        height: 150,
        width: double.infinity,
        decoration: BoxDecoration(
          color: Colors.grey[300],
          borderRadius: BorderRadius.circular(8),
          border: Border.all(
            color: errorFields.contains(key) ? Color(0xFFBB0000) : Colors.transparent,
            width: 2,
          ),
        ),
        child: image != null
            ? ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.file(image, fit: BoxFit.cover),
              )
            : const Center(child: Text("Choose image")),
      ),
    );
  }

  Widget _buildSingleImagePicker(String label, {required bool isVehicle}) {
    final image = isVehicle ? driveVehicleImage : driveLicensePlateImage;
    final errorKey = isVehicle ? 'vehicle' : 'plate';

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: TextStyle(fontSize: 14, color: Colors.grey[700])),
        const SizedBox(height: 6),
        GestureDetector(
          onTap: () => _pickSingleImage(isVehicle: isVehicle),
          child: Container(
            height: 150,
            width: double.infinity,
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: errorFields.contains(errorKey) ? Color(0xFFBB0000) : Colors.transparent,
                width: 2,
              ),
            ),
            child: image != null
                ? ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: Image.file(image, fit: BoxFit.cover),
                  )
                : const Center(child: Text("Choose image")),
          ),
        ),
      ],
    );
  }

  void _submit() {
    Set<String> newErrors = {};

    for (var doc in documents) {
      if (controllers[doc]!.text.isEmpty) newErrors.add('$doc-text');
      if (frontImages[doc] == null) newErrors.add('$doc-front');
      if (backImages[doc] == null) newErrors.add('$doc-back');
    }

    if (driveVehicleImage == null) newErrors.add('vehicle');
    if (driveLicensePlateImage == null) newErrors.add('plate');

    setState(() {
      errorFields = newErrors;
    });

    if (newErrors.isNotEmpty) return;

    widget.data.towLicenseNumber = controllers[documents[0]]?.text;
    widget.data.towLicenseExpiryDate = expiryControllers[documents[0]]?.text;
    widget.data.towLicenseFrontImagePath = frontImages[documents[0]]?.path;
    widget.data.towLicenseBackImagePath = backImages[documents[0]]?.path;

    widget.data.towInspectionNumber = controllers[documents[1]]?.text;
    widget.data.towInspectionExpiryDate = expiryControllers[documents[1]]?.text;
    widget.data.towInspectionFrontImagePath = frontImages[documents[1]]?.path;
    widget.data.towInspectionBackImagePath = backImages[documents[1]]?.path;

    widget.data.towSpecialPermitNumber = controllers[documents[2]]?.text;
    widget.data.towSpecialPermitExpiryDate = expiryControllers[documents[2]]?.text;
    widget.data.towSpecialPermitFrontImagePath = frontImages[documents[2]]?.path;
    widget.data.towSpecialPermitBackImagePath = backImages[documents[2]]?.path;

    widget.data.driveVehicleImagePath = driveVehicleImage?.path;
    widget.data.driveLicensePlateImagePath = driveLicensePlateImage?.path;

    Navigator.pop(context, widget.data);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Legal Document - Res Tow'),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Expanded(
              child: ListView(
                children: [
                  ...documents.map((doc) => _buildExpandableForm(doc)).toList(),
                  const SizedBox(height: 24),
                  const Text("Vehicle & License Plate Photos", style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(child: _buildSingleImagePicker("Vehicle Photo", isVehicle: true)),
                      const SizedBox(width: 16),
                      Expanded(child: _buildSingleImagePicker("License Plate Photo", isVehicle: false)),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Color(0xFFBB0000),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                onPressed: _submit,
                child: const Text("Submit", style: TextStyle(fontSize: 16, color: Colors.white)),
              ),
            )
          ],
        ),
      ),
    );
  }
}
