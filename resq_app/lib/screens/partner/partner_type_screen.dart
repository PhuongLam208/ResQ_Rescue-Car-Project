import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/models/PartnerRegistrationData.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import '../settings/settings_page.dart';
import 'PartnerChecklistScreen.dart';
import 'package:resq_app/config/app_config.dart';

class PartnerTypeScreen extends StatefulWidget {
  final int userId;

  const PartnerTypeScreen({super.key, required this.userId});

  @override
  State<PartnerTypeScreen> createState() => _PartnerTypeScreenState();
}

class _PartnerTypeScreenState extends State<PartnerTypeScreen> {
  int resFixRegistered = 0;
  int resTowRegistered = 0;
  int resDriveRegistered = 0;
  String userFullname = "";

  @override
  void initState() {
    super.initState();
    fetchRegisteredPartnerTypes();
    fetchUserFullname();
  }

  Future<void> fetchRegisteredPartnerTypes() async {
    final response = await http.get(
      Uri.parse('$partnerUrl/registered-types?userId=${widget.userId}'),
      headers: headers,
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      setState(() {
        resFixRegistered = data['resFix'] ?? 0;
        resTowRegistered = data['resTow'] ?? 0;
        resDriveRegistered = data['resDrive'] ?? 0;
      });
    } else {
      print("⚠️ Error loading registered partner types: ${response.statusCode}");
    }
  }

  void _onSelectType(BuildContext context, String type) {
    final data = PartnerRegistrationData(userId: widget.userId);
    if (type == 'fix') data.resFix = 2;
    if (type == 'tow') data.resTow = 2;
    if (type == 'drive') data.resDrive = 2;

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => PartnerChecklistScreen(data: data),
      ),
    );
  }

  Future<void> fetchUserFullname() async {
    final response = await http.get(
      Uri.parse('$partnerUrl/user-fullname?userId=${widget.userId}'),
      headers: headers,
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      setState(() {
        userFullname = data['fullname'] ?? '';
      });
    } else {
      print("⚠️ Error loading user fullname: ${response.statusCode}");
    }
  }

  Widget _partnerOption({
    required String title,
    required String imagePath,
    required bool isDisabled,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: isDisabled ? null : onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        margin: const EdgeInsets.only(bottom: 16),
        decoration: BoxDecoration(
          color: isDisabled ? Colors.grey.shade200 : Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black12,
              blurRadius: 6,
              offset: Offset(0, 3),
            ),
          ],
        ),
        child: Row(
          children: [
            ClipOval(
              child: Image.asset(imagePath, width: 50, height: 50, fit: BoxFit.cover),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Text(
                title,
                style: TextStyle(
                  fontSize: 18,
                  color: isDisabled ? Colors.grey : Colors.black,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            if (isDisabled)
              const Icon(Icons.check_circle, color: Colors.green),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Partner Types'),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              IconButton(
                icon: const Icon(Icons.arrow_back, color: Color(0xFF013171), size: 28),
                onPressed: () {
                  Navigator.pop(context, true);
                },
              ),
              const SizedBox(height: 16),
              Text(
                "Hello,\n$userFullname!",
                style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              const Text(
                "Would you like to become our partner?",
                style: TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 24),
              _partnerOption(
                title: "On-site Repair",
                imagePath: 'assets/images/cartire.jpg',
                isDisabled: resFixRegistered > 0,
                onTap: () => _onSelectType(context, 'fix'),
              ),
              _partnerOption(
                title: "Towing Service",
                imagePath: 'assets/images/towtruck1.jpg',
                isDisabled: resTowRegistered > 0,
                onTap: () => _onSelectType(context, 'tow'),
              ),
              _partnerOption(
                title: "Substitute Driver",
                imagePath: 'assets/images/idea4_resq.jpg',
                isDisabled: resDriveRegistered > 0,
                onTap: () => _onSelectType(context, 'drive'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
