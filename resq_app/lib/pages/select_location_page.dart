import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:http/http.dart' as http;
import 'package:latlong2/latlong.dart';
import 'package:latlong2/latlong.dart' show Distance, LengthUnit;
import 'package:geolocator/geolocator.dart'; // Add this import for location services
import 'package:resq_app/config/app_config.dart';
import 'confirm_request_page.dart';
import '../services/geocoding_service.dart';
import 'package:resq_app/services/rescue_api_service.dart';

class SelectLocationPage extends StatefulWidget {
  final int userId;
  final String rescueType;

  const SelectLocationPage({
    super.key,
    required this.userId,
    required this.rescueType,
  });

  @override
  State<SelectLocationPage> createState() => _SelectLocationPageState();
}

class _SelectLocationPageState extends State<SelectLocationPage> {


  LatLng? startLocation;
  LatLng? destinationLocation;
  String? startAddress;
  String? destinationAddress;

  double zoom = 13;
  final mapController = MapController();

  final geocodingService = GeocodingService();
  List<dynamic> suggestions = [];
  final TextEditingController searchController = TextEditingController();

  bool choosingStart = true;
  List<LatLng> routePoints = [];

  Map<String, dynamic>? requestResponse;
  Timer? _debounceTimer;

  // Helper function to reverse geocode a LatLng (using the updated GeocodingService)
  Future<String> _reverseGeocode(LatLng location) async {
    try {
      final address = await geocodingService.reverse(
        location.latitude,
        location.longitude,
      );
      return address ?? 'Unknown address'; // Return 'Unknown address' if null
    } catch (e) {
      print('Reverse geocoding failed: $e');
      return 'Could not get address';
    }
  }

  // Function to get current location
  Future<void> _getCurrentLocation() async {
    try {
      // Check permissions
      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          _showSnackBar('Location permissions are denied');
          return;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        _showSnackBar('Location permissions are permanently denied');
        return;
      }

      // Get current position
      Position position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      final currentLocation = LatLng(position.latitude, position.longitude);

      // Move map to current location
      mapController.move(currentLocation, zoom);

      if (choosingStart) {
        setState(() {
          startLocation = currentLocation;
          startAddress = "Loading address...";
          routePoints = [];
          searchController.clear();
          suggestions = [];
        });

        // Get address for current location
        _reverseGeocode(currentLocation).then((addr) {
          if (mounted) {
            setState(() {
              startAddress = addr;
            });
          }
        });
      } else {
        setState(() {
          destinationLocation = currentLocation;
          destinationAddress = "Loading address...";
          searchController.clear();
          suggestions = [];
        });

        // Get address for current location
        _reverseGeocode(currentLocation).then((addr) {
          if (mounted) {
            setState(() {
              destinationAddress = addr;
              if (startLocation != null) {
                _fetchRoute(startLocation!, currentLocation);
              }
            });
          }
        });
      }

      _showSnackBar('Current location selected');
    } catch (e) {
      _showSnackBar('Failed to get current location: ${e.toString()}');
    }
  }

  void _clearSearch() {
    setState(() {
      searchController.clear();
      suggestions = [];
    });
    if (_debounceTimer?.isActive ?? false) {
      _debounceTimer!.cancel();
    }
  }

  void _onSearchChanged(String query) {
    if (_debounceTimer?.isActive ?? false) {
      _debounceTimer!.cancel();
    }

    if (query.isEmpty) {
      setState(() {
        suggestions = [];
      });
      return;
    }

    _debounceTimer = Timer(const Duration(milliseconds: 500), () async {
      try {
        final results = await geocodingService.search(query);
        setState(() {
          suggestions = results;
        });
      } catch (e) {
        print('Search failed: $e');
        _showSnackBar('Search failed: ${e.toString()}');
      }
    });
  }

  void _selectSuggestion(dynamic item) async {
    final lat = double.parse(item['lat']);
    final lon = double.parse(item['lon']);
    final selected = LatLng(lat, lon);
    final address = item['display_name'];

    setState(() {
      suggestions = [];
      searchController.text = address;
    });

    mapController.move(selected, zoom);

    if (choosingStart) {
      setState(() {
        startLocation = selected;
        startAddress = address;
        routePoints = [];
      });
    } else {
      setState(() {
        destinationLocation = selected;
        destinationAddress = address;
      });
      if (startLocation != null) {
        await _fetchRoute(startLocation!, selected);
      }
    }
  }

  void _onMapTap(LatLng latlng) async {
    setState(() {
      suggestions = [];
      searchController.clear();
    });

    if (choosingStart) {
      setState(() {
        startLocation = latlng;
        startAddress = "Loading address...";
        routePoints = [];
      });
      _reverseGeocode(latlng).then((addr) {
        if (mounted) {
          setState(() {
            startAddress = addr;
          });
        }
      });
    } else {
      setState(() {
        destinationLocation = latlng;
        destinationAddress = "Loading address...";
      });
      _reverseGeocode(latlng).then((addr) {
        if (mounted) {
          setState(() {
            destinationAddress = addr;
            if (startLocation != null) {
              _fetchRoute(startLocation!, latlng);
            }
          });
        }
      });
    }
  }

  Future<void> _fetchRoute(LatLng start, LatLng end) async {
    if (const Distance().as(LengthUnit.Meter, start, end) < 10) {
      setState(() {
        routePoints = [start, end];
      });
      _showSnackBar('Distance is very short. Calculated as 0.00 KM.');
      return;
    }

    final url =
        'http://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson';

    try {
      final response = await http.get(Uri.parse(url));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);

        if (data['code'] == 'NoRoute') {
          print('OSRM: No route found: ${data['message']}');
          _showSnackBar(
            'No route found between these points. Please select again.',
          );
          setState(() {
            routePoints = [];
          });
          return;
        }

        if (data['routes'] != null &&
            data['routes'].isNotEmpty &&
            data['routes'][0]['geometry'] != null &&
            data['routes'][0]['geometry']['coordinates'] != null) {
          final coords = data['routes'][0]['geometry']['coordinates'] as List;
          setState(() {
            routePoints = coords.map((c) => LatLng(c[1], c[0])).toList();
          });
        } else {
          _showSnackBar('Routing data error. Please try again.');
          setState(() {
            routePoints = [];
          });
        }
      } else {
        _showSnackBar(
          'Failed to load route. Error code: ${response.statusCode}.',
        );
        setState(() {
          routePoints = [];
        });
      }
    } catch (e) {
      _showSnackBar('Connection error when fetching route: ${e.toString()}.');
      setState(() {
        routePoints = [];
      });
    }
  }

  Future<double> calculateDistanceFromApi(List<LatLng> points) async {
    if (points.length < 2) return 0.0;

    double totalDistance = await RescueApiService.getDistanceFromPoints(points);

    return double.parse(totalDistance.toStringAsFixed(2));
  }

  String estimateTime(double distanceKm) {
    const averageSpeed = 30;
    final timeMinutes = (distanceKm / averageSpeed) * 60;
    return "${timeMinutes.round()} minutes";
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 3)),
    );
  }

  Future<void> _createRequestAndNavigate() async {
    if (startLocation == null ||
        startAddress == null ||
        startAddress!.isEmpty ||
        startAddress == "Loading address...") {
      _showSnackBar(
        "Please select a pickup location and wait for the address to load.",
      );
      return;
    }

    bool requiresDestination =
        widget.rescueType == "ResTow" || widget.rescueType == "ResDrive";

    if (requiresDestination) {
      if (destinationLocation == null ||
          destinationAddress == null ||
          destinationAddress!.isEmpty ||
          destinationAddress == "Loading address...") {
        _showSnackBar(
          "Please select a destination and wait for the address to load.",
        );
        return;
      }

      if (routePoints.isEmpty &&
          (const Distance().as(
                LengthUnit.Meter,
                startLocation!,
                destinationLocation!,
              ) >
              10)) {
        _showSnackBar(
          "Unable to calculate route. Please try again or select different points.",
        );
        return;
      }
    }

    final double calculatedDistance = await calculateDistanceFromApi(
      routePoints,
    );
    print('DEBUG: Calculated distance for request: $calculatedDistance KM');

    final requestData = {
      "userId": widget.userId,
      "rescueType": widget.rescueType,
      "startLat": startLocation!.latitude,
      "startLng": startLocation!.longitude,
      "startAddress": startAddress!,
      "endLat": requiresDestination ? destinationLocation?.latitude : null,
      "endLng": requiresDestination ? destinationLocation?.longitude : null,
      "endAddress": requiresDestination ? destinationAddress : null,
      "distanceKm": calculatedDistance,
      "paymentMethod": "CASH",
    };

    final apiService = RescueApiService();

    _showSnackBar("Creating rescue request...");
    try {
      final response = await apiService.createRescueRequest(requestData);

      if (response != null) {
        final estimatedTime = estimateTime(calculatedDistance);

        Navigator.push(
          context,
          MaterialPageRoute(
            builder:
                (_) => ConfirmRequestPage(
                  startLocation: startLocation!,
                  destinationLocation: destinationLocation,
                  routePoints: routePoints,
                  distanceKm: calculatedDistance,
                  rescueType: widget.rescueType,
                  userId: widget.userId,
                  rrid: response.rrid,
                  requestResult: response,
                  startAddress: startAddress!,
                  destinationAddress: destinationAddress!,
                  estimatedTime: estimatedTime,
                ),
          ),
        );
      } else {
        _showSnackBar("Failed to create rescue request. Please try again.");
        print("Failed to create rescue request.");
      }
    } catch (e) {
      print("ERROR: Exception while creating rescue request: $e");
      _showSnackBar(
        "An error occurred while creating request: ${e.toString()}",
      );
    }
  }

  Widget _locationBox(
    String title,
    String? address,
    Color dotColor,
    bool isStart,
  ) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.15),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Container(
            margin: const EdgeInsets.only(right: 8),
            width: 10,
            height: 10,
            decoration: BoxDecoration(color: dotColor, shape: BoxShape.circle),
          ),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontWeight: FontWeight.normal,
                    fontSize: 12,
                    color: Colors.grey,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  address ?? "Not selected",
                  style: const TextStyle(fontSize: 14, color: Colors.black87),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    bool requiresDestination =
        widget.rescueType == "ResTow" || widget.rescueType == "ResDrive";

    return Scaffold(
      appBar: AppBar(
        title: Text(
          choosingStart
              ? "Select Pickup Location"
              : requiresDestination
              ? "Select Destination"
              : "Confirm Request",
          style: const TextStyle(fontSize: 16),
        ),
      ),
      body: Column(
        children: [
          // Search Section with improved UI
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.grey[50],
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.1),
                  blurRadius: 4,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Column(
              children: [
                // Search Bar
                Container(
                  height: 50,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.grey[300]!),
                  ),
                  child: TextField(
                    controller: searchController,
                    decoration: InputDecoration(
                      hintText:
                          choosingStart
                              ? "Search for pickup location..."
                              : (requiresDestination
                                  ? "Search for destination..."
                                  : "No destination search needed"),
                      prefixIcon: const Icon(
                        Icons.search,
                        size: 22,
                        color: Colors.grey,
                      ),
                      suffixIcon:
                          searchController.text.isNotEmpty
                              ? IconButton(
                                icon: const Icon(
                                  Icons.clear,
                                  size: 20,
                                  color: Colors.grey,
                                ),
                                onPressed: _clearSearch,
                              )
                              : null,
                      contentPadding: const EdgeInsets.symmetric(
                        vertical: 12,
                        horizontal: 16,
                      ),
                      border: InputBorder.none,
                      enabledBorder: InputBorder.none,
                      focusedBorder: InputBorder.none,
                    ),
                    style: const TextStyle(fontSize: 14),
                    onChanged: (value) {
                      setState(() {}); // Rebuild to show/hide clear button
                      _onSearchChanged(value);
                    },
                    readOnly: !requiresDestination && !choosingStart,
                  ),
                ),

                const SizedBox(height: 12),

                // Current Location Button (only show when choosing start location)
                if (choosingStart)
                  SizedBox(
                    width: double.infinity,
                    height: 40,
                    child: ElevatedButton.icon(
                      onPressed: _getCurrentLocation,
                      icon: const Icon(
                        Icons.my_location,
                        size: 18,
                        color: Colors.white,
                      ),
                      label: const Text(
                        "Use Current Location",
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color.fromARGB(255, 7, 51, 86),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                        elevation: 2,
                      ),
                    ),
                  ),
              ],
            ),
          ),

          // Suggestions List or Map
          if (suggestions.isNotEmpty && (choosingStart || requiresDestination))
            Expanded(
              child: Container(
                color: Colors.grey[50],
                child: ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: suggestions.length,
                  itemBuilder: (context, index) {
                    final item = suggestions[index];
                    return Container(
                      margin: const EdgeInsets.only(bottom: 8),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: Colors.grey[200]!),
                      ),
                      child: ListTile(
                        dense: true,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 4,
                        ),
                        leading: Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: Colors.red[50],
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: const Icon(
                            Icons.location_on,
                            color: Colors.red,
                            size: 18,
                          ),
                        ),
                        title: Text(
                          item['display_name'] ?? '',
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w500,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        onTap: () => _selectSuggestion(item),
                      ),
                    );
                  },
                ),
              ),
            )
          else
            Expanded(
              child: Stack(
                children: [
                  FlutterMap(
                    mapController: mapController,
                    options: MapOptions(
                      initialCenter: LatLng(10.776, 106.695),
                      initialZoom: zoom,
                      onTap: (tapPos, latlng) => _onMapTap(latlng),
                    ),
                    children: [
                      TileLayer(
                        urlTemplate:
                            "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/256/{z}/{x}/{y}@2x?access_token=$mapboxToken",
                        userAgentPackageName: 'com.example.resq_app',
                      ),
                      MarkerLayer(
                        markers: [
                          if (startLocation != null)
                            Marker(
                              point: startLocation!,
                              width: 35,
                              height: 35,
                              child: const Icon(
                                Icons.location_pin,
                                color: Color.fromARGB(255, 7, 51, 86),
                                size: 35,
                              ),
                            ),
                          if (destinationLocation != null)
                            Marker(
                              point: destinationLocation!,
                              width: 35,
                              height: 35,
                              child: const Icon(
                                Icons.location_pin,
                                color: Color.fromARGB(255, 171, 14, 3),
                                size: 35,
                              ),
                            ),
                        ],
                      ),
                      if (routePoints.isNotEmpty)
                        PolylineLayer(
                          polylines: [
                            Polyline(
                              points: routePoints,
                              color: const Color.fromARGB(255, 7, 51, 86),
                              strokeWidth: 4,
                            ),
                          ],
                        ),
                    ],
                  ),

                  // Location boxes
                  if (startLocation != null)
                    Positioned(
                      top: 10,
                      left: 0,
                      right: 0,
                      child: _locationBox(
                        "Pickup Location",
                        startAddress,
                        const Color.fromARGB(255, 7, 51, 86),
                        true,
                      ),
                    ),
                  if (requiresDestination && destinationLocation != null)
                    Positioned(
                      top: startLocation != null ? 75 : 10,
                      left: 0,
                      right: 0,
                      child: _locationBox(
                        "Destination",
                        destinationAddress,
                        const Color.fromARGB(255, 171, 14, 3),
                        false,
                      ),
                    ),

                  // Confirm Button
                  Positioned(
                    bottom: 30,
                    left: 20,
                    right: 20,
                    child: SizedBox(
                      height: 50,
                      child: ElevatedButton(
                        onPressed: () {
                          if (choosingStart) {
                            if (startLocation == null ||
                                startAddress == null ||
                                startAddress!.isEmpty ||
                                startAddress == "Loading address...") {
                              _showSnackBar(
                                "Please select a pickup location and wait for the address to load.",
                              );
                              return;
                            }
                            setState(() {
                              choosingStart = false;
                              searchController.clear();
                              suggestions = [];
                            });
                            if (!requiresDestination) {
                              _createRequestAndNavigate();
                            }
                          } else {
                            if (requiresDestination &&
                                (destinationLocation == null ||
                                    destinationAddress == null ||
                                    destinationAddress!.isEmpty ||
                                    destinationAddress ==
                                        "Loading address...")) {
                              _showSnackBar(
                                "Please select a destination and wait for the address to load.",
                              );
                              return;
                            }
                            _createRequestAndNavigate();
                          }
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color.fromARGB(
                            255,
                            171,
                            14,
                            3,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                          elevation: 4,
                        ),
                        child: Text(
                          choosingStart
                              ? "Confirm Pickup Location"
                              : (requiresDestination
                                  ? "Confirm Request"
                                  : "Confirm Request"),
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
