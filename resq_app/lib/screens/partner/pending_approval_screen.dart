import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/screens/partner/partner_type_screen.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import '../../main.dart';
import 'PartnerDashboardScreen.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/screens/customer/home_profile.dart';

class PendingApprovalScreen extends StatelessWidget {
  final int userId;

  const PendingApprovalScreen({super.key, required this.userId});

  Future<bool> checkVerificationStatus() async {
    final url = Uri.parse("$partnerUrl/registered-types?userId=$userId");
    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      return json['verificationStatus'] == true;
    }

    throw Exception("Unable to check verification status.");
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<bool>(
      future: checkVerificationStatus(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }

        if (snapshot.hasError) {
          return Scaffold(
            body: Center(
              child: Text('Error checking status: ${snapshot.error}'),
            ),
          );
        }

        // If verified, navigate to Partner Dashboard
        if (snapshot.data == true) {
          Future.microtask(() {
            Navigator.pushReplacement(
              context,
              MaterialPageRoute(
                builder: (_) => PartnerDashboardScreen(userId: userId),
              ),
            );
          });
          return const Scaffold(); // prevent flickering
        }

        // If still pending
        return Scaffold(
          appBar: const CommonAppBar(title: 'Pending Approval'),
          body: Padding(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text(
                  "Your application is under review.\nPlease wait.",
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 18),
                ),
                const SizedBox(height: 32),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF013171),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                    ),
                    onPressed: () {
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(
                          builder:
                              (context) => PartnerTypeScreen(userId: userId),
                        ),
                      );
                    },
                    child: const Text(
                      "Register More Partner Types",
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: OutlinedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF013171)),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                    ),
                    onPressed: () {
                      Navigator.pushAndRemoveUntil(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const HomeProfilePage(),
                        ),
                        (route) => false,
                      );
                    },
                    child: const Text(
                      "Return to Home",
                      style: TextStyle(color: Color(0xFF013171)),
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
