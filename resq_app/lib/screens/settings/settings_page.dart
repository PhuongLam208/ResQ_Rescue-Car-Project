import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/screens/partner/partner_type_screen.dart';
import 'package:resq_app/screens/partner/pending_approval_screen.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:resq_app/screens/auth/login_screen.dart';
import 'PolicyPage.dart';
import 'FaqPage.dart';

class SettingsPage extends StatefulWidget {
  final String status;
  final int userId;

  const SettingsPage({super.key, required this.userId, required this.status});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool isRegistered = false;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    checkIfRegistered();
  }

  Future<void> checkIfRegistered() async {
    try {
      final response = await http.get(
        Uri.parse('$partnerUrl/is-registered?userId=${widget.userId}'),
      );

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        setState(() {
          isRegistered = data['registered'] == true;
          isLoading = false;
        });
      } else {
        setState(() {
          isRegistered = false;
          isLoading = false;
        });
      }
    } catch (e) {
      print('Error checking registration: $e');
      setState(() {
        isRegistered = false;
        isLoading = false;
      });
    }
  }

  _handleLogout() {
    loginResponse = null;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (context) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: const CommonAppBar(title: 'General Settings'),

      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        children: [
          const SizedBox(height: 8),

          // App Color Theme
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: const [
                  Icon(Icons.do_not_disturb_on, color: Color(0xFFBB0000)),
                  SizedBox(width: 12),
                  Text("App Theme", style: TextStyle(fontSize: 16)),
                ],
              ),
              Switch(
                value: true,
                onChanged: (val) {},
                activeColor: Color(0xFFBB0000),
              ),
            ],
          ),
          const Divider(),

          // Language
          ListTile(
            leading: const Icon(Icons.language, color: Color(0xFFBB0000)),
            title: const Text("Language"),
            trailing: Text(
              "English",
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: Color(0xFFBB0000),
              ),
            ),
          ),
          const Divider(),

          // Feedback
          ListTile(
            leading: const Icon(
              Icons.chat_bubble_outline,
              color: Color(0xFFBB0000),
            ),
            title: const Text("Frequently Asked Questions"),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const FaqPage()),
              );
            },
          ),

          // Guidelines
          ListTile(
            leading: const Icon(Icons.chat, color: Color(0xFFBB0000)),
            title: const Text("Terms and Policies"),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const PolicyPage()),
              );
            },
          ),
          const Divider(),

          // Current Version
          ListTile(
            leading: const Icon(Icons.menu_book, color: Color(0xFFBB0000)),
            title: const Text("Current Version"),
            trailing: const Text(
              "3.2.0",
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
          ),

          // Register as Partner
          ListTile(
            leading: const Icon(
              Icons.chat_bubble_outline,
              color: Color(0xFFBB0000),
            ),
            title: const Text("Become a ResQ Partner"),
            onTap: () async {
              if (widget.status == "ACTIVE") {
                final result = await Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder:
                        (context) => PartnerTypeScreen(userId: widget.userId),
                  ),
                );

                if (result == true) {
                  checkIfRegistered();
                }
              } else {
                showDialog(
                  context: context,
                  builder:
                      (context) => AlertDialog(
                        title: const Text(
                          "Action Required",
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Color(0xFFBB0000),
                          ),
                        ),
                        content: const Text(
                          "You need to verify your identity and wait for approval before becoming our partner.",
                        ),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(context),
                            child: const Text("OK"),
                          ),
                        ],
                      ),
                );
              }
            },
          ),
          const Divider(),

          // Switch to Partner Interface
          if (isRegistered)
            ListTile(
              leading: const Icon(Icons.menu_book, color: Color(0xFFBB0000)),
              title: const Text("Switch to Partner Mode"),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder:
                        (context) =>
                            PendingApprovalScreen(userId: widget.userId),
                  ),
                );
              },
            ),
          const Divider(),

          ListTile(
            leading: const Icon(Icons.logout, color: Color(0xFFBB0000)),
            title: const Text("Log Out"),
            onTap: () {
              _handleLogout();
            },
          ),
        ],
      ),
    );
  }
}
