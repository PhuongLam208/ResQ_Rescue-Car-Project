import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:resq_app/models/PartnerRegistrationData.dart';

class LegalDocumentFixScreen extends StatefulWidget {
  final PartnerRegistrationData data;

  const LegalDocumentFixScreen({super.key, required this.data});

  @override
  State<LegalDocumentFixScreen> createState() => _LegalDocumentFixScreenState();
}

class _LegalDocumentFixScreenState extends State<LegalDocumentFixScreen> {
  final _formKey = GlobalKey<FormState>();
  final _licenseController = TextEditingController();
  final _expiryDateController = TextEditingController();

  File? _frontImage;
  File? _backImage;
  bool _showImageError = false;
  DateTime? _selectedExpiryDate;
  final DateFormat _displayFormat = DateFormat('dd/MM/yyyy');
  final DateFormat _apiFormat = DateFormat('yyyy-MM-dd');

  Future<void> _pickImage(bool isFront) async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      setState(() {
        if (isFront) {
          _frontImage = File(pickedFile.path);
        } else {
          _backImage = File(pickedFile.path);
        }
        _showImageError = false;
      });
    }
  }

  Future<void> _selectExpiryDate() async {
    FocusScope.of(context).unfocus();

    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime(2100),
      helpText: 'Select Expiry Date',
      cancelText: 'Cancel',
      confirmText: 'Confirm',
      locale: const Locale('en', 'US'),
    );

    if (picked != null) {
      setState(() {
        _selectedExpiryDate = picked;
        _expiryDateController.text = _displayFormat.format(picked);
      });
    }
  }

  void _submit() {
    final isValid = _formKey.currentState!.validate();

    if (!isValid || _frontImage == null || _backImage == null) {
      setState(() {
        _showImageError = true;
      });
      return;
    }

    widget.data.licenseNumber = _licenseController.text.trim();
    widget.data.licenseExpiryDate = _expiryDateController.text.trim();
    widget.data.documentFrontImagePath = _frontImage!.path;
    widget.data.documentBackImagePath = _backImage!.path;

    print('LegalDocumentScreen Submitted:');
    print('licenseNumber: ${widget.data.licenseNumber}');
    print('licenseExpiryDate: ${widget.data.licenseExpiryDate}');
    print('documentFrontImagePath: ${widget.data.documentFrontImagePath}');
    print('documentBackImagePath: ${widget.data.documentBackImagePath}');

    Navigator.pop(context, widget.data);
  }

  Widget _imagePicker(String label, File? image, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 150,
        height: 150,
        decoration: BoxDecoration(
          color: Colors.grey[300],
          border: Border.all(
            color: _showImageError && image == null ? const Color(0xFFBB0000) : Colors.grey,
            width: 2,
          ),
        ),
        child: image != null
            ? Image.file(image, fit: BoxFit.cover)
            : Center(child: Text(label)),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Legal Document - Res Fix'),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                IconButton(
                  icon: const Icon(Icons.arrow_back, color: Color(0xFF013171)),
                  onPressed: () => Navigator.pop(context),
                ),
                const SizedBox(height: 12),

                const Text("Certificate Number:"),
                const SizedBox(height: 4),
                TextFormField(
                  controller: _licenseController,
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    hintText: "Enter certificate number...",
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return "Please enter the certificate number";
                    }
                    return null;
                  },
                ),

                const SizedBox(height: 12),
                const Text("Expiry Date:"),
                const SizedBox(height: 4),
                TextFormField(
                  controller: _expiryDateController,
                  readOnly: true,
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    hintText: "Select expiry date...",
                    suffixIcon: Icon(Icons.calendar_today),
                  ),
                  onTap: _selectExpiryDate,
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return "Please select an expiry date";
                    }
                    return null;
                  },
                ),

                const SizedBox(height: 20),
                const Text("Front & Back Certificate Images"),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    _imagePicker("Front", _frontImage, () => _pickImage(true)),
                    _imagePicker("Back", _backImage, () => _pickImage(false)),
                  ],
                ),
                if (_showImageError)
                  const Padding(
                    padding: EdgeInsets.only(top: 8),
                    child: Text(
                      "Please select both front and back images.",
                      style: TextStyle(color: Color(0xFFBB0000)),
                    ),
                  ),

                const Spacer(),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Color(0xFFBB0000),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                    ),
                    onPressed: _submit,
                    child: const Text("Confirm", style: TextStyle(fontSize: 16, color: Colors.white),), 
                    
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
