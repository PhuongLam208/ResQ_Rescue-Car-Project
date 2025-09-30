import 'package:flutter/material.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:resq_app/screens/customer/vehicles/new_vehicle_page.dart';
import 'package:resq_app/screens/customer/vehicles/vehicle_detail_page.dart';

class VehiclesPage extends StatefulWidget {
  const VehiclesPage({super.key});

  @override
  State<VehiclesPage> createState() => _VehiclesPageState();
}

class _VehiclesPageState extends State<VehiclesPage> {
  int? userId = loginResponse?.userId;
  List<dynamic>? vehicles;
  String? error;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    fetchVehicles();
  }

  Future<void> fetchVehicles() async {
    try {
      final result = await CustomerService.getCustomerVehicles(userId!);
      setState(() {
        vehicles = result;
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
      appBar: AppBar(
        backgroundColor: Color(0xFF013171),
        title: const Text(
          "Vehicles",
          style: TextStyle(
            fontFamily: 'Raleway',
            fontWeight: FontWeight.w700,
            color: Colors.white,
            fontSize: 24,
          ),
        ),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
      ),
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
                          vehicles == null || vehicles!.isEmpty
                              ? const Center(
                                child: Text(
                                  "No vehicle",
                                  style: TextStyle(
                                    fontFamily: 'Lexend',
                                    fontSize: 18,
                                    color: Colors.black54,
                                    fontStyle: FontStyle.italic,
                                  ),
                                ),
                              )
                              : ListView.builder(
                                itemCount: vehicles!.length,
                                itemBuilder: (context, index) {
                                  final vehicle = vehicles![index];
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
                                        vehicle['plateNo'] ??
                                            'Unknown Plate No',
                                      ),
                                      subtitle: Text(
                                        (vehicle['brand'] ?? 'Unknown Brand') +
                                            ' ' +
                                            (vehicle['model'] ??
                                                'Unknown Model') +
                                            ' ' +
                                            (vehicle['year'].toString() ??
                                                'Unknown Year'),
                                      ),
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
                                                    "Do you want to delete vehicle with plate no ${vehicle['plateNo']}?",
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
                                              await CustomerService.deleteVehicle(
                                                vehicle['vehicleId'],
                                              ); // id phải đúng key
                                              await fetchVehicles(); // cập nhật danh sách sau khi xóa
                                              _showDialog(
                                                "Success",
                                                "Delete vehicle successfully!",
                                              );
                                            } catch (e) {
                                              _showDialog(
                                                "Fail",
                                                "Delete vehicle failed!",
                                              );
                                            }
                                          }
                                        },
                                      ),
                                      onTap: () async {
                                        await PersistentNavBarNavigator.pushNewScreen(
                                          context,
                                          screen: VehicleDetailPage(
                                            vehicle: vehicle,
                                          ),
                                          withNavBar: true,
                                          pageTransitionAnimation:
                                              PageTransitionAnimation.cupertino,
                                        ).then((result) async {
                                          if (result == true) {
                                            await fetchVehicles();
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
                          screen: NewVehiclePage(),
                          withNavBar: true,
                          pageTransitionAnimation:
                              PageTransitionAnimation.cupertino,
                        ).then((result) async {
                          if (result == true) {
                            await fetchVehicles();
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
                        "Add New Vehicle",
                        style: TextStyle(
                          color: Colors.red[900],
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
