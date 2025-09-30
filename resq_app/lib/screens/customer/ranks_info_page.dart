import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class RanksInfoPage extends StatelessWidget {
  const RanksInfoPage({super.key});

  @override
  Widget build(BuildContext context) {
    final ranks = [
      {
        'image': 'assets/icons/earth.png',
        'title': 'Earth Tier',
        'points': 'Entry level',
        'changeLimit': '1 discount',
      },
      {
        'image': 'assets/icons/metal.png',
        'title': 'Metal Tier',
        'points': '1.000 points',
        'changeLimit': '3 discounts',
      },
      {
        'image': 'assets/icons/water.png',
        'title': 'Water Tier',
        'points': '2.500 points',
        'changeLimit': '5 discounts',
      },
      {
        'image': 'assets/icons/wood.png',
        'title': 'Wood Tier',
        'points': '5.000 points',
        'changeLimit': '7 discounts',
      },
      {
        'image': 'assets/icons/fire.png',
        'title': 'Fire Tier',
        'points': '10.000 points',
        'changeLimit': '10 discounts',
      },
    ];

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Membership Tier'),
      body: ListView.builder(
        itemCount: ranks.length,
        padding: const EdgeInsets.all(16),
        itemBuilder: (context, index) {
          final rank = ranks[index];
          return Container(
            margin: const EdgeInsets.only(bottom: 16),
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey.shade400, width: 1.5),
              borderRadius: BorderRadius.circular(12),
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.shade200,
                  offset: const Offset(0, 2),
                  blurRadius: 4,
                ),
              ],
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                // Hình ảnh xếp hạng
                Container(
                  height: 120,
                  width: 95,
                  child: Image.asset(
                    rank['image']!,
                    fit: BoxFit.cover,
                  ),
                ),
                const SizedBox(width: 16),
                // Nội dung thông tin
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        rank['title']!,
                        style: const TextStyle(
                          fontSize: 23,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF013171),
                        ),
                      ),
                      const SizedBox(height: 4),
                      RichText(
                        text: TextSpan(
                          style: DefaultTextStyle.of(context).style.copyWith(fontSize: 15),
                          children: [
                            TextSpan(text: 'Required points: ',
                              style: TextStyle(fontWeight: FontWeight.w700)),
                            TextSpan(
                              text: rank['points'],
                              style: TextStyle(
                                color:  rank['points'] == 'Entry level' ? Colors.black54   : Colors.black87,
                                fontWeight: FontWeight.w500,
                                fontStyle: rank['points'] == 'Entry level' ? FontStyle.italic : FontStyle.normal,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 6),
                      RichText(
                        text: TextSpan(
                          style: DefaultTextStyle.of(context).style.copyWith(fontSize: 14),
                          children: [
                            TextSpan(text: 'Able to claim '),
                            TextSpan(
                              text: rank['changeLimit'],
                              style: TextStyle(color: Colors.red, fontWeight: FontWeight.w900),
                            ),
                            TextSpan(text: ' per month'),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
