import 'dart:async';
import 'package:flutter/material.dart';
import 'package:resq_app/pages/onsite_select_location_page.dart';
import 'package:resq_app/pages/select_location_page.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:geolocator/geolocator.dart';
import 'package:geocoding/geocoding.dart';
import 'package:resq_app/screens/customer/discount/vouchers_page.dart';
import 'package:resq_app/screens/partner/partner_type_screen.dart';

class HomePage extends StatefulWidget {
  final int userId;

  const HomePage({super.key, required this.userId});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _selectedIndex = 0;
  String _currentCity = "Loading...";
  String _currentAddress = "Loading...";

  @override
  void initState() {
    super.initState();
    _determinePosition();
  }

  void _onTabSelected(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  Future<void> _determinePosition() async {
    bool serviceEnabled;
    LocationPermission permission;

    serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      setState(() {
        _currentCity = "Location service disabled";
        _currentAddress = "Please enable location services.";
      });
      return;
    }

    permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        setState(() {
          _currentCity = "Location permission denied";
          _currentAddress = "Please grant location permission.";
        });
        return;
      }
    }

    if (permission == LocationPermission.deniedForever) {
      setState(() {
        _currentCity = "Permission permanently denied";
        _currentAddress = "Enable it from settings.";
      });
      return;
    }

    try {
      Position position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      List<Placemark> placemarks = await placemarkFromCoordinates(
        position.latitude,
        position.longitude,
      );

      if (placemarks.isNotEmpty) {
        Placemark place = placemarks.first;
        setState(() {
          _currentCity = place.locality ?? "Unknown";
          _currentAddress =
              "${place.street ?? ""}, ${place.subLocality ?? ""}, ${place.administrativeArea ?? ""}";
        });
      } else {
        setState(() {
          _currentCity = "Address not found";
          _currentAddress =
              "Lat: ${position.latitude}, Lng: ${position.longitude}";
        });
      }
    } catch (e) {
      setState(() {
        _currentCity = "Location error";
        _currentAddress = e.toString();
      });
    }
  }

  Widget buildBannerCarousel({
    required List<String> imagePaths,
    required double height,
    VoidCallback? onTap, // thêm onTap
  }) {
    final PageController controller = PageController();
    int currentPage = 0;
    Timer? timer;

    void startAutoScroll() {
      timer?.cancel();
      timer = Timer.periodic(const Duration(seconds: 5), (_) {
        if (controller.hasClients) {
          currentPage++;
          if (currentPage >= imagePaths.length) currentPage = 0;
          controller.animateToPage(
            currentPage,
            duration: const Duration(milliseconds: 500),
            curve: Curves.easeInOut,
          );
        }
      });
    }

    startAutoScroll();

    return StatefulBuilder(
      builder: (context, setState) {
        return SizedBox(
          height: height,
          child: Stack(
            alignment: Alignment.bottomCenter,
            children: [
              PageView.builder(
                controller: controller,
                itemCount: imagePaths.length,
                onPageChanged: (index) {
                  setState(() => currentPage = index);
                },
                itemBuilder: (context, index) {
                  return GestureDetector(
                    onTap: onTap,
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(16),
                      child: Image.asset(
                        imagePaths[index],
                        fit: BoxFit.cover,
                        width: double.infinity,
                      ),
                    ),
                  );
                },
              ),
              Positioned(
                bottom: 10,
                child: Row(
                  children: List.generate(imagePaths.length, (index) {
                    return Container(
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color:
                            index == currentPage
                                ? Colors.white
                                : Colors.white54,
                      ),
                    );
                  }),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,

      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildLocationHeader(),
              const SizedBox(height: 32),
              _buildSectionTitle('Rescue Services'),
              const SizedBox(height: 20),
              _buildServiceButtonsGrid(context, widget.userId),
              const SizedBox(height: 32),
              _buildPartnerBanner(),
              const SizedBox(height: 32),
              _buildOtherButtons(),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.bold,
          color: Color(0xFF1E3A8A),
        ),
      ),
    );
  }

  Widget _buildLocationHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF1E3A8A), Color(0xFF3B82F6)],
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.blue.withOpacity(0.3),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(Icons.location_pin, color: Colors.white),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "Current Location",
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  _currentCity,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  _currentAddress,
                  style: const TextStyle(color: Colors.white70, fontSize: 13),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildServiceButtonsGrid(BuildContext context, int userId) {
    return Row(
      children: [
        Expanded(
          child: _buildServiceButton(
            'assets/images/ResQ_TC.png',
            'On-site',
            () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder:
                      (_) => OnsiteSelectLocationPage(
                        userId: userId,
                        rescueType: 'ResFix',
                      ),
                ),
              );
            },
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _buildServiceButton('assets/images/ResQ_KX.png', 'Towing', () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder:
                    (_) => SelectLocationPage(
                      userId: userId,
                      rescueType: 'ResTow',
                    ),
              ),
            );
          }),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _buildServiceButton('assets/images/ResQ_LT.png', 'Driver', () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder:
                    (_) => SelectLocationPage(
                      userId: userId,
                      rescueType: 'ResDrive',
                    ),
              ),
            );
          }),
        ),
      ],
    );
  }

  Widget _buildServiceButton(String asset, String title, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.1),
              blurRadius: 8,
              offset: const Offset(0, 4),
            ),
          ],
          border: Border.all(color: Colors.grey.withOpacity(0.2)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.all(8),
              child: Image.asset(
                asset,
                width: 60,
                height: 60,
                fit: BoxFit.contain,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              title,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: Color(0xFF374151),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPartnerBanner() {
    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => PartnerTypeScreen(userId: widget.userId),
          ),
        );
      },
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.1),
              blurRadius: 20,
              offset: const Offset(0, 8),
            ),
          ],
          border: Border.all(color: Colors.grey.withOpacity(0.1)),
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    'JOIN TEAM',
                    style: TextStyle(
                      color: Color(0xFF6366F1),
                      fontSize: 10,
                      fontWeight: FontWeight.w600,
                      letterSpacing: 1.0,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Become a Partner',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w700,
                      color: Color(0xFF1F2937),
                    ),
                  ),
                  SizedBox(height: 4),
                  Text(
                    'Join our rescue network and help others',
                    style: TextStyle(color: Color(0xFF6B7280), fontSize: 13),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 20),
            const Icon(Icons.groups, color: Color(0xFF6366F1), size: 50),
          ],
        ),
      ),
    );
  }

  Widget _buildOtherButtons() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 4, bottom: 20),
          child: Text(
            'Discounts',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: Color(0xFF013171),
            ),
          ),
        ),
        buildBannerCarousel(
          imagePaths: [
            'assets/images/discount banner.png',
            'assets/images/voucher banner.png',
          ],
          height: 180,
          onTap: () {
            PersistentNavBarNavigator.pushNewScreen(
              context,
              screen: VouchersPage(),
              withNavBar: true,
              pageTransitionAnimation: PageTransitionAnimation.cupertino,
            );
          },
        ),
        const SizedBox(height: 32),
        const Padding(
          padding: EdgeInsets.only(left: 4, bottom: 20),
          child: Text(
            'Services',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: Color(0xFF013171),
            ),
          ),
        ),
        buildBannerCarousel(
          imagePaths: [
            'assets/images/banner all.png',
            'assets/images/on-site banner.png',
            'assets/images/towing banner.png',
            'assets/images/driver banner.png',
          ],
          height: 180,
        ),
      ],
    );
  }
}
