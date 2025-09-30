import 'dart:io';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:latlong2/latlong.dart';
import 'package:resq_app/models/rescue_notification_dto.dart';
import 'package:resq_app/models/rescue_service.dart';
import 'package:resq_app/models/sendrequestforpartner.dart';
import 'package:resq_app/pages/tracking_page.dart';
import 'package:resq_app/screens/chatbox/MessagesScreen.dart';
import 'package:resq_app/screens/partner/partner_type_screen.dart';
import 'package:resq_app/services/rescue_api_service.dart';
import 'package:resq_app/services/rescue_request_websocket.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'dart:convert';
import '../../config/app_config.dart';
import 'partner_document_screen.dart';
import 'PartnerServiceEditScreen.dart';
import 'package:resq_app/models/RRInfoDto.dart';
import 'package:resq_app/screens/partner/booking_partner.dart';
import 'package:resq_app/services/partner_service.dart';
import 'package:resq_app/services/api_result.dart';
import 'package:resq_app/screens/customer/home_profile.dart';
import 'package:resq_app/services/payment_service.dart';
import 'package:intl/intl.dart';

class PartnerDashboardScreen extends StatefulWidget {
  final int userId;

  const PartnerDashboardScreen({super.key, required this.userId});

  @override
  State<PartnerDashboardScreen> createState() => _PartnerDashboardScreenState();
}

class _PartnerDashboardScreenState extends State<PartnerDashboardScreen> {
  // String wsBaseUrl = 'ws://172.18.172.85:9090'; // dùng 'ws' thay cho 'http'
  Map<String, dynamic>? partnerData;
  bool isLoading = true;
  String? userAvatar;
  bool isAllow = false;

  StompClient? _stompClient;
  RescueRequestNotificationDto? currentRequest;
  bool _isDialogShowing = false;

  void _connectStompWebSocket() {
    if (_stompClient == null || !_stompClient!.connected) {
      _stompClient = StompClient(
        config: StompConfig.sockJS(
          url: '$wsBaseUrl/ws',
          onConnect: (StompFrame frame) {
            print(' STOMP connected');
            _stompClient?.subscribe(
              destination: '/topic/rescue/partner/${widget.userId}',
              callback: (StompFrame frame) {
                if (frame.body != null) {
                  print('Received STOMP frame: ${frame.body}');
                  try {
                    final data = json.decode(frame.body!);
                    if (mounted) {
                      setState(() {
                        // PARSE TRỰC TIẾP THÀNH ĐỐI TƯỢNG DTO
                        currentRequest = RescueRequestNotificationDto.fromJson(
                          data,
                        );
                      });
                      if (!_isDialogShowing) {
                        _showRescueDialog();
                      }
                    }
                  } catch (e) {
                    print('JSON parsing error: $e');
                  }
                }
              },
            );
          },
          onWebSocketError: (error) {
            print(' WebSocket error: $error');
          },
          onStompError: (frame) {
            print('STOMP error: ${frame.body}');
          },
          onDisconnect: (StompFrame frame) {
            print('STOMP disconnected');
          },
          heartbeatOutgoing: Duration(seconds: 10),
          heartbeatIncoming: Duration(seconds: 10),
        ),
      );
      try {
        _stompClient?.activate();
      } catch (e) {
        print('STOMP activation error: $e');
      }
    }
  }

  @override
  void initState() {
    super.initState();
    fetchUserAvatar();
    _loadPaymentData();

    fetchPartnerInfo().then((_) {
      _connectStompWebSocket();
    });
  }

  @override
  void dispose() {
    _stompClient?.deactivate();
    _stompClient = null;
    super.dispose();
  }
  // @override
  // void initState() {
  //   super.initState();
  //   fetchUserAvatar();
  //   _loadPaymentData();

  //   fetchPartnerInfo().then((_) {
  //     connectWebSocket();
  //     Future.doWhile(() async {
  //       await Future.delayed(const Duration(seconds: 10));
  //       if (mounted) {
  //         // await checkNewRescueRequest();
  //         return true;
  //       }
  //       return false;
  //     });
  //   });
  // }

  // Moved acceptRequest and denyRequest methods to this class
  Future<void> acceptRequest(int rrid) async {
    final url = Uri.parse(
      '$baseUrl/api/resq/pcrescue/accept?rrid=$rrid&userId=${widget.userId}',
    );
    final resp = await http.post(url);
    if (resp.statusCode == 200) {
      print("Accepted request $rrid");
      if (mounted) {
        setState(() => currentRequest = null);
      }
    } else {
      print(" Accept failed: ${resp.body}");
    }
  }

  Future<void> denyRequest(int rrid) async {
    final url = Uri.parse(
      '$baseUrl/api/resq/pcrescue/deny?rrid=$rrid&userId=${widget.userId}',
    );
    final resp = await http.post(url);
    if (resp.statusCode == 200) {
      print(" Denied request $rrid");
      if (mounted) {
        setState(() => currentRequest = null);
      }
    } else {
      print(" Deny failed: ${resp.body}");
    }
  }

  void _showRescueDialog() {
    if (currentRequest == null || !mounted) return;

    // Các điểm tọa độ cho TrackingPage
    LatLng userLocation = LatLng(
      currentRequest!.startLatitude, // Sử dụng thuộc tính DTO
      currentRequest!.startLongitude, // Sử dụng thuộc tính DTO
    );
    LatLng destinationLocation = LatLng(
      currentRequest!.endLatitude, // Sử dụng thuộc tính DTO
      currentRequest!.endLongitude, // Sử dụng thuộc tính DTO
    );
    // LẤY CÁC GIÁ TRỊ START/END CHO TRACKING PAGE TỪ currentRequest DTO
    LatLng trackingStartLocation = LatLng(
      currentRequest!.startLatitude, // Sử dụng thuộc tính DTO
      currentRequest!.startLongitude, // Sử dụng thuộc tính DTO
    );
    LatLng trackingEndLocation = LatLng(
      currentRequest!.endLatitude, // Sử dụng thuộc tính DTO
      currentRequest!.endLongitude, // Sử dụng thuộc tính DTO
    );

    _isDialogShowing = true;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder:
          (BuildContext dialogContext) => AlertDialog(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            title: Row(
              children: const [
                Icon(Icons.notification_important, color: Colors.redAccent),
                SizedBox(width: 8),
                Text(
                  'Request Rescue',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
              ],
            ),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildInfoRow(
                  ' From:',
                  currentRequest!.from,
                ), // Sử dụng thuộc tính DTO
                _buildInfoRow(
                  ' To:',
                  currentRequest!.to,
                ), // Sử dụng thuộc tính DTO
                _buildInfoRow(
                  'Type:',
                  currentRequest!.serviceType,
                ), // Sử dụng thuộc tính DTO
                // _buildInfoRow(
                //   '📏 Khoảng cách:',
                //   currentRequest!.distanceKm !=
                //           null // Kiểm tra nullable
                //       ? '${currentRequest!.distanceKm.toStringAsFixed(1)} km'
                //       : '‑',
                // ),
                _buildInfoRow(
                  ' Price:',
                  NumberFormat.currency(locale: 'vi_VN', symbol: '₫').format(
                    currentRequest!.finalPrice,
                  ), // Sử dụng thuộc tính DTO
                ),
                _buildInfoRow(
                  ' Payment Method:',
                  currentRequest!.paymentMethod, // Sử dụng thuộc tính DTO
                ),
                _buildInfoRow(
                  ' Message:',
                  currentRequest!.message,
                ), // Sử dụng thuộc tính DTO
              ],
            ),
            actions: [
              TextButton.icon(
                onPressed: () async {
                  try {
                    await denyRequest(
                      currentRequest!.rrid,
                    ); // Sử dụng thuộc tính DTO
                  } catch (e) {
                    print("Error on denyRequest: $e");
                  } finally {
                    if (mounted && _isDialogShowing) {
                      Navigator.pop(dialogContext);
                      _isDialogShowing = false;
                    }
                  }
                },
                icon: const Icon(Icons.close, color: Colors.grey),
                label: const Text('Deny'),
              ),
              ElevatedButton.icon(
                onPressed: () async {
                  try {
                    await acceptRequest(
                      currentRequest!.rrid,
                    ); // Sử dụng thuộc tính DTO
                    if (mounted && _isDialogShowing) {
                      Navigator.pop(dialogContext);
                      _isDialogShowing = false;
                    }

                    if (mounted) {
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(
                          builder:
                              (context) => TrackingPage(
                                rrid:
                                    currentRequest!
                                        .rrid, // Sử dụng thuộc tính DTO
                                startLocation: trackingStartLocation,
                                destinationLocation: trackingEndLocation,
                                // isPartnerView: true,
                                rescueData: currentRequest,
                              ),
                        ),
                      );
                    }
                  } catch (e) {
                    print("Error on acceptRequest: $e");
                    if (mounted && _isDialogShowing) {
                      Navigator.pop(dialogContext);
                      _isDialogShowing = false;
                    }
                  }
                },
                icon: const Icon(Icons.check, color: Colors.white),
                label: const Text('Accept'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
              ),
            ],
          ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(width: 8),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }

  void _partnerWithdraw() async {
    final int partnerId = 1;
    const amount = 3500000 - 500000;
    final formattedAmount = NumberFormat('#,###', 'en_US').format(amount);

    if (amount <= 50000) {
      _showDialog(
        "Insufficient Balance",
        "Your wallet must have at least 50,000 VND to maintain operations.",
      );
      return;
    }
    final bool? confirm = await showDialog<bool>(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text(
              'Confirm Withdraw',
              style: TextStyle(
                fontFamily: "Raleway",
                fontSize: 24,
                fontWeight: FontWeight.w500,
                color: Color(0xFF013171),
              ),
            ),
            content: Text(
              'Do you want to withdraw $formattedAmount VND?',
              style: TextStyle(fontFamily: "Lexend", fontSize: 15),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text(
                  'Cancel',
                  style: TextStyle(
                    color: Colors.red,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              ElevatedButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: Text(
                  'Confirm',
                  style: TextStyle(
                    color: Color(0xFF013171),
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
    );

    if (confirm != true) return;

    _showLoading();

    final paymentResult = await PaymentService.payToPartner(partnerId);

    if (mounted) Navigator.pop(context);

    if (paymentResult) {
      final updateWallet = await PartnerService.updateWalletPoint(partnerId);
      if (updateWallet) {
        _showDialog("Withdraw Success", "Your withdraw is successful!");
        // Cập nhật lại trang
      }
    } else {
      _showDialog(
        "Withdraw Failed",
        "We couldn't process your withdrawal request. Please try again later.",
      );
    }
  }

  //Loading while run
  void _showLoading() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );
  }

  //Show noti
  void _showDialog(String title, String message) {
    final isFailed =
        title.toLowerCase().contains("failed") ||
        title.toLowerCase().contains("insufficient");
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
                  color: isFailed ? Colors.red[700] : Colors.green[900],
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

    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        Navigator.of(context, rootNavigator: true).pop();
      }
    });
  }

  Future<void> _loadPaymentData() async {
    final data = await PartnerService.getPartnerPaypalPayment(widget.userId);
    if (data != null) {
      setState(() {
        isAllow = true;
        print(isAllow);
      });
    } else {
      setState(() {
        isAllow = false;
      });
    }
  }

  String getFixedUrl(String url) {
    return url.replaceFirst("http://localhost", "$baseUrl");
  }

  Future<void> fetchPartnerInfo() async {
    try {
      final response = await http.get(
        Uri.parse('$partnerUrl/info?userId=${widget.userId}'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        setState(() {
          partnerData = json.decode(response.body);
          isLoading = false;
        });
      } else {
        setState(() {
          partnerData = {
            'resFix': {'status': 0, 'tag': 'Not registered'},
            'resTow': {'status': 0, 'tag': 'Not registered'},
            'resDrive': {'status': 0, 'tag': 'Not registered'},
          };
          isLoading = false;
        });
      }
    } catch (e) {
      print("Error fetching partner info: $e");
      setState(() => isLoading = false);
    }
  }

  Future<void> fetchUserAvatar() async {
    try {
      final response = await http.get(
        Uri.parse('$partnerUrl/user-avatar?userId=${widget.userId}'),
        headers: headers,
      );
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        final rawAvatar = data['avatar'] as String?;

        if (rawAvatar != null && rawAvatar.isNotEmpty) {
          final fullAvatar =
              rawAvatar.startsWith('http') ? rawAvatar : '$baseUrl$rawAvatar';

          setState(() {
            userAvatar = fullAvatar;
          });
        }
      }
    } catch (e) {
      print("Avatar error: $e");
    }
  }

  Widget buildAvatar() {
    if (userAvatar != null && userAvatar!.isNotEmpty) {
      return CircleAvatar(
        radius: 50,
        backgroundImage: NetworkImage(userAvatar!),
      );
    } else {
      return const CircleAvatar(
        radius: 50,
        backgroundImage: AssetImage('images/partner.png'),
      );
    }
  }

  Future<List<Map<String, dynamic>>> fetchDocuments(int userId) async {
    final url = '$partnerUrl/documents?userId=$userId';
    final response = await http.get(Uri.parse(url), headers: headers);

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      print("Document data: $data");
      List<Map<String, dynamic>> result = [];

      final documentTypes = {
        'resFix': 'Fixing',
        'resTow': 'Towing',
        'resDrive': 'Driving',
      };

      for (var resTypeKey in documentTypes.keys) {
        final docList = data[resTypeKey];
        if (docList == null || docList is! List) continue;

        for (var doc in docList) {
          if (doc is Map<String, dynamic>) {
            final String? number = doc['number'];
            final String? type = doc['type'];
            final String frontImage = getFixedUrl(doc['frontImage'] ?? '');
            final String backImage = getFixedUrl(doc['backImage'] ?? '');

            result.add({
              'id': doc['id'],
              'type': '${documentTypes[resTypeKey]} - ${type ?? "Unknown"}',
              'number': number,
              'frontUrl': frontImage,
              'backUrl': backImage,
              'expired': doc['expired'] ?? false,
              'status': doc['status'],
            });
          }
        }
      }

      return result;
    } else {
      throw Exception('Error fetching documents: ${response.statusCode}');
    }
  }

  Future<void> cancelPartnerType(String typeKey) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Confirmation'),
            content: Text(
              'Are you sure you want to cancel $typeKey registration?',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('No'),
              ),
              TextButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('Yes'),
              ),
            ],
          ),
    );

    if (confirm != true) return;

    final url = Uri.parse(
      '$partnerUrl/cancel?userId=${widget.userId}&type=$typeKey',
    );

    try {
      final response = await http.post(url, headers: headers);
      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('✅ Successfully cancelled registration for $typeKey'),
          ),
        );
        await fetchPartnerInfo();
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('❌ Error cancelling $typeKey: ${response.body}'),
          ),
        );
      }
    } catch (e) {
      print("❌ Cancel error: $e");
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Connection error occurred')),
      );
    }
  }

  Future<void> toggleOnlineStatus(bool newStatus) async {
    final url = Uri.parse(
      '$partnerUrl/online?userId=${widget.userId}&status=$newStatus',
    );

    try {
      final response = await http.put(url, headers: headers);
      if (response.statusCode == 200) {
        setState(() {
          partnerData?['isOnline'] = newStatus;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Status updated: ${newStatus ? "Online" : "Offline"}',
            ),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to update online status')),
        );
      }
    } catch (e) {
      print("❌ Online status error: $e");
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Server connection error')));
    }
  }

  Widget buildTag(String tag) {
    Color bgColor;
    switch (tag) {
      case "Active":
        bgColor = Colors.green;
        break;
      case "Pending":
        bgColor = Colors.orange;
        break;
      case "Rejected":
        bgColor = Color(0xFFBB0000);
        break;
      default:
        bgColor = Colors.grey;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        tag,
        style: const TextStyle(color: Colors.white, fontSize: 12),
      ),
    );
  }

  // Future<void> checkNewRescueRequest() async {
  //   final int? partnerId = partnerData?['partnerId'];
  //   if (partnerId == null) return;

  //   // final rrInfo = await PartnerService().fetchNewRescueRequest(partnerId);
  //   if (rrInfo != null) {
  //     showNewRescuePopup(rrInfo, partnerId);
  //   }
  // }

  void showNewRescuePopup(RRInfoDto rrInfo, int partnerId) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                "You have a new rescue request!",
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 18,
                  color: Color(0xFF013171),
                ),
              ),
              const SizedBox(height: 12),
              Align(
                alignment: Alignment.centerLeft,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "+ Description: ${rrInfo.description}",
                      style: const TextStyle(fontSize: 16),
                    ),
                    Text(
                      "+ Customer's Payment Method: ${rrInfo.paymentMethod} ",
                      style: const TextStyle(fontSize: 16),
                    ),
                    Text(
                      "+ Estimated reward: ${rrInfo.total.toStringAsFixed(0)} VND",
                      style: const TextStyle(fontSize: 16),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // ElevatedButton(
                  //   style: ElevatedButton.styleFrom(
                  //     backgroundColor: const Color(0xFFBB0000),
                  //   ),
                  //   onPressed: () async {
                  //     ApiResult result = await PartnerService().partnerDenied(
                  //       rrInfo.rrId,
                  //     );
                  //     if (context.mounted) Navigator.pop(context);
                  //     ScaffoldMessenger.of(
                  //       context,
                  //     ).showSnackBar(SnackBar(content: Text(result.message)));
                  //   },
                  //   child: const Text(
                  //     "Decline",
                  //     style: TextStyle(color: Colors.white),
                  //   ),
                  // ),
                  const SizedBox(width: 20),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF013171),
                    ),
                    onPressed: () {
                      Navigator.pop(context);
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder:
                              (context) => BookingPartnerScreen(
                                rrId: rrInfo.rrId,
                                partnerId: partnerId,
                                paymentMethod: rrInfo.paymentMethod,
                              ),
                        ),
                      );
                    },
                    child: const Text(
                      "Accept",
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  String _formatAmount(dynamic amount) {
    if (amount == null) return '0';
    try {
      final num value = num.parse(amount.toString());
      return value.toStringAsFixed(0);
    } catch (_) {
      return '0';
    }
  }

  @override
  Widget build(BuildContext context) {
    final items = [
      {'label': 'ResFix', 'icon': Icons.build, 'key': 'resFix'},
      {'label': 'ResTow', 'icon': Icons.local_shipping, 'key': 'resTow'},
      {'label': 'ResDrive', 'icon': Icons.drive_eta, 'key': 'resDrive'},
    ];

    return Scaffold(
      appBar: const CommonAppBar(title: 'Partner Dashboard'),
      body:
          isLoading
              ? const Center(child: CircularProgressIndicator())
              : SingleChildScrollView(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 20,
                ),
                child: Column(
                  children: [
                    buildAvatar(),
                    const SizedBox(height: 12),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Wallet Amount: ${_formatAmount(partnerData?['walletAmount'])} VND',
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w500,
                            color: Color(0xFF013171),
                          ),
                        ),
                        TextButton(
                          onPressed: _partnerWithdraw,
                          style: TextButton.styleFrom(
                            backgroundColor: Color(0xFF013171),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(
                              horizontal: 24,
                              vertical: 12,
                            ),
                          ),
                          child: const Text("Withdraw"),
                        ),
                      ],
                    ),

                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          partnerData?['isOnline'] == true
                              ? 'Online'
                              : 'Offline',
                        ),
                        const SizedBox(width: 8),
                        Switch(
                          value: partnerData?['isOnline'] == true,
                          onChanged: toggleOnlineStatus,
                          activeColor: Color(0xFF013171),
                        ),
                      ],
                    ),
                    const Divider(height: 32, thickness: 1.2),
                    Column(
                      children:
                          items.map((item) {
                            final statusMap =
                                partnerData?[item['key']]
                                    as Map<String, dynamic>? ??
                                {};
                            final String tag =
                                statusMap['tag'] ?? 'Not registered';
                            final int status = statusMap['status'] ?? 0;

                            return Card(
                              margin: const EdgeInsets.only(bottom: 16),
                              elevation: 2,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: ListTile(
                                contentPadding: const EdgeInsets.symmetric(
                                  horizontal: 16,
                                  vertical: 12,
                                ),
                                leading: Icon(
                                  item['icon'] as IconData,
                                  color: Color(0xFF013171),
                                ),
                                title: Text(
                                  item['label'] as String,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                subtitle: buildTag(tag),
                                trailing:
                                    status != 0
                                        ? IconButton(
                                          icon: const Icon(
                                            Icons.cancel,
                                            color: Color(0xFFBB0000),
                                          ),
                                          tooltip: 'Cancel Registration',
                                          onPressed:
                                              () => cancelPartnerType(
                                                item['key'] as String,
                                              ),
                                        )
                                        : null,
                                onTap: () {
                                  if (status == 0) {
                                    Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder:
                                            (_) => PartnerTypeScreen(
                                              userId: widget.userId,
                                            ),
                                      ),
                                    );
                                  } else if (status == 1) {
                                    Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder:
                                            (_) => PartnerServiceEditScreen(
                                              userId: widget.userId,
                                              partnerType:
                                                  item['key'] as String,
                                            ),
                                      ),
                                    );
                                  } else if (status == 2) {
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      const SnackBar(
                                        content: Text(
                                          "Pending approval, please wait",
                                        ),
                                      ),
                                    );
                                  } else if (status == 4) {
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      const SnackBar(
                                        content: Text(
                                          "Rejected, please register again",
                                        ),
                                      ),
                                    );
                                  }
                                },
                              ),
                            );
                          }).toList(),
                    ),
                    const Divider(thickness: 1.2),
                    ListTile(
                      leading: const Icon(Icons.document_scanner_outlined),
                      textColor: Color(0xFF013171),
                      title: const Text("Legal Documents"),
                      onTap: () async {
                        try {
                          final documents = await fetchDocuments(widget.userId);
                          await Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder:
                                  (_) => PartnerDocumentScreen(
                                    userId: widget.userId,
                                    documents: documents,
                                  ),
                            ),
                          );
                          await fetchPartnerInfo();
                        } catch (e) {
                          print("❌ Error fetching documents: $e");
                        }
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.message_outlined),
                      textColor: Color(0xFF013171),
                      title: const Text("Messages"),
                      onTap: () {
                        final int? partnerId = partnerData?['partnerId'];
                        if (partnerId != null) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder:
                                  (_) => MessagesScreen(userId: widget.userId),
                            ),
                          );
                        } else {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text("Failed to retrieve partnerId"),
                            ),
                          );
                        }
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.swap_horiz),
                      textColor: Color(0xFF013171),
                      title: const Text("Switch to Customer Interface"),
                      onTap: () {
                        Navigator.pushReplacement(
                          context,
                          MaterialPageRoute(
                            builder: (_) => const HomeProfilePage(),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ),
    );
  }
}
