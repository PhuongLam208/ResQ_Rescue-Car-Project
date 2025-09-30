import 'package:flutter/material.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class FaqPage extends StatelessWidget {
  const FaqPage({super.key});

  @override
  Widget build(BuildContext context) {
    final List<Map<String, String>> faqs = [
      {
        "question": "What services does vehicle rescue include?",
        "answer":
        "ResQ offers a full range of roadside assistance services, including:\n\n"
            "• Towing services: Transporting your vehicle to the nearest garage or location of your choice.\n"
            "• On-site mechanical support: Battery jump-start, flat tire replacement, fuel delivery, and more.\n"
            "• Driver substitution: A certified driver takes you and your vehicle safely if you're unable to drive.\n\n"
            "All services are conducted by trained technicians and professional rescue teams."
      },
      {
        "question": "Where does the service operate?",
        "answer":
        "ResQ currently operates in major cities and provinces including Ho Chi Minh City, Hanoi, Da Nang, Can Tho, and more.\n\n"
            "You can open the ResQ app to check availability in your location. We're expanding to serve you better."
      },
      {
        "question": "How are rescue fees calculated?",
        "answer":
        "Fees are based on:\n\n"
            "• Type of service (e.g., towing, jump start)\n"
            "• Distance traveled\n"
            "• Time of day (night or holidays may add fees)\n"
            "• Extra labor or parts (if needed)\n\n"
            "An estimate is shown in the app before confirming your request."
      },
      {
        "question": "Do I need to book in advance?",
        "answer":
        "No. ResQ is an on-demand service. Request help anytime through the app.\n\n"
            "For long-distance towing or planned maintenance transport, use the app’s advance booking feature."
      },
      {
        "question": "How long does it take for a rescue team to arrive?",
        "answer":
        "Arrival time depends on:\n\n"
            "• Your location & traffic\n"
            "• Availability of nearby teams\n"
            "• Time of request (peak hours might take longer)\n\n"
            "On average, expect 30–45 minutes. Track their arrival in real time via the app."
      },
      {
        "question": "Is the service available at night or on holidays?",
        "answer":
        "Yes! ResQ operates 24/7 including weekends and national holidays.\n\n"
            "Night-time or holiday service may include extra fees, always shown before confirmation."
      },
      {
        "question": "How do I pay for the service?",
        "answer":
        "You can pay securely via:\n\n"
            "• Momo\n"
            "• VNPay\n"
            "• Credit/Debit Cards (Visa, MasterCard)\n"
            "• Company wallet (if applicable)\n\n"
            "Receipts are emailed and saved in your app history after each transaction."
      },
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      appBar: const CommonAppBar(title: 'Frequently Asked Questions'),
      body: ListView.separated(
        itemCount: faqs.length,
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
        separatorBuilder: (context, index) => const SizedBox(height: 6),
        itemBuilder: (context, index) {
          return CustomExpansionTile(
            question: faqs[index]['question']!,
            answer: faqs[index]['answer']!,
          );
        },
      ),
    );
  }
}

class CustomExpansionTile extends StatefulWidget {
  final String question;
  final String answer;

  const CustomExpansionTile({
    super.key,
    required this.question,
    required this.answer,
  });

  @override
  State<CustomExpansionTile> createState() => _CustomExpansionTileState();
}

class _CustomExpansionTileState extends State<CustomExpansionTile> {
  bool _expanded = false;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 1,
      margin: const EdgeInsets.symmetric(horizontal: 6),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ExpansionTile(
        tilePadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        childrenPadding: const EdgeInsets.fromLTRB(20, 0, 20, 16),
        title: Text(
          widget.question,
          style: const TextStyle(
            fontWeight: FontWeight.w600,
            fontSize: 16,
            color: Color(0xFF013171),
          ),
        ),
        trailing: Icon(
          _expanded ? Icons.remove_circle_outline : Icons.add_circle_outline,
          color: const Color(0xFF013171),
        ),
        onExpansionChanged: (bool expanded) {
          setState(() {
            _expanded = expanded;
          });
        },
        children: [
          Text(
            widget.answer,
            style: const TextStyle(
              fontSize: 15,
              height: 1.6,
              color: Colors.black87,
            ),
          ),
        ],
      ),
    );
  }
}
