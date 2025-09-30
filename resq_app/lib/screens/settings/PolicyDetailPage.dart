import 'package:flutter/material.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class PolicyDetailPage extends StatelessWidget {
  final String title;
  final String content;

  const PolicyDetailPage({super.key, required this.title, required this.content});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      appBar: CommonAppBar(title: title),
      body: Scrollbar(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
          child: Text(
            content,
            style: const TextStyle(
              fontSize: 16,
              height: 1.6,
              fontWeight: FontWeight.w400,
              color: Colors.black87,
              fontFamily: 'Roboto',
            ),
          ),
        ),
      ),
    );
  }
}
