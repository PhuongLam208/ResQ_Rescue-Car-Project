import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:geolocator/geolocator.dart';
import '../services/geocoding_service.dart';
import '../services/rescue_api_service.dart';
import '../services/resfix_service_api.dart';
import 'confirm_request_page.dart';

class OnsiteSelectLocationPage extends StatefulWidget {
  final int userId;
  final String rescueType;

  const OnsiteSelectLocationPage({
    super.key,
    required this.userId,
    required this.rescueType,
  });

  @override
  State<OnsiteSelectLocationPage> createState() =>
      _OnsiteSelectLocationPageState();
}

class _OnsiteSelectLocationPageState extends State<OnsiteSelectLocationPage> {
  final String mapboxToken =
      "pk.eyJ1IjoidHJhbXRyYW4xMjMiLCJhIjoiY21kNGRkMHQ0MGY2NTJscjZmcDY4bzVuNCJ9.L4-zGwpDVXx9aKqTqbDyvA";

  final mapController = MapController();
  final GeocodingService geocodingService = GeocodingService();
  final TextEditingController searchController = TextEditingController();

  LatLng? selectedLocation;
  String? selectedAddress;
  List<dynamic> suggestions = [];
  String? selectedDamageType;
  int selectedDamagePrice = 0;
  bool isLoadingLocation = false;
  bool isLoadingAddress = false;

  List<Map<String, dynamic>> damageOptions = [];

  @override
  void initState() {
    super.initState();
    fetchResfixServices();
  }

  Future<void> fetchResfixServices() async {
    try {
      final services = await ResfixServiceApi.fetchResfixDamages();
      setState(() {
        damageOptions =
            services.map((item) {
              return {
                'name': item['name'],
                'price': item['price'],
                'icon': Icons.build,
              };
            }).toList();
      });
    } catch (e) {
      print('Error fetching resfix services: $e');
    }
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 3)),
    );
  }

  Future<void> _getCurrentLocation() async {
    setState(() {
      isLoadingLocation = true;
    });

    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        _showSnackBar("Location services are disabled.");
        return;
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          _showSnackBar("Location permissions are denied.");
          return;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        _showSnackBar("Location permissions are permanently denied.");
        return;
      }

      Position position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      final currentLocation = LatLng(position.latitude, position.longitude);

      setState(() {
        selectedLocation = currentLocation;
        searchController.clear();
        suggestions = [];
      });

      mapController.move(currentLocation, 15);
      await _getAddressFromLocation(currentLocation);
    } catch (e) {
      print('Error getting current location: $e');
      _showSnackBar("Failed to get current location: ${e.toString()}");
    } finally {
      setState(() {
        isLoadingLocation = false;
      });
    }
  }

  Future<void> _getAddressFromLocation(LatLng location) async {
    setState(() {
      isLoadingAddress = true;
      selectedAddress = "Loading address...";
    });

    try {
      final address = await geocodingService.reverse(
        location.latitude,
        location.longitude,
      );
      setState(() {
        selectedAddress = address ?? "Unable to get address";
        searchController.text = address ?? "";
      });
    } catch (e) {
      print('Error getting address: $e');
      setState(() {
        selectedAddress = "Unable to get address";
      });
    } finally {
      setState(() {
        isLoadingAddress = false;
      });
    }
  }

  void _onSearchChanged(String query) async {
    if (query.isEmpty) {
      setState(() => suggestions = []);
      return;
    }
    try {
      final results = await geocodingService.search(query);
      setState(() => suggestions = results);
    } catch (e) {
      print('Search failed: $e');
    }
  }

  void _selectSuggestion(dynamic item) {
    final lat = double.parse(item['lat']);
    final lon = double.parse(item['lon']);
    final selected = LatLng(lat, lon);

    setState(() {
      selectedLocation = selected;
      selectedAddress = item['display_name'] ?? '';
      searchController.text = item['display_name'] ?? '';
      suggestions = [];
    });
    mapController.move(selected, 15);
  }

  void _onMapTap(LatLng latlng) {
    setState(() {
      selectedLocation = latlng;
      searchController.clear();
      suggestions = [];
    });
    _getAddressFromLocation(latlng);
  }

  Future<void> _showDamageOptions() async {
    final selectedItem = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) {
        return Container(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                "Select Damage Type",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 16),
              ...damageOptions.map((item) {
                return Card(
                  child: ListTile(
                    leading: Icon(item['icon'], color: Colors.blue),
                    title: Text(item['name']),
                    trailing: Text(
                      "${item['price']}đ",
                      style: const TextStyle(
                        fontWeight: FontWeight.w600,
                        color: Colors.green,
                      ),
                    ),
                    onTap: () => Navigator.pop(context, item),
                  ),
                );
              }).toList(),
            ],
          ),
        );
      },
    );

    if (selectedItem != null) {
      setState(() {
        selectedDamageType = selectedItem['name'];
        selectedDamagePrice = selectedItem['price'];
      });
    }
  }

  Future<void> _createRequestAndNavigate() async {
    if (selectedLocation == null ||
        selectedAddress == null ||
        selectedAddress!.isEmpty ||
        selectedAddress == "Loading address...") {
      _showSnackBar(
        "Please select a location and wait for the address to load.",
      );
      return;
    }

    if (selectedDamageType == null) {
      _showSnackBar("Please select damage type");
      return;
    }

    final requestData = {
      "userId": widget.userId,
      "rescueType": widget.rescueType,
      "startLat": selectedLocation!.latitude,
      "startLng": selectedLocation!.longitude,
      "startAddress": selectedAddress!,
      "endLat": 0.0,
      "endLng": 0.0,
      "endAddress": 0.0,
      "distanceKm": 0.0,
      "paymentMethod": "CASH",
      "damageType": selectedDamageType,
      "estimatedPrice": selectedDamagePrice,
    };

    final apiService = RescueApiService();

    _showSnackBar("Creating repair request...");
    try {
      final response = await apiService.createRescueRequest(requestData);

      if (response != null) {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder:
                (_) => ConfirmRequestPage(
                  rrid: response.rrid,
                  startLocation: selectedLocation!,
                  destinationLocation: selectedLocation!,
                  routePoints: [],
                  distanceKm: 0.0,
                  rescueType: widget.rescueType,
                  userId: widget.userId,
                  requestResult: response,
                  startAddress: selectedAddress!,
                  destinationAddress: "",
                  estimatedTime: "Partner will arrive as soon as possible",
                ),
          ),
        );
      } else {
        _showSnackBar("Failed to create repair request. Please try again.");
        print("Failed to create repair request.");
      }
    } catch (e) {
      print("ERROR: Exception while creating repair request: $e");
      _showSnackBar(
        "An error occurred while creating request: ${e.toString()}",
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Select Repair Location"),
        backgroundColor: const Color.fromARGB(255, 171, 14, 3),
        foregroundColor: Colors.white,
      ),
      body: Stack(
        children: [
          Column(
            children: [
              Container(
                padding: const EdgeInsets.all(8.0),
                color: Colors.grey.shade50,
                child: Column(
                  children: [
                    TextField(
                      controller: searchController,
                      decoration: InputDecoration(
                        hintText: "Search location...",
                        prefixIcon: const Icon(Icons.search),
                        suffixIcon:
                            isLoadingLocation
                                ? const Padding(
                                  padding: EdgeInsets.all(12.0),
                                  child: SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  ),
                                )
                                : IconButton(
                                  icon: const Icon(Icons.my_location),
                                  onPressed: _getCurrentLocation,
                                  tooltip: "Use current location",
                                ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        filled: true,
                        fillColor: Colors.white,
                      ),
                      onChanged: _onSearchChanged,
                    ),
                    if (selectedLocation != null && selectedAddress != null)
                      Container(
                        margin: const EdgeInsets.only(top: 8),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.blue.shade50,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.blue.shade200),
                        ),
                        child: Row(
                          children: [
                            Icon(
                              Icons.location_on,
                              color: Colors.blue.shade700,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    "Selected Location:",
                                    style: TextStyle(
                                      fontWeight: FontWeight.w600,
                                      fontSize: 12,
                                    ),
                                  ),
                                  Text(
                                    isLoadingAddress
                                        ? "Loading address..."
                                        : selectedAddress!,
                                    style: const TextStyle(fontSize: 14),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
              ),
              if (suggestions.isNotEmpty)
                Expanded(
                  child: Container(
                    color: Colors.white,
                    child: ListView.builder(
                      itemCount: suggestions.length,
                      itemBuilder: (context, index) {
                        final item = suggestions[index];
                        return ListTile(
                          leading: const Icon(
                            Icons.location_on,
                            color: Colors.red,
                          ),
                          title: Text(item['display_name'] ?? ''),
                          onTap: () => _selectSuggestion(item),
                        );
                      },
                    ),
                  ),
                )
              else
                Expanded(
                  child: FlutterMap(
                    mapController: mapController,
                    options: MapOptions(
                      initialCenter: LatLng(10.776, 106.695),
                      initialZoom: 13,
                      onTap: (tapPos, latlng) => _onMapTap(latlng),
                    ),
                    children: [
                      TileLayer(
                        urlTemplate:
                            "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/256/{z}/{x}/{y}@2x?access_token=$mapboxToken",
                        userAgentPackageName: 'com.example.rescue_app',
                      ),
                      if (selectedLocation != null)
                        MarkerLayer(
                          markers: [
                            Marker(
                              point: selectedLocation!,
                              width: 40,
                              height: 40,
                              child: const Icon(
                                Icons.location_pin,
                                color: Color.fromARGB(255, 7, 51, 86),
                                size: 40,
                              ),
                            ),
                          ],
                        ),
                    ],
                  ),
                ),
            ],
          ),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: SafeArea(
              top: false,
              child: SingleChildScrollView(
                child: Container(
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.vertical(
                      top: Radius.circular(16),
                    ),
                    boxShadow: [
                      BoxShadow(color: Colors.black26, blurRadius: 8),
                    ],
                  ),
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      GestureDetector(
                        onTap: _showDamageOptions,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            vertical: 12,
                            horizontal: 16,
                          ),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey.shade300),
                            borderRadius: BorderRadius.circular(8),
                            color: Colors.grey.shade50,
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.build_circle,
                                color: Colors.blue.shade600,
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      selectedDamageType ??
                                          "Select damage type",
                                      style: TextStyle(
                                        fontWeight: FontWeight.w500,
                                        color:
                                            selectedDamageType != null
                                                ? Colors.black87
                                                : Colors.grey.shade600,
                                      ),
                                    ),
                                    if (selectedDamagePrice > 0)
                                      Text(
                                        "Estimated: ${selectedDamagePrice}đ",
                                        style: TextStyle(
                                          fontSize: 12,
                                          color: Colors.green.shade600,
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                  ],
                                ),
                              ),
                              const Icon(Icons.keyboard_arrow_down),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color.fromARGB(
                              255,
                              171,
                              14,
                              3,
                            ),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ),
                          onPressed: _createRequestAndNavigate,
                          icon: const Icon(Icons.build),
                          label: const Text(
                            "Request Repair Service",
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
            ),
          ),
        ],
      ),
    );
  }
}
