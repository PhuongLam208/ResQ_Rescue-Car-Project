import 'package:flutter/material.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:resq_app/screens/payment/new_payment_page.dart';
import 'package:resq_app/screens/payment/payment_detail_page.dart';

class PaymentsPage extends StatefulWidget {
  const PaymentsPage({super.key});

  @override
  State<PaymentsPage> createState() => _PaymentsPageState();
}

class _PaymentsPageState extends State<PaymentsPage> {
  int? userId = loginResponse?.userId;
  List<dynamic>? payments;
  String? error;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    fetchPayments();
  }

  Future<void> fetchPayments() async {
    try {
      final result = await CustomerService.getPayments(userId!);
      setState(() {
        payments = result;
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        error = e.toString();
        isLoading = false;
      });
    }
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
              style: const TextStyle(fontFamily: 'Lexend', fontSize: 17),
            ),
          ),
    );

    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) {
        Navigator.of(context, rootNavigator: true).pop();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Payments'),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child:
            isLoading
                ? const Center(child: CircularProgressIndicator())
                : error != null
                ? Center(
                  child: Text(
                    "Error: $error",
                    style: const TextStyle(color: Colors.red),
                  ),
                )
                : Column(
                  children: [
                    Expanded(
                      child:
                          payments == null || payments!.isEmpty
                              ? const Center(
                                child: Text(
                                  "No payment",
                                  style: TextStyle(
                                    fontFamily: 'Lexend',
                                    fontSize: 18,
                                    color: Colors.black54,
                                    fontStyle: FontStyle.italic,
                                  ),
                                ),
                              )
                              : ListView.builder(
                                itemCount: payments!.length,
                                itemBuilder: (context, index) {
                                  final pay = payments![index];
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
                                        pay['method'] ?? 'Unknown method',
                                      ),
                                      subtitle: Text(pay['paypalEmail']),
                                      trailing: IconButton(
                                        icon: Icon(
                                          Icons.delete,
                                          color: Colors.red[800],
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
                                                      "Delete Payment",
                                                      style: TextStyle(
                                                        fontFamily: "Raleway",
                                                        fontSize: 26,
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        color: const Color(
                                                          0xFF013171,
                                                        ),
                                                      ),
                                                    ),
                                                  ),
                                                  content: Text(
                                                    "Do you want to delete payment with email ${pay['paypalEmail']}?",
                                                    style: const TextStyle(
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
                                                          color: Colors.red,
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
                                              await CustomerService.deletePayment(
                                                pay['paymentId'],
                                              );
                                              await fetchPayments();
                                              _showDialog(
                                                "Success",
                                                "Delete payment successfully!",
                                              );
                                            } catch (e) {
                                              _showDialog(
                                                "Fail",
                                                "Delete payment failed!",
                                              );
                                            }
                                          }
                                        },
                                      ),
                                      onTap: () async {
                                        await PersistentNavBarNavigator.pushNewScreen(
                                          context,
                                          screen: PaymentDetailPage(
                                            payment: pay,
                                          ),
                                          withNavBar: true,
                                          pageTransitionAnimation:
                                              PageTransitionAnimation.cupertino,
                                        ).then((result) async {
                                          if (result == true) {
                                            await fetchPayments();
                                          }
                                        });
                                      },
                                    ),
                                  );
                                },
                              ),
                    ),
                    const SizedBox(height: 20),
                    if ((payments?.isEmpty ??
                        true)) // chỉ hiện khi chưa có payment
                      TextButton.icon(
                        onPressed: () async {
                          await PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: NewPayemtPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          ).then((result) async {
                            if (result == true) {
                              await fetchPayments();
                            }
                          });
                        },
                        icon: Container(
                          decoration: const BoxDecoration(
                            shape: BoxShape.circle,
                          ),
                          padding: const EdgeInsets.all(4),
                          child: Image.asset(
                            'assets/icons/plus_red.png',
                            width: 22,
                            height: 20,
                            fit: BoxFit.contain,
                          ),
                        ),
                        label: Text(
                          "Add New Payment",
                          style: TextStyle(
                            color: Colors.red[900],
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                          ),
                        ),
                        style: TextButton.styleFrom(
                          foregroundColor: const Color(0xFF013171),
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
