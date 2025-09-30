import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';

import 'package:resq_app/models/PartnerRegistrationData.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class LegalDocumentDriveScreen extends StatefulWidget {
  final PartnerRegistrationData data;
  const LegalDocumentDriveScreen({super.key, required this.data});

  @override
  State<LegalDocumentDriveScreen> createState() => _LegalDocumentDriveScreenState();
}

class _LegalDocumentDriveScreenState extends State<LegalDocumentDriveScreen> {
  final Map<String, TextEditingController> controllers = {};
  final Map<String, TextEditingController> expiryControllers = {};
  final Map<String, File?> frontImages = {};
  final Map<String, File?> backImages = {};
  final picker = ImagePicker();
  Set<String> errorFields = {};

  final List<String> documents = ["Valid Driver's License (required)"];

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
    for (var c in controllers.values) {
      c.dispose();
    }
    for (var c in expiryControllers.values) {
      c.dispose();
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

  Future<void> _pickExpiryDate(String docKey) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );

    if (picked != null) {
      expiryControllers[docKey]?.text =
          DateFormat('dd/MM/yyyy').format(picked);
    }
  }

  void _submit() {
    Set<String> newErrors = {};
    final key = documents[0];

    if (controllers[key]!.text.isEmpty) newErrors.add('$key-text');
    if (frontImages[key] == null) newErrors.add('$key-front');
    if (backImages[key] == null) newErrors.add('$key-back');

    setState(() {
      errorFields = newErrors;
    });

    if (newErrors.isNotEmpty) return;

    widget.data.driveLicenseNumber = controllers[key]?.text;
    widget.data.driveLicenseExpiryDate = expiryControllers[key]?.text;
    widget.data.driveLicenseFrontImagePath = frontImages[key]?.path;
    widget.data.driveLicenseBackImagePath = backImages[key]?.path;

    Navigator.pop(context, widget.data);
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
        title: Text(
          title,
          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
        ),
        childrenPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        children: [
          const SizedBox(height: 6),
          const Align(
            alignment: Alignment.centerLeft,
            child: Text(
              "Document Number:",
              style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
            ),
          ),
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
              errorText: errorFields.contains('$title-text') ? 'This field cannot be empty' : null,
            ),
          ),
          const SizedBox(height: 12),
          const Align(
            alignment: Alignment.centerLeft,
            child: Text(
              "Expiration Date:",
              style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
            ),
          ),
          const SizedBox(height: 6),
          TextFormField(
            controller: expiryControllers[title],
            readOnly: true,
            onTap: () => _pickExpiryDate(title),
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              hintText: "Select expiration date...",
              suffixIcon: Icon(Icons.calendar_today),
            ),
          ),
          const SizedBox(height: 12),
          const Text("Front Image:"),
          const SizedBox(height: 8),
          _imagePicker(title, isFront: true),
          const SizedBox(height: 12),
          const Text("Back Image:"),
          const SizedBox(height: 8),
          _imagePicker(title, isFront: false),
          const SizedBox(height: 12),
        ],
      ),
    );
  }

  Widget _imagePicker(String docKey, {required bool isFront}) {
    File? imageFile = isFront ? frontImages[docKey] : backImages[docKey];
    final errorKey = '$docKey-${isFront ? 'front' : 'back'}';

    return Container(
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
      child: GestureDetector(
        onTap: () => _pickImage(docKey, isFront: isFront),
        child: imageFile != null
            ? ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.file(imageFile, fit: BoxFit.cover),
              )
            : const Center(child: Text("Select image")),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Legal Document - Res Drive'),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Expanded(
              child: ListView(
                children: documents.map((doc) => _buildExpandableForm(doc)).toList(),
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
                child: const Text("Confirm", style: TextStyle(fontSize: 16, color: Colors.white)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
