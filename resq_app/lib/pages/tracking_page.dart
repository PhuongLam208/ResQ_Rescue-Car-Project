import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

// Make sure these imports are correct for your project
import 'package:resq_app/config/app_config.dart'; // Contains mapboxToken and baseUrl
import 'package:resq_app/models/rescue_notification_dto.dart'; // Your DTO for initial request data

class TrackingPage extends StatefulWidget {
  final int rrid;
  // We'll primarily use rescueData for locations, but keep these for flexibility
  final LatLng startLocation;
  final LatLng? destinationLocation;
  final RescueRequestNotificationDto? rescueData; // Initial request data

  const TrackingPage({
    super.key,
    required this.rrid,
    required this.startLocation, // This might be overridden by rescueData if available
    this.destinationLocation, // This might be overridden by rescueData if available
    this.rescueData,
  });

  @override
  State<TrackingPage> createState() => _TrackingPageState();
}

class _TrackingPageState extends State<TrackingPage> {
  List<LatLng> routePoints = [];
  MapController mapController = MapController();
  bool isLoadingRoute = false;

  // Actual LatLngs to be used for map display, derived from widget.rescueData if present
  late LatLng _actualStartLocation;
  late LatLng? _actualDestinationLocation;

  @override
  void initState() {
    super.initState();
    _initializeLocations(); // Initialize locations based on available data
    _fetchAndGenerateRoute();
  }

  void _initializeLocations() {
    // Prioritize locations from rescueData if available and valid
    if (widget.rescueData != null &&
        widget.rescueData!.startLatitude != null &&
        widget.rescueData!.startLongitude != null) {
      _actualStartLocation = LatLng(
        widget.rescueData!.startLatitude!,
        widget.rescueData!.startLongitude!,
      );
      print('DEBUG: Start location from rescueData: $_actualStartLocation');
    } else {
      _actualStartLocation = widget.startLocation;
      print(
        'DEBUG: Start location from widget.startLocation: $_actualStartLocation',
      );
    }

    // Determine actual destination location - assuming your DTO has toLatitude/toLongitude
    if (widget.rescueData != null &&
        widget.rescueData!.endLatitude != null &&
        widget.rescueData!.endLongitude != null) {
      _actualDestinationLocation = LatLng(
        widget.rescueData!.endLatitude!,
        widget.rescueData!.endLongitude!,
      );
      print(
        'DEBUG: Destination location from rescueData: $_actualDestinationLocation',
      );
    } else {
      _actualDestinationLocation = widget.destinationLocation;
      print(
        'DEBUG: Destination location from widget.destinationLocation: $_actualDestinationLocation',
      );
    }

    // Fallback if destination is still null (e.g., for onsite repair)
    if (_actualDestinationLocation == null) {
      print(
        'DEBUG: No explicit destination location found. Assuming onsite repair or missing destination data.',
      );
    }
  }

  // Fetches the route between start and destination
  Future<void> _fetchAndGenerateRoute() async {
    if (_actualDestinationLocation == null) {
      // No destination, so no route to generate
      setState(() {
        routePoints = [];
      });
      print('DEBUG: No destination, skipping route generation.');
      return;
    }

    setState(() {
      isLoadingRoute = true;
      routePoints = []; // Clear previous route
    });

    // Ensure mapboxToken is correctly accessed
    if (mapboxToken.isEmpty) {
      _showSnackbar('Error: Mapbox Token is not configured.');
      print('ERROR: Mapbox Token is empty or null.');
      setState(() {
        isLoadingRoute = false;
      });
      return;
    }

    print(
      'DEBUG: Attempting to fetch route from: ${_actualStartLocation.longitude},${_actualStartLocation.latitude} to ${_actualDestinationLocation!.longitude},${_actualDestinationLocation!.latitude}',
    );

    try {
      final response = await http.get(
        Uri.parse(
          'https://api.mapbox.com/directions/v5/mapbox/driving/${_actualStartLocation.longitude},${_actualStartLocation.latitude};${_actualDestinationLocation!.longitude},${_actualDestinationLocation!.latitude}?geometries=geojson&access_token=$mapboxToken',
        ),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = json.decode(response.body);
        if (data['routes'] != null && data['routes'].isNotEmpty) {
          final List<dynamic> coordinates =
              data['routes'][0]['geometry']['coordinates'];
          setState(() {
            routePoints =
                coordinates
                    .map<LatLng>((coord) => LatLng(coord[1], coord[0]))
                    .toList();
          });
          print(
            'DEBUG: Route fetched successfully with ${routePoints.length} points.',
          );
          _zoomToFitRoute();
        } else {
          print('DEBUG: Mapbox API response: No routes found.');
          _showSnackbar('No route found. Please check points.');
          setState(() {
            routePoints = _generateStraightLineRoute(
              _actualStartLocation,
              _actualDestinationLocation!,
            );
          });
        }
      } else {
        print(
          'ERROR: Failed to load route: ${response.statusCode} - ${response.body}',
        );
        _showSnackbar('Failed to load route: ${response.statusCode}');
        // Fallback to a straight line if API fails
        setState(() {
          routePoints = _generateStraightLineRoute(
            _actualStartLocation,
            _actualDestinationLocation!,
          );
        });
      }
    } catch (e) {
      print('ERROR: Error fetching route from Mapbox: $e');
      _showSnackbar('Error fetching route: $e');
      // Fallback to a straight line if any error occurs
      setState(() {
        routePoints = _generateStraightLineRoute(
          _actualStartLocation,
          _actualDestinationLocation!,
        );
      });
    } finally {
      setState(() {
        isLoadingRoute = false;
      });
    }
  }

  // Generates a straight line between two points as a fallback
  List<LatLng> _generateStraightLineRoute(LatLng start, LatLng end) {
    List<LatLng> route = [];
    const int steps = 50;
    for (int i = 0; i <= steps; i++) {
      double lat =
          start.latitude + (end.latitude - start.latitude) * (i / steps);
      double lng =
          start.longitude + (end.longitude - start.longitude) * (i / steps);
      route.add(LatLng(lat, lng));
    }
    print('DEBUG: Generated straight line route as fallback.');
    return route;
  }

  // Zooms the map to fit the entire route
  void _zoomToFitRoute() {
    if (routePoints.isNotEmpty) {
      final bounds = LatLngBounds.fromPoints(routePoints);
      mapController.fitCamera(
        CameraFit.bounds(bounds: bounds, padding: const EdgeInsets.all(50)),
      );
      print('DEBUG: Map zoomed to fit route.');
    } else {
      // If no route, just center on start location
      mapController.move(_actualStartLocation, 14.0);
      print('DEBUG: No route points, map centered on start location.');
    }
  }

  // Helper to show snackbars
  void _showSnackbar(String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  // Builds a row for address display
  Widget _buildAddressCard(String start, String end) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: const [BoxShadow(color: Colors.black12, blurRadius: 6)],
      ),
      child: Column(
        children: [
          _buildDotRow(Colors.blue, start),
          const SizedBox(height: 12),
          _buildDotRow(Colors.red, end),
        ],
      ),
    );
  }

  // Builds a single row with a colored dot and text
  Widget _buildDotRow(Color color, String text) {
    return Row(
      children: [
        Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
        const SizedBox(width: 12),
        Expanded(child: Text(text, style: const TextStyle(fontSize: 14))),
      ],
    );
  }

  // Builds the trip details section
  Widget _buildTripDetail(RescueRequestNotificationDto? requestData) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            "Request Information", // Changed to English
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          _buildInfoRow(
            "Request ID",
            requestData?.rrid?.toString() ?? "-",
          ), // Changed to English
          _buildInfoRow(
            "Service Type",
            requestData?.serviceType ?? "-",
          ), // Changed to English
          _buildInfoRow(
            "Requester",
            requestData?.userFullName ?? "-",
          ), // Changed to English
          _buildInfoRow(
            "Message",
            requestData?.message ?? "-",
          ), // Changed to English
          _buildInfoRow(
            "Estimated Price", // Changed to English
            "${requestData?.finalPrice?.toStringAsFixed(0) ?? '-'} VNĐ",
          ),
          _buildInfoRow(
            "Payment Method",
            requestData?.paymentMethod ?? "-",
          ), // Changed to English
        ],
      ),
    );
  }

  // Builds a single info row with label and value
  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 14, color: Colors.grey)),
          Text(
            value,
            style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // Addresses displayed in the card should come from rescueData's string addresses
    final startAddress =
        widget.rescueData?.from ?? "Loading address..."; // Changed to English
    final endAddress =
        widget.rescueData?.to ?? "No destination"; // Changed to English

    // Determine initial map center
    LatLng mapCenter = _actualStartLocation;
    if (_actualDestinationLocation != null) {
      mapCenter = LatLng(
        (_actualStartLocation.latitude + _actualDestinationLocation!.latitude) /
            2,
        (_actualStartLocation.longitude +
                _actualDestinationLocation!.longitude) /
            2,
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Track Request', // Changed to English
          style: TextStyle(color: Colors.white),
        ),
        backgroundColor: Colors.blue[900], // Changed to dark blue
        iconTheme: const IconThemeData(color: Colors.white),
        centerTitle: true,
      ),
      body: Stack(
        children: [
          // Map Layer
          FlutterMap(
            mapController: mapController,
            options: MapOptions(
              initialCenter: mapCenter,
              initialZoom: 14,
              // It's crucial to set allowPanningOnMarkers to true if markers are dense or interactable
              // allowPanningOnMarkers: true, // This is a good default for user experience
            ),
            children: [
              TileLayer(
                urlTemplate:
                    "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/256/{z}/{x}/{y}@2x?access_token=${mapboxToken}", // Ensure token is correctly used
                userAgentPackageName: 'com.example.resq_app',
              ),
              // Markers for start and destination
              MarkerLayer(
                markers: [
                  Marker(
                    point:
                        _actualStartLocation, // Use the actual derived start location
                    width: 40,
                    height: 40,
                    child: const Icon(
                      Icons.location_on,
                      color: Colors.blue,
                      size: 40,
                    ),
                  ),
                  if (_actualDestinationLocation != null)
                    Marker(
                      point:
                          _actualDestinationLocation!, // Use the actual derived destination location
                      width: 40,
                      height: 40,
                      child: const Icon(
                        Icons.flag,
                        color: Colors.red,
                        size: 40,
                      ),
                    ),
                ],
              ),
              // Route Polyline
              if (routePoints.isNotEmpty)
                PolylineLayer(
                  polylines: [
                    Polyline(
                      points: routePoints,
                      color: Colors.blueAccent,
                      strokeWidth: 4,
                    ),
                  ],
                ),
            ],
          ),

          // Address Card (always visible at the top)
          Positioned(
            top: 16,
            left: 16,
            right: 16,
            child: _buildAddressCard(startAddress, endAddress),
          ),

          // Bottom Sheet for trip details
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: Container(
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
                boxShadow: [BoxShadow(color: Colors.black26, blurRadius: 10)],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min, // Wrap content height
                children: [
                  Container(
                    width: 40,
                    height: 4,
                    margin: const EdgeInsets.symmetric(vertical: 8),
                    decoration: BoxDecoration(
                      color: Colors.grey[300],
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                  // Display general request information
                  _buildTripDetail(widget.rescueData),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),

          // Loading indicator for route fetching
          if (isLoadingRoute)
            const Positioned.fill(
              child: Center(child: CircularProgressIndicator()),
            ),
        ],
      ),
    );
  }
}
