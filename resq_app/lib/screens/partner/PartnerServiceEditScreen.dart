import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class PartnerServiceEditScreen extends StatefulWidget {
  final int userId;
  final String partnerType; // 'resFix', 'resTow', or 'resDrive'

  const PartnerServiceEditScreen({
    super.key,
    required this.userId,
    required this.partnerType,
  });

  @override
  State<PartnerServiceEditScreen> createState() =>
      _PartnerServiceEditScreenState();
}

class _PartnerServiceEditScreenState extends State<PartnerServiceEditScreen> {
  List<dynamic> availableServices = [];
  Set<int> selectedServiceIds = {};
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    loadServices();
  }

  Future<void> loadServices() async {
    try {
      final available = await http.get(
        Uri.parse('$partnerUrl/services?type=${widget.partnerType}'),
        headers: headers,
      );
      final selected = await http.get(
        Uri.parse(
          '$partnerUrl/services/selected?userId=${widget.userId}&type=${widget.partnerType}',
        ),
        headers: headers,
      );

      if (available.statusCode == 200 && selected.statusCode == 200) {
        setState(() {
          availableServices = json.decode(available.body);
          selectedServiceIds =
              (json.decode(selected.body) as List)
                  .map<int>((id) => id as int)
                  .toSet();
          isLoading = false;
        });
      }
    } catch (e) {
      print("❌ Error loading services: $e");
      setState(() => isLoading = false);
    }
  }

  Future<void> submitUpdatedServices() async {
    try {
      final response = await http.post(
        Uri.parse('$partnerUrl/services/update'),
        headers: headers,
        body: json.encode({
          'userId': widget.userId,
          'type': widget.partnerType,
          'serviceIds': selectedServiceIds.toList(),
        }),
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Services updated successfully!")),
        );
        Navigator.pop(context);
      } else {
        print("❌ Response failed: ${response.statusCode} - ${response.body}");
        throw Exception(
          "Update failed (${response.statusCode}): ${response.body.isNotEmpty ? response.body : 'No response body'}",
        );
      }
    } catch (e) {
      print("❌ Error submitting updated services: $e");
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Error: $e")));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Edit Services'),
      body:
          isLoading
              ? const Center(child: CircularProgressIndicator())
              : ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: availableServices.length,
                itemBuilder: (context, index) {
                  final service = availableServices[index];
                  final serviceId = service['serviceId'];
                  final serviceName = service['serviceName'];

                  return CheckboxListTile(
                    title: Text(serviceName),
                    value: selectedServiceIds.contains(serviceId),
                    onChanged: (bool? value) {
                      setState(() {
                        if (value == true) {
                          selectedServiceIds.add(serviceId);
                        } else {
                          selectedServiceIds.remove(serviceId);
                        }
                      });
                    },
                  );
                },
              ),
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.all(16),
        child: ElevatedButton(
          onPressed: submitUpdatedServices,
          style: ElevatedButton.styleFrom(
            backgroundColor: Color(0xFF013171),
            padding: const EdgeInsets.symmetric(vertical: 14),
          ),
          child: const Text(
            "Save Changes",
            style: TextStyle(fontSize: 16, color: Colors.white),
          ),
        ),
      ),
    );
  }
}
