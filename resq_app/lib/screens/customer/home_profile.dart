import 'package:flutter/material.dart';
import 'package:resq_app/screens/customer/documentary/documentaries_page.dart';
import 'package:resq_app/screens/customer/personalData/new_personal_data_page.dart';
import 'package:resq_app/screens/customer/profile/profile_page.dart';
import 'package:resq_app/screens/customer/vehicles/vehicles_page.dart';
import 'package:resq_app/screens/payment/payments_page.dart';
import './security_profile.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/models/home_profile_dto.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/screens/settings/settings_page.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:resq_app/screens/customer/history_page.dart';
import 'package:resq_app/pages/statistics_customer_page.dart';
import 'package:resq_app/screens/customer/discount/vouchers_page.dart';
import 'package:resq_app/screens/customer/ranks_info_page.dart';
import '../../services/avatar_picker.dart';

class HomeProfilePage extends StatefulWidget {
  const HomeProfilePage({super.key});

  @override
  State<HomeProfilePage> createState() => _HomeProfilePageState();
}

class _HomeProfilePageState extends State<HomeProfilePage> {
  int currentIndex = 2;
  HomeProfileDto? profile;
  int userId = loginResponse?.userId ?? 0;

  @override
  void initState() {
    super.initState();
    _loadProfile();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();

    // Gọi lại khi quay lại màn hình hiện tại
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (ModalRoute.of(context)?.isCurrent == true) {
        _loadProfile();
      }
    });
  }

  Future<void> _loadProfile() async {
    final result = await CustomerService().fetchHomeProfile(userId);
    setState(() => profile = result);
  }

  @override
  Widget build(BuildContext context) {
    final screenHeight = MediaQuery.of(context).size.height;
    final screenWidth = MediaQuery.of(context).size.width;

    final bgHeight = screenHeight * 0.22;
    final infoHeight = screenHeight * 0.16;
    final navBarHeight = screenHeight * 0.10;
    final avatarRadius = 100.0;
    final avatarTop = bgHeight - avatarRadius;
    final gridHeight = screenHeight - bgHeight - infoHeight - navBarHeight;
    final avatarSize = 80.0;
    final avatarPadding = 6.0;
    final avatarBorder = 2.0;
    final avatarTotalWidth =
        avatarSize * 2 + avatarPadding * 2 + avatarBorder * 2;

    String resTitle = "Res Earth";
    String resIcon = "assets/icons/earth.png";
    int loyalty = profile?.loyaltyPoint ?? 0;

    if (loyalty >= 10000) {
      resTitle = "Res Fire";
      resIcon = "assets/icons/fire.png";
    } else if (loyalty >= 5000) {
      resTitle = "Res Wood";
      resIcon = "assets/icons/wood.png";
    } else if (loyalty >= 2500) {
      resTitle = "Res Water";
      resIcon = "assets/icons/water.png";
    } else if (loyalty >= 1000) {
      resTitle = "Res Metal";
      resIcon = "assets/icons/metal.png";
    }

    return Scaffold(
      backgroundColor: Colors.white,

      body: Column(
        children: [
          Stack(
            clipBehavior: Clip.none,
            children: [
              SizedBox(
                height: bgHeight,
                width: double.infinity,
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    Image.asset('assets/images/homebg.png', fit: BoxFit.cover),
                    GestureDetector(
                      onTap: () {
                        if (userId == 0) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Please login first!'),
                            ),
                          );
                          return;
                        }
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => SettingsPage(userId: userId, status: profile?.status ?? ''),
                          ),
                        );
                      },
                      child: Container(
                        alignment: Alignment.topRight,
                        padding: const EdgeInsets.only(top: 40, right: 20),
                        child: Image.asset(
                          'assets/icons/settings.png',
                          width: 40,
                          height: 40,
                          fit: BoxFit.contain,
                          errorBuilder:
                              (context, error, stackTrace) => const Icon(
                                Icons.settings,
                                color: Colors.white,
                              ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              Positioned(
                top: avatarTop,
                left: (screenWidth - avatarTotalWidth) / 2,
                child: GestureDetector(
                  onTap: () async {
                    await changeAvatar(userId, context);
                    await _loadProfile(); // refresh sau khi đổi ảnh
                  },
                  child: Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.black, width: 2),
                      color: Colors.white,
                    ),
                    child: CircleAvatar(
                      radius: avatarSize,
                      backgroundColor: Colors.white,
                      child: ClipOval(
                        child:
                            profile?.avatar.isNotEmpty == true
                                ? Image.network(
                                  profile!.imageUrl,
                                  width: avatarSize * 2,
                                  height: avatarSize * 2,
                                  fit: BoxFit.cover,
                                  errorBuilder:
                                      (_, __, ___) => Image.asset(
                                        'assets/images/logo.png',
                                        width: avatarSize,
                                      ),
                                )
                                : Image.asset(
                                  'assets/images/logo.png',
                                  width: avatarSize,
                                ),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
          SizedBox(
            height: infoHeight,
            width: double.infinity,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                // crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  // Left: Name + Loyalty Point
                  Expanded(
                    flex: 2,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 34),
                        Text(
                          (profile?.userName?.isNotEmpty ?? false)
                              ? profile!.userName!
                              : loginResponse?.userName ?? 'User not Found',
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFFC30003),
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          'Loyalty Points: $loyalty',
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFFC30003),
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Right: Res Title & Res Icon
                  Flexible(
                    flex: 1,
                    child: FittedBox(
                      fit: BoxFit.scaleDown,
                      alignment: Alignment.center,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          GestureDetector(
                            onTap: () {
                              PersistentNavBarNavigator.pushNewScreen(
                                context,
                                screen: RanksInfoPage(),
                                withNavBar: true,
                                pageTransitionAnimation:
                                    PageTransitionAnimation.cupertino,
                              );
                            },
                            child: Image.asset(
                              resIcon,
                              width: 60,
                              height: 60,
                              fit: BoxFit.contain,
                              errorBuilder:
                                  (context, error, stackTrace) => const Icon(
                                    Icons.shield,
                                    color: Colors.orange,
                                    size: 40,
                                  ),
                            ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            resTitle,
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w600,
                              color: Color(0xFFC30003),
                            ),
                            overflow: TextOverflow.ellipsis,
                            maxLines: 1,
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const Divider(height: 0.5, thickness: 0.5, color: Colors.grey),
          Expanded(
            child: SizedBox(
              height: gridHeight,
              width: screenWidth,
              child: GridView.builder(
                physics: const NeverScrollableScrollPhysics(),
                padding: EdgeInsets.zero,
                itemCount: 9,
                gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  mainAxisExtent: gridHeight / 3,
                ),
                itemBuilder: (context, index) {
                  final titles = [
                    "Personal\nProfile",
                    "Security\nProfile",
                    "Identity\nVerification",
                    "Documents",
                    "Rescue\nHistory",
                    "Available\nOffers",
                    "Vehicles",
                    "Payment\nMethods",
                    "Account\nStatistics",
                  ];
                  final icons = [
                    "assets/icons/profile.png",
                    "assets/icons/security.png",
                    "assets/icons/verify.png",
                    "assets/icons/document.png",
                    "assets/icons/history.png",
                    "assets/icons/discount.png",
                    "assets/icons/vehicle.png",
                    "assets/icons/payment.png",
                    "assets/icons/stats.png",
                  ];
                  return _buildGridButton(
                    titles[index],
                    icons[index],
                    onTap: () {
                      switch (index) {
                        case 0:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: ProfilePage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 1:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: SecurityProfilePage(hasPD : profile?.hasPD ?? false),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 2:
                          if (profile?.hasPD == true) {
                            showDialog(
                              context: context,
                              builder:
                                  (context) => AlertDialog(
                                    title: const Text("Notice"),
                                    content: const Text(
                                      "You have already verified your identity. Please go to Security Profile to check it.",
                                    ),
                                    actions: [
                                      TextButton(
                                        onPressed:
                                            () => Navigator.of(context).pop(),
                                        child: const Text("OK"),
                                      ),
                                    ],
                                  ),
                            );
                            return;
                          }
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: NewPersonalDataPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 3:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: DocumentariesPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 4:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: HistoryPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 5:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: VouchersPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 6:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: VehiclesPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 7:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: PaymentsPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        case 8:
                          PersistentNavBarNavigator.pushNewScreen(
                            context,
                            screen: StatisticsPage(),
                            withNavBar: true,
                            pageTransitionAnimation:
                                PageTransitionAnimation.cupertino,
                          );
                          break;
                        default:
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: Text('Clicked on "${titles[index]}"'),
                            ),
                          );
                      }
                    },
                    isDisabled: index == 2 && (profile?.hasPD ?? false),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildGridButton(
    String label,
    String iconPath, {
    VoidCallback? onTap,
    bool isDisabled = false,
  }) {
    return GestureDetector(
      onTap: isDisabled ? null : onTap,
      child: Container(
        decoration: BoxDecoration(
          border: Border.all(color: Colors.grey.shade300, width: 0.8),
          color: isDisabled ? Colors.grey.shade200 : Colors.white,
        ),
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Opacity(
              opacity: isDisabled ? 0.4 : 1.0,
              child: Image.asset(
                iconPath,
                width: 32,
                height: 32,
                errorBuilder:
                    (context, error, stackTrace) => const Icon(
                      Icons.image_not_supported,
                      color: Color(0xFFBB0000),
                    ),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              label,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 13,
                color: isDisabled ? Colors.grey : Colors.black,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
