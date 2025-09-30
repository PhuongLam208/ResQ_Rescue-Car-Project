import 'package:flutter/material.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/auth_service.dart';
import 'package:resq_app/models/auth/login_request.dart';
import './verify_phone_screen.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:resq_app/screens/main_screens.dart';
import 'dart:io';
import 'package:flutter/material.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> with WidgetsBindingObserver {
  final TextEditingController loginController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  bool _checkingPermission = false;
  bool isLoading = false;

  String? phoneError;
  String? passwordError;

  @override
  void initState() {
    super.initState();
    _requestPermissions();
  }

  Future<void> _requestPermissions() async {
    final androidInfo = await DeviceInfoPlugin().androidInfo;
    final sdkInt = androidInfo.version.sdkInt;

    final permissions = <Permission>[
      Permission.camera,
      Permission.locationWhenInUse,
      sdkInt >= 33 ? Permission.photos : Permission.storage,
    ];

    await permissions.request();
  }

  void _handleLogin() async {
    final phone = loginController.text.trim();
    final pass = passwordController.text.trim();

    setState(() {
      phoneError = null;
      passwordError = null;
    });

    bool hasError = false;

    if (phone.isEmpty) {
      phoneError = 'Phone number is required';
      hasError = true;
    } else if (phone.length < 9 ||
        phone.length > 10 ||
        !RegExp(r'^\d+$').hasMatch(phone)) {
      phoneError = 'Phone number must be 9–10 digits';
      hasError = true;
    }

    if (pass.isEmpty) {
      passwordError = 'Password is required';
      hasError = true;
    }

    if (hasError) {
      setState(() {});
      return;
    }

    setState(() {
      isLoading = true;
    });

    final request = LoginRequest(phoneNumber: phone, password: pass);
    final response = await AuthService.login(request);
    loginResponse = response;

    setState(() {
      isLoading = false;
    });

    if (response != null) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Welcome ${response.userName}!')));
      Navigator.of(context, rootNavigator: true).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => MainScreen(userId: response.userId)),
        (_) => false,
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Login failed. Please try again.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async => false,
      child: Scaffold(
        backgroundColor: Colors.white,
        body: SafeArea(
          child:
              _checkingPermission
                  ? const CircularProgressIndicator()
                  : Center(
                    child: SingleChildScrollView(
                      padding: const EdgeInsets.symmetric(horizontal: 24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const SizedBox(height: 16),
                          const Text(
                            'WELCOME TO',
                            style: TextStyle(
                              fontSize: 16,
                              letterSpacing: 1.2,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Image.asset('assets/images/logo.png', height: 100),
                          const SizedBox(height: 32),

                          // Số điện thoại
                          TextField(
                            controller: loginController,
                            keyboardType: TextInputType.phone,
                            inputFormatters: [
                              FilteringTextInputFormatter.digitsOnly,
                              LengthLimitingTextInputFormatter(10),
                            ],
                            decoration: InputDecoration(
                              labelText: 'Phone Number',
                              labelStyle: TextStyle(color: Color(0xFF013171)),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                              errorText: phoneError,
                            ),
                          ),

                          const SizedBox(height: 16),

                          // Mật khẩu
                          TextField(
                            controller: passwordController,
                            obscureText: true,
                            decoration: InputDecoration(
                              labelText: 'Password',
                              labelStyle: TextStyle(color: Color(0xFF013171)),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                              errorText: passwordError,
                            ),
                          ),

                          const SizedBox(height: 24),

                          SizedBox(
                            width: double.infinity,
                            height: 50,
                            child: ElevatedButton(
                              onPressed: isLoading ? null : _handleLogin,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Color(0xFFBB0000),
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
                                        'Login',
                                        style: TextStyle(
                                          fontSize: 16,
                                          color: Colors.white,
                                        ),
                                      ),
                            ),
                          ),

                          const SizedBox(height: 24),

                          GestureDetector(
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder:
                                      (context) => const VerifyPhoneScreen(
                                        purpose: 'register',
                                      ),
                                ),
                              );
                            },
                            child: const Text.rich(
                              TextSpan(
                                text: 'Haven’t got an account yet ? ',
                                children: [
                                  TextSpan(
                                    text: 'REGISTER NOW!',
                                    style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      decoration: TextDecoration.underline,
                                    ),
                                  ),
                                ],
                              ),
                              textAlign: TextAlign.center,
                            ),
                          ),

                          const SizedBox(height: 12),

                          GestureDetector(
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder:
                                      (context) => const VerifyPhoneScreen(
                                        purpose: 'forgot',
                                      ),
                                ),
                              );
                            },
                            child: const Text(
                              'FORGOT PASSWORD',
                              style: TextStyle(
                                decoration: TextDecoration.underline,
                                color: Colors.black87,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
        ),
      ),
    );
  }
}
