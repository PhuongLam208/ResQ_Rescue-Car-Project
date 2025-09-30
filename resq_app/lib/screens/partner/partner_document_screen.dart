import 'dart:io';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:resq_app/config/app_config.dart';
import 'package:image_picker/image_picker.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class PartnerDocumentScreen extends StatefulWidget {
  final int userId;
  final List<Map<String, dynamic>> documents;

  const PartnerDocumentScreen({
    super.key,
    required this.userId,
    required this.documents,
  });

  @override
  State<PartnerDocumentScreen> createState() => _PartnerDocumentScreenState();
}

class _PartnerDocumentScreenState extends State<PartnerDocumentScreen> {
  Map<String, List<Map<String, dynamic>>> grouped = {};

  @override
  void initState() {
    super.initState();
    grouped = {'resFix': [], 'resTow': [], 'resDrive': []};

    for (var doc in widget.documents) {
      if (doc['type'] != null) {
        if (doc['type'].contains('Fixing')) {
          grouped['resFix']!.add(doc);
        } else if (doc['type'].contains('Towing')) {
          grouped['resTow']!.add(doc);
        } else if (doc['type'].contains('Driving')) {
          grouped['resDrive']!.add(doc);
        }
      }
    }

    debugPrint("📋 All initial documents:");
    for (var key in grouped.keys) {
      debugPrint("🔹 $key:");
      for (var doc in grouped[key]!) {
        debugPrint(doc.toString());
      }
    }
  }

  String buildFullUrl(String? relativePath) {
    if (relativePath == null || relativePath.isEmpty) return '';
    return '$baseUrl/$relativePath';
  }

  Future<void> reloadDocuments() async {
    final url = '$partnerUrl/documents?userId=${widget.userId}';
    final response = await http.get(Uri.parse(url), headers: headers);

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      final newGrouped = {
        'resFix': <Map<String, dynamic>>[],
        'resTow': <Map<String, dynamic>>[],
        'resDrive': <Map<String, dynamic>>[],
      };

      final documentTypes = {
        'resFix': 'Res Fix : On-site Repair',
        'resTow': 'Res Tow : Towing Service',
        'resDrive': 'Res Drive : Substitute Driver',
      };

      for (var resTypeKey in documentTypes.keys) {
        final docList = data[resTypeKey];
        if (docList == null || docList is! List) continue;

        for (var doc in docList) {
          final String? number = doc['number'];
          final String? type = doc['type'];
          final String frontImage = doc['frontImage'] ?? '';
          final String backImage = doc['backImage'] ?? '';

          newGrouped[resTypeKey]!.add({
            'id': doc['id'],
            'type': type ?? "Unknown",
            'number': number,
            'frontUrl': frontImage,
            'backUrl': backImage,
          });
        }
      }

      setState(() {
        grouped = newGrouped;
      });
    }
  }

  void _showEditDialog(Map<String, dynamic> doc) async {
    final numberController = TextEditingController(text: doc['number'] ?? '');
    File? newFrontImage;
    File? newBackImage;

    showDialog(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Edit Document'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: numberController,
                  decoration: const InputDecoration(
                    labelText: 'Document Number',
                  ),
                ),
                const SizedBox(height: 10),
                ElevatedButton(
                  onPressed: () async {
                    final picked = await ImagePicker().pickImage(
                      source: ImageSource.gallery,
                    );
                    if (picked != null) newFrontImage = File(picked.path);
                  },
                  child: const Text("Choose Front Image"),
                ),
                ElevatedButton(
                  onPressed: () async {
                    final picked = await ImagePicker().pickImage(
                      source: ImageSource.gallery,
                    );
                    if (picked != null) newBackImage = File(picked.path);
                  },
                  child: const Text("Choose Back Image"),
                ),
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel'),
              ),
              ElevatedButton(
                onPressed: () async {
                  final request = http.MultipartRequest(
                    'PUT',
                    Uri.parse('$partnerUrl/documents/update'),
                  );

                  final isVehicle = doc['id'] == null;
                  request.fields['documentId'] =
                      isVehicle ? '-1' : doc['id'].toString();
                  request.fields['userId'] = widget.userId.toString();
                  if (!isVehicle) {
                    request.fields['documentNumber'] =
                        numberController.text.trim();
                  }

                  if (newFrontImage != null) {
                    request.files.add(
                      await http.MultipartFile.fromPath(
                        'frontImage',
                        newFrontImage!.path,
                      ),
                    );
                  }
                  if (newBackImage != null) {
                    request.files.add(
                      await http.MultipartFile.fromPath(
                        'backImage',
                        newBackImage!.path,
                      ),
                    );
                  }

                  final response = await request.send();

                  if (response.statusCode == 200) {
                    await reloadDocuments();
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text("✅ Successfully updated")),
                    );
                  } else {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text("❌ Update failed")),
                    );
                  }
                },
                child: const Text('Save'),
              ),
            ],
          ),
    );
  }

  Widget _buildGroup(String title, List<Map<String, dynamic>> items) {
    final frontVehicle = items.firstWhere(
      (d) => d['type'] == "Tow Truck Image (from vehicle)",
      orElse: () => {},
    );
    final backVehicle = items.firstWhere(
      (d) => d['type'] == "License Plate Image (from vehicle)",
      orElse: () => {},
    );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 10),
        ...items.map((doc) {
          final type = doc['type'] ?? '';
          final isVehicleFront = type == "Tow Truck Image (from vehicle)";
          final isVehicleBack = type == "License Plate Image (from vehicle)";
          if (isVehicleFront || isVehicleBack) return Container();

          return Card(
            margin: const EdgeInsets.symmetric(vertical: 8),
            child: Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "Type: $type",
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      if (doc['number'] != null)
                        Text(
                          "Number: ${doc['number']}",
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                      const Spacer(),
                      if ((doc['status'] ?? '').toString().toUpperCase() ==
                          'EXPIRED')
                        IconButton(
                          icon: const Icon(Icons.edit, color: Colors.orange),
                          onPressed: () => _showEditDialog(doc),
                        ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(child: _buildImage(doc['frontUrl'], "Front")),
                      Expanded(child: _buildImage(doc['backUrl'], "Back")),
                    ],
                  ),
                ],
              ),
            ),
          );
        }).toList(),

        if (frontVehicle.isNotEmpty || backVehicle.isNotEmpty)
          Card(
            margin: const EdgeInsets.symmetric(vertical: 8),
            child: Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    "Vehicle Photos",
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(
                        child: _buildImage(
                          frontVehicle['frontImage'] ??
                              frontVehicle['frontUrl'] ??
                              '',
                          "Tow Truck",
                        ),
                      ),
                      Expanded(
                        child: _buildImage(
                          backVehicle['backImage'] ??
                              backVehicle['backUrl'] ??
                              '',
                          "License Plate",
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildImage(String? url, String label) {
    return url != null && url.isNotEmpty
        ? Image.network(buildFullUrl(url), height: 100, fit: BoxFit.cover)
        : Container(
          height: 100,
          color: Colors.grey.shade200,
          alignment: Alignment.center,
          child: Text("No $label Image"),
        );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Partner Documents'),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            if (grouped['resFix']!.isNotEmpty)
              _buildGroup("Res Fix : On-site Repair", grouped['resFix']!),
            if (grouped['resTow']!.isNotEmpty)
              _buildGroup("Res Tow : Towing Service", grouped['resTow']!),
            if (grouped['resDrive']!.isNotEmpty)
              _buildGroup(
                "Res Drive : Substitute Driver",
                grouped['resDrive']!,
              ),
          ],
        ),
      ),
    );
  }
}
