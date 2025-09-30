import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/models/PartnerRegistrationData.dart';
import 'package:resq_app/config/app_config.dart';

class SelectServiceScreen extends StatefulWidget {
  final PartnerRegistrationData data;

  const SelectServiceScreen({super.key, required this.data});

  @override
  State<SelectServiceScreen> createState() => _SelectServiceScreenState();
}

class _SelectServiceScreenState extends State<SelectServiceScreen> {
  List<dynamic> services = [];
  final Set<int> selectedIndexes = {};

  @override
  void initState() {
    super.initState();
    _fetchServices();
  }

  Future<void> _fetchServices() async {
    final type = widget.data.getServiceType();
    print("${type}"); // "ResFix", "ResTow", or "ResDrive"
    final url = Uri.parse("$partnerUrl/services?type=$type");

    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        setState(() {
          services = data;
        });
      } else {
        print("Failed to load services: ${response.statusCode}");
      }
    } catch (e) {
      print("Error fetching services: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              Align(
                alignment: Alignment.centerLeft,
                child: IconButton(
                  padding: EdgeInsets.zero, // loại bỏ padding mặc định
                  constraints:
                      const BoxConstraints(), // loại bỏ kích thước mặc định
                  icon: const Icon(Icons.arrow_back, color: Color(0xFF013171)),
                  onPressed: () => Navigator.pop(context),
                ),
              ),

              const SizedBox(height: 20),
              services.isEmpty
                  ? const Center(child: CircularProgressIndicator())
                  : Expanded(
                    child:
                        services.isEmpty
                            ? const Center(child: CircularProgressIndicator())
                            : ListView.builder(
                              itemCount: services.length,
                              itemBuilder: (context, index) {
                                final service = services[index];
                                final selected = selectedIndexes.contains(
                                  index,
                                );

                                return CheckboxListTile(
                                  title: Text(
                                    service['serviceName'] ??
                                        'Service Name Unavailable',
                                  ),
                                  value: selected,
                                  onChanged: (val) {
                                    setState(() {
                                      if (val == true) {
                                        selectedIndexes.add(index);
                                      } else {
                                        selectedIndexes.remove(index);
                                      }
                                    });
                                  },
                                  secondary: Text(
                                    "${(service['fixedPrice'] ?? 0).toString()} VND",
                                    style: const TextStyle(
                                      color: Colors.grey,
                                      fontStyle: FontStyle.italic,
                                    ),
                                  ),
                                );
                              },
                            ),
                  ),

              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Color(0xFFBB0000),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  onPressed: () {
                    final selectedServices =
                        selectedIndexes
                            .map((i) => services[i]['serviceId'] as int)
                            .toList();

                    if (selectedServices.isEmpty) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text("Please select at least one service"),
                        ),
                      );
                      return;
                    }

                    widget.data.selectedServices = selectedServices;

                    print('SelectServiceScreen Submitted:');
                    print('Selected Services: ${widget.data.selectedServices}');

                    Navigator.pop(context, widget.data);
                  },

                  child: const Text(
                    "Submit",
                    style: TextStyle(fontSize: 16, color: Colors.white),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
