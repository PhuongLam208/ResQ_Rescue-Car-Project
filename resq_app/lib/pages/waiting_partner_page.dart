import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/pages/tracking_page.dart';
import 'package:resq_app/services/rescue_api_service.dart';
import 'package:resq_app/services/rescue_socket_service.dart';
import 'package:resq_app/screens/main_screens.dart';

class WaitingPartnerPage extends StatefulWidget {
  final LatLng startLocation;
  final LatLng? destinationLocation;
  final List<LatLng>? routePoints;
  final int rrid;

  const WaitingPartnerPage({
    super.key,
    required this.startLocation,
    this.destinationLocation,
    this.routePoints,
    required this.rrid,
  });

  @override
  State<WaitingPartnerPage> createState() => _WaitingPartnerPageState();
}

class _WaitingPartnerPageState extends State<WaitingPartnerPage> {
  late RescueSocketService _socketService;
  bool _isLoading = false;
  bool _isConnected = false;

  final apiService = RescueApiService();

  @override
  void initState() {
    super.initState();
    _socketService = RescueSocketService(
      partnerId: 0, // ID partner của bạn ở đây,
      onRescueRequestReceived: _handleRescueRequest,
    );
    _socketService.connect();
    _dispatchRequest();
  }

  Future<void> _dispatchRequest() async {
    setState(() => _isLoading = true);
    try {
      final dispatched = await apiService.dispatchRescueRequest(
        widget.rrid,
        widget.startLocation.latitude,
        widget.startLocation.longitude,
      );
      print('Rescue request dispatched successfully: $dispatched');
    } catch (e) {
      print('Error dispatching rescue request: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _handleRescueRequest(Map<String, dynamic> message) {
    final type = message['type'];

    switch (type) {
      case 'PARTNER_ASSIGNED':
        _onPartnerAssigned(message);
        break;
      case 'PARTNER_ACCEPTED':
        _onPartnerAccepted(message);
        break;
      case 'REQUEST_CANCELLED':
        _onRequestCancelled(message);
        break;
      case 'REQUEST_TIMEOUT':
        _onRequestTimeout(message);
        break;
    }
  }

  void _onPartnerAssigned(Map<String, dynamic> data) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text("Partner assigned! Waiting for confirmation..."),
        backgroundColor: Colors.orange,
        duration: Duration(seconds: 2),
      ),
    );
  }

  void _onPartnerAccepted(Map<String, dynamic> data) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text("Partner found! Starting rescue..."),
        backgroundColor: Colors.green,
        duration: Duration(seconds: 2),
      ),
    );

    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder:
            (context) => TrackingPage(
              rrid: widget.rrid,
              startLocation: widget.startLocation,
              destinationLocation: widget.destinationLocation,
              // routePoints: widget.routePoints,
              // isPartnerView: false,
            ),
      ),
    );
  }

  void _onRequestCancelled(Map<String, dynamic> data) {
    if (!mounted) return;
    setState(() => _isLoading = false);
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text("Request has been cancelled."),
        backgroundColor: Colors.red,
        duration: Duration(seconds: 3),
      ),
    );
    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) {
        Navigator.of(context).popUntil((route) => route.isFirst);
      }
    });
  }

  void _onRequestTimeout(Map<String, dynamic> data) {
    if (!mounted) return;
    setState(() => _isLoading = false);
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text(
          "No partners available at the moment. Please try again later.",
        ),
        backgroundColor: Colors.red,
        duration: Duration(seconds: 4),
      ),
    );
    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) {
        Navigator.of(context).popUntil((route) => route.isFirst);
      }
    });
  }

  void _showCancelDialog(BuildContext context) {
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (context) {
        return AlertDialog(
          title: const Text("CANCEL TRIP"),
          content: const Text(
            "Are you sure you want to cancel the rescue trip?",
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text("NO"),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
              onPressed: () async {
                Navigator.of(context).pop();
                await _cancelRescueRequest();
              },
              child: const Text("CANCEL"),
            ),
          ],
        );
      },
    );
  }

  Future<void> _cancelRescueRequest() async {
    try {
      String message = await apiService.cancelRescueRequest(widget.rrid);
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(message)));
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(
          builder: (_) => MainScreen(userId: loginResponse!.userId),
        ),
        (route) => false,
      );
    } catch (e) {
      print('Error cancelling request: $e');
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Error cancelling request: $e')));
    }
  }

  @override
  void dispose() {
    _socketService.disconnect(); // Ngắt kết nối STOMP
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final LatLng initialFocus =
        widget.destinationLocation ?? widget.startLocation;

    return Scaffold(
      resizeToAvoidBottomInset: false,
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        title: const Text("Finding Partner"),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16.0),
            child: Icon(
              _isConnected ? Icons.wifi : Icons.wifi_off,
              color: _isConnected ? Colors.green : Colors.red,
            ),
          ),
        ],
      ),
      body: Stack(
        children: [
          FlutterMap(
            options: MapOptions(initialCenter: initialFocus, initialZoom: 14),
            children: [
              TileLayer(
                urlTemplate:
                    'https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoidHJhbXRyYW4xMjMiLCJhIjoiY21kNGRkMHQ0MGY2NTJscjZmcDY4bzVuNCJ9.L4-zGwpDVXx9aKqTqbDyvA',
                userAgentPackageName: 'com.example.resq_app',
              ),
              MarkerLayer(
                markers: [
                  Marker(
                    point: widget.startLocation,
                    width: 40,
                    height: 40,
                    child: const Icon(
                      Icons.location_pin,
                      color: Color(0xFF073356),
                      size: 40,
                    ),
                  ),
                  if (widget.destinationLocation != null)
                    Marker(
                      point: widget.destinationLocation!,
                      width: 40,
                      height: 40,
                      child: const Icon(
                        Icons.location_pin,
                        color: Color(0xFFBB1105),
                        size: 40,
                      ),
                    ),
                ],
              ),
              if (widget.routePoints != null && widget.routePoints!.isNotEmpty)
                PolylineLayer(
                  polylines: [
                    Polyline(
                      points: widget.routePoints!,
                      color: const Color(0xFF073356),
                      strokeWidth: 4,
                    ),
                  ],
                ),
            ],
          ),
          if (_isLoading)
            Positioned.fill(
              child: Container(
                color: Colors.black.withOpacity(0.3),
                child: const Center(child: CircularProgressIndicator()),
              ),
            ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: const BorderRadius.vertical(
                  top: Radius.circular(16),
                ),
                boxShadow: const [
                  BoxShadow(color: Colors.black26, blurRadius: 8),
                ],
              ),
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Stack(
                    alignment: Alignment.center,
                    children: [
                      const Positioned(
                        top: 0,
                        left: 20,
                        child: Icon(Icons.wifi, color: Colors.blue, size: 24),
                      ),
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.blue,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(
                          Icons.directions_car,
                          color: Colors.white,
                          size: 32,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    "Looking for rescue driver...",
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                      color: Colors.black87,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    _isConnected ? "Please wait..." : "Connecting...",
                    style: const TextStyle(fontSize: 14, color: Colors.grey),
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton(
                      onPressed: () => _showCancelDialog(context),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.red,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        elevation: 0,
                      ),
                      child: const Text(
                        "Cancel Trip",
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
