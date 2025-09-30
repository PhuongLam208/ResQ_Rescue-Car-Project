import 'package:flutter/material.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:resq_app/screens/auth/verify_phone_screen.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  int? userId = loginResponse?.userId;
  Map<String, dynamic>? customer;
  String? error;
  bool isLoading = true;
  bool isEditing = false;

  final List<String> genderOptions = ["Male", "Female", "Other"];
  String? selectedGender;
  DateTime? selectedDob;
  late TextEditingController emailController;
  late TextEditingController usernameController;
  Map<String, dynamic> errors = {};

  @override
  void initState() {
    super.initState();
    emailController = TextEditingController();
    usernameController = TextEditingController();
    fetchCustomer();
  }

  @override
  void dispose() {
    emailController.dispose();
    usernameController.dispose();
    super.dispose();
  }

  Future<void> fetchCustomer() async {
    try {
      final result = await CustomerService.getCustomerProfile(userId!);
      setState(() {
        customer = result;
        usernameController.text = result['username'] ?? '';
        emailController.text = result['email'] ?? '';
        selectedGender = result['gender'];
        if (result['dob'] != null) {
          selectedDob = DateTime.tryParse(result['dob']);
        }
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        error = e.toString();
        isLoading = false;
      });
    }
  }

  String formatDob(DateTime? date) {
    if (date == null) return "No Date";
    return DateFormat('dd/MM/yyyy').format(date.toLocal());
  }

  Widget _buildStaticInfo(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 130,
            child: Text(
              "$label:",
              style: const TextStyle(
                fontFamily: 'Raleway',
                fontWeight: FontWeight.w800,
                fontSize: 16,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value ?? "---",
              style: const TextStyle(fontFamily: 'Lexend', fontSize: 16),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEditableInfo(String label, Widget inputWidget) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          SizedBox(
            width: 130,
            child: Text(
              "$label:",
              style: const TextStyle(
                fontFamily: 'Raleway',
                fontWeight: FontWeight.w800,
                fontSize: 16,
              ),
            ),
          ),
          Expanded(child: inputWidget),
        ],
      ),
    );
  }

  Future<void> _pickDate() async {
    final initial = selectedDob ?? DateTime(2000);
    final picked = await showDatePicker(
      context: context,
      initialDate: initial,
      firstDate: DateTime(1800),
      lastDate: DateTime.now(),
    );
    if (picked != null) {
      setState(() {
        selectedDob = picked;
      });
    }
  }

  void resetFormFields() {
    if (customer != null) {
      usernameController.text = customer!['username'] ?? '';
      emailController.text = customer!['email'] ?? '';
      selectedGender = customer!['gender'];
      selectedDob =
          customer!['dob'] != null ? DateTime.tryParse(customer!['dob']) : null;
    }
  }

  Future<bool> _submitUpdate() async {
    if (customer == null) return false;

    final dto = {
      "userId": customer!["userId"],
      "username": usernameController.text,
      "email": emailController.text,
      "sdt": customer!["sdt"],
      "gender": selectedGender,
      "dob": selectedDob?.toIso8601String(),
    };

    try {
      final result = await CustomerService.updateCustomer(userId!, dto);
      print("Result: $result");
      if (!result.containsKey("errors")) {
        return true;
      } else {
        final dynamic serverErrors = result['errors'];
        setState(() {
          errors =
              (serverErrors is Map)
                  ? serverErrors.map(
                    (k, v) => MapEntry(k.toString(), v.toString()),
                  )
                  : {};
        });
        return false;
      }
    } catch (e) {
      setState(() {
        errors = {"general": "Failed to update: $e"};
      });
      return false;
    }
  }

  void _showSuccessDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        Future.delayed(const Duration(seconds: 2), () {
          if (context.mounted) Navigator.of(context).pop();
        });

        return AlertDialog(
          backgroundColor: Colors.white,
          title: Text(
            "Update Success",
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 25,
              fontFamily: 'Raleway',
              fontWeight: FontWeight.bold,
              color: Colors.green[700],
            ),
          ),
          content: const Text(
            "Your information has been updated successfully!",
            textAlign: TextAlign.center,
            style: TextStyle(fontFamily: "Lexend", fontSize: 17),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Profile Information'),
      body: Padding(
        padding: const EdgeInsets.symmetric(vertical: 30, horizontal: 50),
        child: Builder(
          builder: (_) {
            if (isLoading)
              return const Center(child: CircularProgressIndicator());
            if (error != null) {
              return Center(
                child: Text(
                  "Error: $error",
                  style: const TextStyle(color: Color(0xFFBB0000)),
                ),
              );
            }
            if (customer == null) {
              return const Center(
                child: Text(
                  "Customer not found",
                  style: TextStyle(color: Color(0xFFBB0000)),
                ),
              );
            }

            return SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const SizedBox(height: 40),
                  CircleAvatar(
                    radius: 100,
                    backgroundImage: NetworkImage(
                      "$baseUrl/${customer!["avatar"] ?? "/uploads/avatar/user.png"}",
                    ),
                    backgroundColor: Colors.transparent,
                  ),
                  const SizedBox(height: 12),
                  isEditing
                      ? _buildEditableInfo(
                        "Username",
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            SizedBox(
                              height: 40,
                              child: TextFormField(
                                controller: usernameController,
                                decoration: const InputDecoration(
                                  border: OutlineInputBorder(),
                                  contentPadding: EdgeInsets.symmetric(
                                    horizontal: 12,
                                    vertical: 4,
                                  ),
                                ),
                              ),
                            ),
                            if (errors['username'] != null)
                              Text(
                                errors['username'],
                                style: const TextStyle(
                                  color: Color(0xFFBB0000),
                                  fontSize: 12,
                                ),
                              ),
                          ],
                        ),
                      )
                      : Text(
                        customer!["username"] ?? "---",
                        style: const TextStyle(
                          fontSize: 24,
                          fontFamily: 'Lexend',
                          fontWeight: FontWeight.w700,
                          color: Color(0xFFBB0000),
                        ),
                      ),
                  const SizedBox(height: 20),
                  isEditing
                      ? _buildEditableInfo(
                        "Email",
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            SizedBox(
                              height: 40,
                              child: TextFormField(
                                controller: emailController,
                                decoration: const InputDecoration(
                                  border: OutlineInputBorder(),
                                  contentPadding: EdgeInsets.symmetric(
                                    horizontal: 12,
                                    vertical: 4,
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      )
                      : _buildStaticInfo("Email", customer!["email"]),
                  isEditing
                      ? Padding(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            const SizedBox(
                              width: 130,
                              child: Text(
                                "Phone No:",
                                style: TextStyle(
                                  fontFamily: 'Raleway',
                                  fontWeight: FontWeight.w800,
                                  fontSize: 16,
                                ),
                              ),
                            ),
                            Expanded(
                              child: Text(
                                customer!["sdt"] ?? "---",
                                style: const TextStyle(
                                  fontFamily: 'Lexend',
                                  fontSize: 16,
                                ),
                              ),
                            ),
                            TextButton(
                              style: TextButton.styleFrom(
                                backgroundColor: Color(0xFF013171),
                                minimumSize: const Size(0, 25),
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 8,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10),
                                ),
                              ),
                              onPressed: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder:
                                        (context) => const VerifyPhoneScreen(
                                          purpose: 'update',
                                        ),
                                  ),
                                );
                              },
                              child: const Text(
                                "Change",
                                style: TextStyle(
                                  fontFamily: 'Lexend',
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                  fontSize: 12,
                                ),
                              ),
                            ),
                          ],
                        ),
                      )
                      : _buildStaticInfo("Phone No", customer!["sdt"]),
                  isEditing
                      ? _buildEditableInfo(
                        "Gender",
                        DropdownButtonFormField<String>(
                          value: selectedGender,
                          items:
                              genderOptions.map((String gender) {
                                return DropdownMenuItem(
                                  value: gender,
                                  child: Text(gender),
                                );
                              }).toList(),
                          onChanged:
                              (val) => setState(() => selectedGender = val),
                          decoration: const InputDecoration(
                            border: OutlineInputBorder(),
                            contentPadding: EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 4,
                            ),
                          ),
                        ),
                      )
                      : _buildStaticInfo("Gender", customer!["gender"]),
                  isEditing
                      ? _buildEditableInfo(
                        "DOB",
                        GestureDetector(
                          onTap: _pickDate,
                          child: Container(
                            height: 40,
                            alignment: Alignment.centerLeft,
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            decoration: BoxDecoration(
                              border: Border.all(color: Colors.grey),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              selectedDob != null
                                  ? formatDob(selectedDob)
                                  : "---",
                              style: const TextStyle(
                                fontSize: 16,
                                fontFamily: 'Lexend',
                              ),
                            ),
                          ),
                        ),
                      )
                      : _buildStaticInfo("DOB", formatDob(selectedDob)),
                  const SizedBox(height: 40),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      ElevatedButton.icon(
                        onPressed: () async {
                          if (isEditing) {
                            print("HI"); // Hiện dialog trước
                            final success = await _submitUpdate();
                            print(success); // Hiện dialog trước
                            if (success) {
                              if (context.mounted) _showSuccessDialog();
                              setState(() => isEditing = false);
                              fetchCustomer(); // Gọi fetch lại sau (không cần await nếu không dùng kết quả)
                            }
                          } else {
                            setState(() => isEditing = true);
                          }
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Color(0xFF013171),
                          padding: const EdgeInsets.symmetric(
                            horizontal: 24,
                            vertical: 12,
                          ),
                        ),
                        icon: Icon(
                          isEditing ? Icons.save : Icons.edit,
                          color: Colors.white,
                        ),
                        label: Text(
                          isEditing ? "Save" : "Edit Profile",
                          style: const TextStyle(
                            color: Colors.white,
                            fontFamily: 'Lexend',
                            fontSize: 17,
                          ),
                        ),
                      ),
                      if (isEditing) ...[
                        const SizedBox(width: 16),
                        ElevatedButton(
                          onPressed: () {
                            resetFormFields(); // ← Gọi reset dữ liệu từ customer
                            setState(() {
                              isEditing = false; // ← Tắt chế độ chỉnh sửa
                              errors.clear(); // ← Xoá lỗi nếu có
                            });
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Color(0xFFBB0000),
                            padding: const EdgeInsets.symmetric(
                              horizontal: 20,
                              vertical: 12,
                            ),
                          ),
                          child: const Text(
                            "Cancel",
                            style: TextStyle(
                              color: Colors.white,
                              fontFamily: 'Lexend',
                              fontSize: 17,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                  if (errors['general'] != null) ...[
                    const SizedBox(height: 10),
                    Text(
                      errors['general'],
                      style: const TextStyle(color: Color(0xFFBB0000)),
                    ),
                  ],
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
