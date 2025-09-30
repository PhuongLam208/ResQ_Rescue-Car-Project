import 'package:flutter/material.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'PolicyDetailPage.dart';

class PolicyPage extends StatelessWidget {
  const PolicyPage({super.key});

  @override
  Widget build(BuildContext context) {
    final List<String> policies = [
      "Terms and Conditions",
      "Payment Security Policy",
      "Operational Regulations",
      "Dispute Resolution Mechanism",
      "Privacy Policy",
    ];

    return Scaffold(
      appBar: const CommonAppBar(title: 'Terms and Policies'),
      body: ListView.separated(
        itemCount: policies.length,
        separatorBuilder: (context, index) => const Divider(height: 1),
        itemBuilder: (context, index) {
          final title = policies[index];
          return ListTile(
            leading: const Icon(Icons.policy, color: Color(0xFF013171)),
            title: Text(
              title,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 16,
              ),
            ),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {
              String content = _getPolicyContent(title);
              if (content.isNotEmpty) {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => PolicyDetailPage(title: title, content: content),
                  ),
                );
              }
            },
          );
        },
      ),
    );
  }

  String _getPolicyContent(String title) {
    switch (title) {
      case "Privacy Policy":
        return '''PERSONAL DATA PROTECTION POLICY OF GREEN AND SMART MOBILITY JOINT STOCK COMPANY (GSM)

This policy outlines how GSM collects, uses, and processes personal data arising during its business operations.

1. GENERAL PROVISIONS

1.1 Personal Data: Information in the form of symbols, letters, numbers, images, sounds, or similar on an electronic medium associated with a specific or identifiable individual.''';

      case "Dispute Resolution Mechanism":
        return '''DISPUTE RESOLUTION MECHANISM

We are committed to resolving all disputes between users and the company fairly, promptly, and effectively.

1. METHODS OF RESOLUTION

1.1 Users can submit complaints via:
- Email: hotro@resq.vn
- Phone: 1900 2088
- Address: 123 ABC Street, XYZ District, HCMC

1.2 Our support team will respond within 3 business days.

2. RESOLUTION TIMEFRAME

2.1 Disputes will be resolved within 7 business days after receiving complete information.

3. COMPLICATED CASES

3.1 If negotiation fails, the dispute will be resolved in the competent People’s Court in HCMC in accordance with the law.''';

      case "Operational Regulations":
        return '''OPERATIONAL REGULATIONS OF THE RESQ APP

These regulations define principles, rights, and responsibilities among users, partners, and ResQ Company in using rescue services.

1. SCOPE
1.1 Applies to all users, partners, and employees using the ResQ app.

2. PRINCIPLES
2.1 Operate with transparency, fairness, and protection of all parties.

2.2 Users have the right to request rescue services in emergencies to ensure safety.

3. RESPONSIBILITIES

3.1 Users must provide accurate and up-to-date information when registering and using the app.

3.2 Partners must adhere to rescue quality and timing commitments.

3.3 ResQ is responsible for managing the system and supporting dispute resolution.

4. AMENDMENTS
4.1 ResQ reserves the right to adjust regulations and will publish changes on the app.''';

      case "Payment Security Policy":
        return '''PAYMENT SECURITY POLICY

ResQ is committed to protecting user payment information using modern technology and complying with legal regulations.

1. DATA COLLECTION
1.1 We only collect necessary data such as name, phone, email, and card information (for online payments).

2. STORAGE & SECURITY
2.1 All payment data is encrypted and stored securely.
2.2 We do not store card numbers or CVV/CVC codes.

3. INFORMATION SHARING
3.1 ResQ does not share payment info with third parties unless required by law or authorities.

4. THIRD-PARTY GATEWAYS
4.1 When using Momo, VNPay, Visa/MasterCard, users are redirected to secure gateways.

5. USER PROTECTION
5.1 ResQ may suspend or cancel suspicious transactions.
5.2 Users can contact: hotro@resq.vn or 1900 2088 for assistance.''';

      case "Terms and Conditions":
        return '''TERMS AND CONDITIONS FOR USING THE RESQ RESCUE SERVICE

By registering and using ResQ, users agree to the terms and policies below:

1. CONDITIONS OF USE
1.1 Users must be 18+ years old and have full legal capacity.
1.2 Registration information must be accurate and up to date.

2. RIGHTS AND OBLIGATIONS
2.1 Users have the right to request services within the scope provided.
2.2 Users must pay fully and not misuse the service.

3. LIMITATIONS OF LIABILITY
3.1 ResQ is not responsible for damages beyond its control or caused by the user.

4. TERMINATION
4.1 ResQ may suspend or terminate accounts for policy or legal violations.

5. ADDITIONAL TERMS
5.1 Terms may be changed and published in the app without prior notice.
5.2 Users must regularly review and comply with updates.''';

      default:
        return '';
    }
  }
}
