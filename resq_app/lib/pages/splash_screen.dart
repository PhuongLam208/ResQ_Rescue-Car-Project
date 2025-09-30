// lib/pages/splash_screen.dart
import 'package:flutter/material.dart';
// import 'package:provider/provider.dart';
// import 'package:resq_app/provider/auth_provider.dart'; // Đảm bảo đúng đường dẫn
import 'package:resq_app/screens/auth/login_screen.dart';
// Màn hình đăng nhập
import 'package:resq_app/screens/main_screens.dart'; // Màn hình chính sau đăng nhập (hoặc Home Page của bạn)

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    // _navigateToNextScreen();
  }

  // _navigateToNextScreen() async {
  //   // Đảm bảo loadLoginState được gọi và hoàn thành
  //   final authProvider = Provider.of<AuthProvider>(context, listen: false);
  //   await authProvider.loadLoginState(); // Chờ để tải trạng thái đăng nhập

  //   // Sau khi tải xong, kiểm tra trạng thái và điều hướng
  //   if (authProvider.isAuthenticated) {
  //     // Nếu đã đăng nhập, chuyển đến màn hình chính
  //     Navigator.of(context).pushReplacement(
  //       MaterialPageRoute(
  //         builder:
  //             (context) =>
  //                 MainScreen(userId: authProvider.userId!), // Truyền userId
  //       ),
  //     );
  //   } else {
  //     // Nếu chưa đăng nhập, chuyển đến màn hình Login
  //     Navigator.of(context).pushReplacement(
  //       MaterialPageRoute(builder: (context) => const LoginScreen()),
  //     );
  //   }
  // }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Bạn có thể thêm logo hoặc một indicator ở đây
            CircularProgressIndicator(),
            SizedBox(height: 20),
            Text("Loading...", style: TextStyle(fontSize: 16)),
          ],
        ),
      ),
    );
  }
}
