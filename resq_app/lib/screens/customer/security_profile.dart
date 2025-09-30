import 'package:flutter/material.dart';
import 'package:dotted_border/dotted_border.dart';
import 'package:resq_app/screens/customer/personalData/personal_data_detail_page.dart';
import './change_password.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:resq_app/config/app_config.dart';

class SecurityProfilePage extends StatefulWidget {
  final bool hasPD;
  const SecurityProfilePage({super.key, required this.hasPD});

  @override
  State<SecurityProfilePage> createState() => _SecurityProfilePageState();
}

class _SecurityProfilePageState extends State<SecurityProfilePage> {
  String? frontImageUrl;
  String? backImageUrl;
  String? verificationStatus;

  @override
  void initState() {
    super.initState();
    _loadPDImages();
  }

  Future<void> _loadPDImages() async {
    final result = await CustomerService.getCustomerPersonalData(
      loginResponse?.userId ?? 0,
    );

    if (result != null && mounted) {
      setState(() {
        String stripAdminPrefix(String url) {
          final uriParts = url.split('?');
          final path = uriParts[0].replaceFirst('/admin/personaldoc', '');
          final query = uriParts.length > 1 ? '?${uriParts[1]}' : '';
          return '$path$query';
        }

        final String? frontImagePath = result['frontImageUrl'];
        final String? backImagePath = result['backImageUrl'];

        frontImageUrl =
            frontImagePath != null
                ? '$customerUrl${stripAdminPrefix(frontImagePath)}'
                : null;

        backImageUrl =
            backImagePath != null
                ? '$customerUrl${stripAdminPrefix(backImagePath)}'
                : null;

        verificationStatus = result['verificationStatus']; 
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Security Profile'),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              const SizedBox(height: 20),
              Row(
                children: [
                  Expanded(
                    child: SizedBox(
                      height: 44,
                      child: OutlinedButton(
                        onPressed: () {
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: ChangePasswordScreen(
                              userId: loginResponse?.userId ?? 0,
                            ),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                        },
                        style: _buttonStyle(),
                        child: const Text('Change Password'),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: SizedBox(
                      height: 44,
                      child: OutlinedButton(
                        onPressed:
                            !widget.hasPD
                                ? null
                                : () {
                                  PersistentNavBarNavigator.pushNewScreen(
                                    context,
                                    screen: const PersonalDataDetailPage(),
                                    withNavBar: true,
                                    pageTransitionAnimation:
                                        PageTransitionAnimation.cupertino,
                                  );
                                },
                        style: _buttonStyle(),
                        child: const Text('Update Personal Data'),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 40),
              _buildImageBox(frontImageUrl, 'Front side'),
              const SizedBox(height: 24),
              _buildImageBox(backImageUrl, 'Back side'),
              if (verificationStatus != null)
                Padding(
                  padding: const EdgeInsets.only(top: 16),
                  child: Text(
                    'Status: $verificationStatus',
                    style: TextStyle(
                      color:
                          verificationStatus == "VERIFIED"
                              ? Colors.green
                              : Colors.red,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  ButtonStyle _buttonStyle() {
    return OutlinedButton.styleFrom(
      foregroundColor: const Color(0xFF013171),
      side: const BorderSide(color: Color(0xFF013171), width: 1.4),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      padding: const EdgeInsets.symmetric(horizontal: 12),
      textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13.5),
    );
  }

  Widget _buildImageBox(String? imageUrl, String label) {
    return SizedBox(
      height: 180,
      width: double.infinity,
      child: DottedBorder(
        borderType: BorderType.RRect,
        radius: const Radius.circular(14),
        dashPattern: const [6, 4],
        color: Colors.grey,
        strokeWidth: 1.2,
        child:
            imageUrl != null
                ? ClipRRect(
                  borderRadius: BorderRadius.circular(14),
                  child: Image.network(imageUrl, fit: BoxFit.cover),
                )
                : const Center(
                  child: Text(
                    'Identity not verified',
                    style: TextStyle(
                      color: Colors.grey,
                      fontStyle: FontStyle.italic,
                    ),
                  ),
                ),
      ),
    );
  }
}
