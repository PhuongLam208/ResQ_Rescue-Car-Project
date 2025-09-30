import 'package:flutter/material.dart';
import 'package:resq_app/screens/customer/home_profile.dart';
import 'package:resq_app/services/auth_service.dart';
import 'package:resq_app/models/auth/register.dart';
import 'package:resq_app/models/auth/login_request.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'dart:convert';

class RegisterScreen extends StatefulWidget {
  final String phoneNumber;

  const RegisterScreen({super.key, required this.phoneNumber});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final fullNameController = TextEditingController();
  final emailController = TextEditingController();
  final dobController = TextEditingController();
  final passwordController = TextEditingController();
  final confirmPasswordController = TextEditingController();
  String gender = 'Male';

  @override
  void dispose() {
    fullNameController.dispose();
    emailController.dispose();
    dobController.dispose();
    passwordController.dispose();
    confirmPasswordController.dispose();
    super.dispose();
  }

  InputDecoration buildInputDecoration(String hint) {
    return InputDecoration(
      hintText: hint,
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
      filled: true,
      fillColor: Colors.white,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: const BorderSide(color: Colors.grey),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: const BorderSide(color: Colors.grey),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: const BorderSide(color: Color(0xFF013171), width: 1.5),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: const BorderSide(color: Color(0xFFBB0000)),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: const BorderSide(color: Color(0xFFBB0000), width: 1.5),
      ),
      errorStyle: const TextStyle(fontSize: 12, height: 1.2),
    );
  }

  void handleRegister() async {
    if (_formKey.currentState!.validate()) {
      final request = Register(
        fullName: fullNameController.text,
        password: passwordController.text,
        email:
            emailController.text.trim().isEmpty
                ? ''
                : emailController.text.trim(),
        phoneNumber: widget.phoneNumber,
        gender: gender,
        dob: dobController.text,
      );

      final registerResult = await AuthService().register(request);

      if (registerResult.statusCode == 200) {
        final loginRequest = LoginRequest(
          phoneNumber: widget.phoneNumber,
          password: passwordController.text,
        );

        final Response = await AuthService.login(loginRequest);
        loginResponse = Response;

        if (loginResponse != null) {
          final prefs = await SharedPreferences.getInstance();
          await prefs.setString(
            'login_response',
            jsonEncode(loginResponse?.toJson()),
          );

          if (!mounted) return;
          Navigator.pushAndRemoveUntil(
            context,
            MaterialPageRoute(builder: (_) => HomeProfilePage()),
            (route) => false,
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text("Login failed after registration")),
          );
        }
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Register failed: ${registerResult.body}")),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Register'),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text("Fullname"),
              const SizedBox(height: 2),
              TextFormField(
                controller: fullNameController,
                style: const TextStyle(fontSize: 14),
                decoration: buildInputDecoration('Enter your full name'),
                validator:
                    (value) =>
                        value == null || value.isEmpty
                            ? 'Required field'
                            : null,
              ),
              const SizedBox(height: 12),

              const Text(
                "Phone Number",
                style: TextStyle(color: Color(0xFF013171)),
              ),
              const SizedBox(height: 2),
              TextFormField(
                initialValue: widget.phoneNumber,
                readOnly: true,
                style: const TextStyle(fontSize: 14),
                decoration: buildInputDecoration('Phone Number'),
              ),
              const SizedBox(height: 12),

              const Text(
                "Email (optional)",
                style: TextStyle(color: Color(0xFF013171)),
              ),
              const SizedBox(height: 2),
              TextFormField(
                controller: emailController,
                style: const TextStyle(fontSize: 14),
                decoration: buildInputDecoration('example@email.com'),
                validator: (value) {
                  if (value != null && value.isNotEmpty) {
                    final emailRegex = RegExp(
                      r'^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$',
                    );
                    if (!emailRegex.hasMatch(value)) {
                      return 'Invalid email format';
                    }
                  }
                  return null;
                },
              ),
              const SizedBox(height: 12),

              const Text(
                "Date of Birth",
                style: TextStyle(color: Color(0xFF013171)),
              ),
              const SizedBox(height: 2),
              TextFormField(
                controller: dobController,
                style: const TextStyle(fontSize: 14),
                readOnly: true,
                decoration: buildInputDecoration('YYYY-MM-DD'),
                validator:
                    (value) =>
                        (value == null || value.isEmpty)
                            ? 'Please select your birth date'
                            : null,
                onTap: () async {
                  FocusScope.of(context).requestFocus(FocusNode());
                  final date = await showDatePicker(
                    context: context,
                    firstDate: DateTime(1900),
                    lastDate: DateTime.now(),
                    initialDate: DateTime(2000),
                  );
                  if (date != null) {
                    dobController.text = "${date.toLocal()}".split(' ')[0];
                  }
                },
              ),
              const SizedBox(height: 12),

              const Text("Gender", style: TextStyle(color: Color(0xFF013171))),
              Row(
                children: [
                  Radio<String>(
                    value: 'Male',
                    groupValue: gender,
                    onChanged: (val) => setState(() => gender = val!),
                  ),
                  const Text("Male"),
                  Radio<String>(
                    value: 'Female',
                    groupValue: gender,
                    onChanged: (val) => setState(() => gender = val!),
                  ),
                  const Text("Female"),
                  Radio<String>(
                    value: 'Other',
                    groupValue: gender,
                    onChanged: (val) => setState(() => gender = val!),
                  ),
                  const Text("Other"),
                ],
              ),
              const SizedBox(height: 12),

              const Text(
                "Password",
                style: TextStyle(color: Color(0xFF013171)),
              ),
              const SizedBox(height: 2),
              TextFormField(
                obscureText: true,
                controller: passwordController,
                style: const TextStyle(fontSize: 14),
                decoration: buildInputDecoration('Enter password'),
                validator:
                    (value) =>
                        value == null || value.isEmpty
                            ? 'Required field'
                            : null,
              ),
              const SizedBox(height: 12),

              const Text(
                "Confirm Password",
                style: TextStyle(color: Color(0xFF013171)),
              ),
              const SizedBox(height: 2),
              TextFormField(
                obscureText: true,
                controller: confirmPasswordController,
                style: const TextStyle(fontSize: 14),
                decoration: buildInputDecoration('Re-enter password'),
                validator: (value) {
                  if (value == null || value.isEmpty) return 'Required field';
                  if (value != passwordController.text) {
                    return 'Passwords do not match';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 24),

              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: handleRegister,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFBB0000),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                  ),
                  child: const Text(
                    'Register',
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
