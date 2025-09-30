import 'package:flutter/material.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:resq_app/screens/customer/documentary/new_documentary_page.dart';

import 'package:resq_app/screens/customer/documentary/documentary_detail_page.dart';

class DocumentariesPage extends StatefulWidget {
  const DocumentariesPage({super.key});

  @override
  State<DocumentariesPage> createState() => _DocumentariesPageState();
}

class _DocumentariesPageState extends State<DocumentariesPage> {
  int? userId = loginResponse?.userId;
  List<dynamic>? documents;
  String? error;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    fetchDocuments();
  }

  Future<void> fetchDocuments() async {
    try {
      final result = await CustomerService.getCustomerDocuments(userId!);
      setState(() {
        documents = result;
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        error = e.toString();
        isLoading = false;
      });
    }
  }

  String _maskDocNumber(dynamic value) {
    final documentNumber = (value ?? '').toString();
    if (documentNumber.length >= 4) {
      return '***' + documentNumber.substring(documentNumber.length - 4);
    } else {
      return documentNumber.isEmpty
          ? 'Unknown Document Number'
          : '***' + documentNumber;
    }
  }

  List<String> _getAvailableTypes() {
    final existingTypes = documents?.map((e) => e['type']).toSet() ?? {};
    final allTypes = {'Identity Card', 'Passport'};
    return allTypes.difference(existingTypes.toSet()).toList();
  }

  void _showDialog(String title, String message) {
    showDialog(
      context: context,
      builder:
          (_) => AlertDialog(
            title: Center(
              child: Text(
                title,
                style: TextStyle(
                  fontFamily: 'Raleway',
                  fontSize: 23,
                  color: Colors.green[900],
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            content: Text(
              message,
              style: TextStyle(fontFamily: 'Lexend', fontSize: 17),
            ),
          ),
    );

    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) {
        Navigator.of(context, rootNavigator: true).pop(); // Đóng dialog
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Documentaries'),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child:
            isLoading
                ? const Center(child: CircularProgressIndicator())
                : error != null
                ? Center(
                  child: Text(
                    "Error: $error",
                    style: const TextStyle(color: Color(0xFFBB0000)),
                  ),
                )
                : Column(
                  children: [
                    Expanded(
                      child:
                          documents == null || documents!.isEmpty
                              ? const Center(
                                child: Text(
                                  "No document",
                                  style: TextStyle(
                                    fontFamily: 'Lexend',
                                    fontSize: 18,
                                    color: Colors.black54,
                                    fontStyle: FontStyle.italic,
                                  ),
                                ),
                              )
                              : ListView.builder(
                                itemCount: documents!.length,
                                itemBuilder: (context, index) {
                                  final doc = documents![index];
                                  return Container(
                                    margin: const EdgeInsets.symmetric(
                                      vertical: 8,
                                    ),
                                    decoration: BoxDecoration(
                                      color: Colors.white,
                                      border: Border.all(
                                        color: const Color(0xFF013171),
                                        width: 1.68,
                                      ),
                                      borderRadius: BorderRadius.circular(12),
                                      boxShadow: [
                                        BoxShadow(
                                          color: Colors.black.withOpacity(0.17),
                                          blurRadius: 2,
                                          offset: const Offset(2, 6),
                                        ),
                                      ],
                                    ),
                                    child: ListTile(
                                      title: Text(
                                        doc['documentType'] ?? 'Unknown Type',
                                      ),
                                      subtitle: Text(
                                        _maskDocNumber(doc['documentNumber']) +
                                            ' - Expired Date: ' +
                                            (doc['expiryDate'] ??
                                                'Unknown Expiration Date'),
                                      ),
                                      trailing: IconButton(
                                        icon: Icon(
                                          Icons.delete,
                                          color: Color(0xFFBB0000),
                                        ),
                                        onPressed: () async {
                                          final confirm = await showDialog<
                                            bool
                                          >(
                                            context: context,
                                            builder:
                                                (ctx) => AlertDialog(
                                                  title: Center(
                                                    child: Text(
                                                      "Delete Vehicle",
                                                      style: TextStyle(
                                                        fontFamily: "Raleway",
                                                        fontSize: 26,
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        color: Color(
                                                          0xFF013171,
                                                        ),
                                                      ),
                                                    ),
                                                  ),
                                                  content: Text(
                                                    "Do you want to delete documents with plate no ${doc['documentType']}?",
                                                    style: TextStyle(
                                                      fontFamily: "Lexend",
                                                      fontSize: 15,
                                                    ),
                                                  ),
                                                  actions: [
                                                    TextButton(
                                                      onPressed:
                                                          () => Navigator.pop(
                                                            ctx,
                                                            false,
                                                          ),
                                                      child: Text(
                                                        "Cancel",
                                                        style: TextStyle(
                                                          color:
                                                              Colors.blue[900],
                                                          fontSize: 16,
                                                          fontWeight:
                                                              FontWeight.bold,
                                                        ),
                                                      ),
                                                    ),
                                                    TextButton(
                                                      onPressed:
                                                          () => Navigator.pop(
                                                            ctx,
                                                            true,
                                                          ),
                                                      child: const Text(
                                                        "Delete",
                                                        style: TextStyle(
                                                          color: Color(
                                                            0xFFBB0000,
                                                          ),
                                                          fontSize: 16,
                                                          fontWeight:
                                                              FontWeight.bold,
                                                        ),
                                                      ),
                                                    ),
                                                  ],
                                                ),
                                          );

                                          if (confirm == true) {
                                            try {
                                              await CustomerService.deleteDocument(
                                                doc['documentId'],
                                              ); // id phải đúng key
                                              await fetchDocuments(); // cập nhật danh sách sau khi xóa
                                              _showDialog(
                                                "Success",
                                                "Delete document successfully!",
                                              );
                                            } catch (e) {
                                              _showDialog(
                                                "Fail",
                                                "Delete document failed!",
                                              );
                                            }
                                          }
                                        },
                                      ),
                                      onTap: () async {
                                        await PersistentNavBarNavigator.pushNewScreen(
                                          context,
                                          screen: DocumentaryDetailPage(
                                            document: doc,
                                          ),
                                          withNavBar: true,
                                          pageTransitionAnimation:
                                              PageTransitionAnimation.cupertino,
                                        ).then((result) async {
                                          if (result == true) {
                                            await fetchDocuments();
                                          }
                                        });
                                      },
                                    ),
                                  );
                                },
                              ),
                    ),
                    const SizedBox(height: 20),
                    TextButton.icon(
                      onPressed: () async {
                        await PersistentNavBarNavigator.pushNewScreen(
                          context,
                          screen: NewDocumentaryPage(),
                          withNavBar: true,
                          pageTransitionAnimation:
                              PageTransitionAnimation.cupertino,
                        ).then((result) async {
                          if (result == true) {
                            await fetchDocuments();
                          }
                        });
                      },
                      icon: Container(
                        decoration: const BoxDecoration(shape: BoxShape.circle),
                        padding: const EdgeInsets.all(4),
                        child: Image.asset(
                          'assets/icons/plus_red.png',
                          width: 22,
                          height: 20,
                          fit: BoxFit.contain,
                        ),
                      ),
                      label: Text(
                        "Add New Document",
                        style: TextStyle(
                          color: Color(0xFFBB0000),
                          fontWeight: FontWeight.bold,
                          fontSize: 18,
                        ),
                      ),
                      style: TextButton.styleFrom(
                        foregroundColor: Color(0xFF013171),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                  ],
                ),
      ),
    );
  }
}
