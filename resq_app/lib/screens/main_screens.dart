import 'package:flutter/material.dart';
import 'package:persistent_bottom_nav_bar/persistent_bottom_nav_bar.dart';
import 'package:resq_app/screens/home_page.dart';
import 'package:resq_app/screens/chatbox/MessagesScreen.dart';
import 'package:resq_app/screens/customer/home_profile.dart';
import 'package:resq_app/screens/notification/NotificationScreen.dart';

class MainScreen extends StatefulWidget {
  final int userId;

  const MainScreen({super.key, required this.userId});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  late PersistentTabController _controller;

  @override
  void initState() {
    super.initState();
    _controller = PersistentTabController(initialIndex: 0);
  }

  List<Widget> _buildScreens() {
    return [
      HomePage(userId: widget.userId),
      MessagesScreen(userId: widget.userId),
      HomeProfilePage(),
      NotificationScreen(userId: widget.userId),
    ];
  }

  List<PersistentBottomNavBarItem> _navBarsItems() {
    return [
      PersistentBottomNavBarItem(
        icon: Image.asset('assets/icons/clickhome.png', width: 30),
        inactiveIcon: Image.asset('assets/icons/home.png', width: 30),
        // title: 'Home',
        activeColorPrimary: Color(0xFF013171),
        inactiveColorPrimary: Colors.grey,
      ),
      PersistentBottomNavBarItem(
        icon: Image.asset('assets/icons/clickchat.png', width: 30),
        inactiveIcon: Image.asset('assets/icons/chat.png', width: 30),
        // title: "Chat",
        activeColorPrimary: Color(0xFF013171),
        inactiveColorPrimary: Colors.grey,
      ),
      PersistentBottomNavBarItem(
        icon: Image.asset('assets/icons/clickallprofile.png', width: 30),
        inactiveIcon: Image.asset('assets/icons/allprofile.png', width: 30),
        // title: "Profile",
        activeColorPrimary: Color(0xFF013171),
        inactiveColorPrimary: Colors.grey,
      ),
      PersistentBottomNavBarItem(
        icon: Image.asset('assets/icons/clicknotification.png', width: 30),
        inactiveIcon: Image.asset('assets/icons/notification.png', width: 30),
        // title: "Notification",
        activeColorPrimary: Color(0xFF013171),
        inactiveColorPrimary: Colors.grey,
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    return PersistentTabView(
      context,
      controller: _controller,
      screens: _buildScreens(),
      items: _navBarsItems(),
      confineToSafeArea: true,
      backgroundColor: Colors.white,
      handleAndroidBackButtonPress: true,
      resizeToAvoidBottomInset: true,
      stateManagement: true,
      navBarStyle: NavBarStyle.style6, // style3, style9... cũng được
    );
  }
}
