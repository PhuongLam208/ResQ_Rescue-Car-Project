import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:resq_app/services/verify_service.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import './register_screen.dart';
import './forgot_password_screen.dart';
import 'package:resq_app/services/api_result.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/screens/customer/home_profile.dart';
import 'package:resq_app/models/auth/login_response.dart';

class VerifyPhoneScreen extends StatefulWidget {
  final String purpose; // 'register', 'forgot', or 'update'

  const VerifyPhoneScreen({super.key, required this.purpose});

  @override
  State<VerifyPhoneScreen> createState() => _VerifyPhoneScreenState();
}

class _VerifyPhoneScreenState extends State<VerifyPhoneScreen> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController phoneController = TextEditingController();
  final TextEditingController otpController = TextEditingController();

  bool isOtpSent = false;
  bool isLoading = false;
  int userId = loginResponse?.userId ?? 0;

  InputDecoration customInputDecoration(String hint) {
    return InputDecoration(
      hintText: hint,
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
      enabledBorder: OutlineInputBorder(
        borderSide: const BorderSide(color: Colors.grey),
        borderRadius: BorderRadius.circular(10),
      ),
      focusedBorder: OutlineInputBorder(
        borderSide: const BorderSide(color: Color(0xFF013171), width: 1.5),
        borderRadius: BorderRadius.circular(10),
      ),
      errorBorder: OutlineInputBorder(
        borderSide: const BorderSide(color: Colors.red),
        borderRadius: BorderRadius.circular(10),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderSide: const BorderSide(color: Colors.red, width: 1.5),
        borderRadius: BorderRadius.circular(10),
      ),
      errorStyle: const TextStyle(fontSize: 12, height: 1.2),
    );
  }

  void sendOtp() async {
    final otpBackup = otpController.text;
    otpController.text = '000000';
    if (!_formKey.currentState!.validate()) {
      otpController.text = otpBackup;
      return;
    }
    otpController.text = otpBackup;

    setState(() => isLoading = true);

    final phone = phoneController.text.trim();
    ApiResult result;
    if (widget.purpose == 'register') {
      result = await VerifyService().sendOtp(phone);
    } else if (widget.purpose == 'forgot') {
      result = await VerifyService().forgetPassword(phone);
    } else {
      result = await VerifyService().updatePhoneNumber(userId, phone);
    }

    setState(() {
      isOtpSent = true;
      isLoading = false;
    });

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(result.body),
        backgroundColor:
            result.statusCode == 200 ? Colors.green : const Color(0xFFBB0000),
      ),
    );
  }

  void verifyOtp() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => isLoading = true);

    final phone = phoneController.text.trim();
    final otp = otpController.text.trim();
    final otpType =
        widget.purpose == 'register'
            ? 'REGISTER'
            : widget.purpose == 'forgot'
            ? 'FORGOT PASSWORD'
            : 'VERIFY';

    final result = await VerifyService().verifyOtp(phone, otp, otpType);

    setState(() => isLoading = false);

    if (result.statusCode == 200) {
      if (widget.purpose == 'register') {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => RegisterScreen(phoneNumber: phone),
          ),
        );
      } else if (widget.purpose == 'forgot') {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => ForgotPasswordScreen(phoneNumber: phone),
          ),
        );
      } else {
        // ✅ Check userId before updating phone
        if (userId == 0) {
          showDialog(
            context: context,
            builder:
                (_) => AlertDialog(
                  title: const Text('Login Required'),
                  content: const Text(
                    'Please log in first to update your phone number.',
                  ),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text('OK'),
                    ),
                  ],
                ),
          );
          return;
        }
      
        final updateResult = await CustomerService().changePhoneNumber(
          userId,
          phone,
        );

        if (updateResult.statusCode == 200) {
          Navigator.pushAndRemoveUntil(
            context,
            MaterialPageRoute(builder: (context) => const HomeProfilePage()),
            (route) => false,
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Update failed: ${updateResult.message}'),
              backgroundColor: const Color(0xFFBB0000),
            ),
          );
        }
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error ${result.message}'),
          backgroundColor: const Color(0xFFBB0000),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Verify Phone Number'),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 24),
                TextFormField(
                  controller: phoneController,
                  keyboardType: TextInputType.phone,
                  maxLength: 10,
                  inputFormatters: [
                    FilteringTextInputFormatter.digitsOnly,
                    LengthLimitingTextInputFormatter(10),
                  ],
                  decoration: customInputDecoration('Enter your phone number'),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Phone number is required';
                    } else if (value.length < 9 ||
                        value.length > 10 ||
                        !RegExp(r'^\d{9,10}$').hasMatch(value)) {
                      return 'Phone number must be 9–10 digits only';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                if (isOtpSent) ...[
                  Row(
                    children: [
                      Expanded(
                        flex: 3,
                        child: TextFormField(
                          controller: otpController,
                          keyboardType: TextInputType.number,
                          inputFormatters: [
                            FilteringTextInputFormatter.digitsOnly,
                            LengthLimitingTextInputFormatter(10),
                          ],
                          decoration: customInputDecoration('Enter OTP'),
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'OTP is required';
                            } else if (value.length > 10 ||
                                !RegExp(r'^\d{4,10}$').hasMatch(value)) {
                              return 'Invalid OTP (digits only, max 10)';
                            }
                            return null;
                          },
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        flex: 2,
                        child: SizedBox(
                          height: 48,
                          child: ElevatedButton(
                            onPressed: isLoading ? null : sendOtp,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFFBB0000),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                            ),
                            child:
                                isLoading
                                    ? const CircularProgressIndicator(
                                      color: Colors.white,
                                    )
                                    : const Text(
                                      'Resend',
                                      style: TextStyle(
                                        fontSize: 14,
                                        color: Colors.white,
                                      ),
                                    ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ] else ...[
                  SizedBox(
                    height: 48,
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: isLoading ? null : sendOtp,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFBB0000),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child:
                          isLoading
                              ? const CircularProgressIndicator(
                                color: Colors.white,
                              )
                              : const Text(
                                'Send OTP',
                                style: TextStyle(
                                  fontSize: 16,
                                  color: Colors.white,
                                ),
                              ),
                    ),
                  ),
                ],
                const SizedBox(height: 24),
                if (isOtpSent)
                  SizedBox(
                    height: 48,
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: isLoading ? null : verifyOtp,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFBB0000),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child:
                          isLoading
                              ? const CircularProgressIndicator(
                                color: Colors.white,
                              )
                              : const Text(
                                'Confirm',
                                style: TextStyle(
                                  fontSize: 16,
                                  color: Colors.white,
                                ),
                              ),
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
