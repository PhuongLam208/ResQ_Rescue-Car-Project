import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class MapPage extends StatefulWidget {
  const MapPage({Key? key}) : super(key: key);

  @override
  State<MapPage> createState() => _MapPageState();
}

class _MapPageState extends State<MapPage> {
  final LatLng myLocation = LatLng(10.776, 106.695); // Hồ Chí Minh
  double zoom = 13.0;

  late MapController mapController;

  @override
  void initState() {
    super.initState();
    mapController = MapController();
  }

  void _zoomIn() {
    setState(() {
      zoom += 1;
      mapController.move(mapController.camera.center, zoom);
    });
  }

  void _zoomOut() {
    setState(() {
      zoom -= 1;
      mapController.move(mapController.camera.center, zoom);
    });
  }

  void _centerToMyLocation() {
    mapController.move(myLocation, zoom);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Rescue Map'),
      body: Stack(
        children: [
          FlutterMap(
            mapController: mapController,
            options: MapOptions(initialCenter: myLocation, initialZoom: zoom),
            children: [
              TileLayer(
                urlTemplate:
                    "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                subdomains: ['a', 'b', 'c'],
                userAgentPackageName: 'com.example.rescue_app',
              ),
              MarkerLayer(
                markers: [
                  Marker(
                    width: 40,
                    height: 40,
                    point: myLocation,
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
          Positioned(
            bottom: 16,
            right: 16,
            child: Column(
              children: [
                FloatingActionButton(
                  heroTag: 'zoomIn',
                  mini: true,
                  onPressed: _zoomIn,
                  child: const Icon(Icons.add),
                ),
                const SizedBox(height: 8),
                FloatingActionButton(
                  heroTag: 'zoomOut',
                  mini: true,
                  onPressed: _zoomOut,
                  child: const Icon(Icons.remove),
                ),
                const SizedBox(height: 8),
                FloatingActionButton(
                  heroTag: 'center',
                  mini: true,
                  onPressed: _centerToMyLocation,
                  child: const Icon(Icons.my_location),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
