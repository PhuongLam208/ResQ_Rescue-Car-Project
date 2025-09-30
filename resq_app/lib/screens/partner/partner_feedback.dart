import 'package:flutter/material.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import '../../services/partner_service.dart';
import '../../services/api_result.dart';
import './partner_payment.dart';

class PartnerFeedbackScreen extends StatefulWidget {
  final int rrid;
  final int partnerId;
  final String paymentMethod;
  const PartnerFeedbackScreen({Key? key, required this.rrid, required this.partnerId, required this.paymentMethod}) : super(key: key);

  @override
  State<PartnerFeedbackScreen> createState() => _PartnerFeedbackScreenState();
}

class _PartnerFeedbackScreenState extends State<PartnerFeedbackScreen> {
  int customerRating = 0;
  final TextEditingController commentController = TextEditingController();

  final List<String> tags = ["Friendly", "Polite", "Calm"];
  final Set<String> selectedTags = {};

  Future<void> submitFeedback() async {
    final service = PartnerService();
    ApiResult result = await service.partnerFeedback(
      rrid: widget.rrid,
      customerRate: customerRating,
      rescueDescription: commentController.text.trim(),
    );

    if (result.isSuccess) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Feedback submitted successfully!")),
      );
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => PartnerPaymentScreen(
          rrid: widget.rrid,
          partnerId: widget.partnerId,
          paymentMethod: widget.paymentMethod,
          )),
      );
    } else {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Error: ${result.message}")));
    }
    
  }

  void toggleTag(String tag) {
    setState(() {
      if (selectedTags.contains(tag)) {
        selectedTags.remove(tag);
        final pattern = RegExp(r'(\s*,?\s*)' + RegExp.escape(tag));
        commentController.text =
            commentController.text.replaceAll(pattern, '').trim();
        commentController.text = commentController.text.replaceAll(
          RegExp(r'^,+|,+$'),
          '',
        );
      } else {
        selectedTags.add(tag);
        final currentText = commentController.text.trim();
        commentController.text =
            currentText.isEmpty ? tag : "$currentText, $tag";
      }

      commentController.selection = TextSelection.fromPosition(
        TextPosition(offset: commentController.text.length),
      );
    });
  }

  Widget buildStarRating(int rating, Function(int) onChanged) {
    return Row(
      children: List.generate(5, (index) {
        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 1.5),
          child: GestureDetector(
            onTap: () => onChanged(index + 1),
            child: Icon(
              index < rating ? Icons.star : Icons.star_border,
              color: Colors.amber,
              size: 30,
            ),
          ),
        );
      }),
    );
  }

  @override
  Widget build(BuildContext context) {
    const tagColor = Color(0xFF013171);
    const redColor = Color(0xFFBB0000);
    final defaultStyle = TextStyle(fontSize: 20, color: tagColor);

    return Scaffold(
      backgroundColor: const Color(0xFFF4F6FA),
      appBar: const CommonAppBar(title: 'Partner Feedback'),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(30, 40, 30, 30),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              /// Title: Rate the customer
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text("Rate This Customer:", style: defaultStyle),
                  buildStarRating(
                    customerRating,
                    (val) => setState(() => customerRating = val),
                  ),
                ],
              ),
              const SizedBox(height: 32),

              const Text(
                "Write your feedback:",
                style: TextStyle(fontSize: 20, color: tagColor),
              ),
              const SizedBox(height: 20),

              /// Tags
              LayoutBuilder(
                builder: (context, constraints) {
                  double outerPadding = 4;
                  double totalWidth = constraints.maxWidth - outerPadding * 2;
                  int tagCount = tags.length;
                  double spacing = 12;
                  double totalSpacing = spacing * (tagCount - 1);
                  double tagWidth = (totalWidth - totalSpacing) / tagCount;

                  return Padding(
                    padding: EdgeInsets.symmetric(horizontal: outerPadding),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children:
                          tags.map((tag) {
                            final isSelected = selectedTags.contains(tag);
                            return SizedBox(
                              width: tagWidth,
                              child: GestureDetector(
                                onTap: () => toggleTag(tag),
                                child: Container(
                                  alignment: Alignment.center,
                                  padding: const EdgeInsets.symmetric(
                                    vertical: 8,
                                  ),
                                  decoration: BoxDecoration(
                                    border: Border.all(color: tagColor),
                                    borderRadius: BorderRadius.circular(10),
                                    color:
                                        isSelected
                                            ? tagColor.withOpacity(0.1)
                                            : Colors.transparent,
                                  ),
                                  child: Text(
                                    tag,
                                    style: const TextStyle(
                                      color: tagColor,
                                      fontWeight: FontWeight.w500,
                                      fontSize: 18,
                                    ),
                                  ),
                                ),
                              ),
                            );
                          }).toList(),
                    ),
                  );
                },
              ),
              const SizedBox(height: 20),

              /// Input box
              Container(
                margin: const EdgeInsets.symmetric(horizontal: 2),
                child: TextField(
                  controller: commentController,
                  maxLines: 5,
                  maxLength: 500,
                  style: defaultStyle,
                  decoration: InputDecoration(
                    hintText: "Write something...",
                    hintStyle: defaultStyle.copyWith(
                      color: tagColor.withOpacity(0.5),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: tagColor),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: tagColor, width: 1.5),
                    ),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: tagColor),
                    ),
                    contentPadding: const EdgeInsets.all(15),
                  ),
                ),
              ),
              const SizedBox(height: 30),

              /// Instruction text
              Center(
                child: Text(
                  "Please complete this feedback \n to receive your payment for this Rescue!",
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.black.withOpacity(0.7),
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: 28),

              /// Submit button
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  ElevatedButton(
                    onPressed: () async {
                      if (customerRating == 0) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text(
                              "Please rate the customer before submitting.",
                            ),
                            backgroundColor: Colors.redAccent,
                          ),
                        );
                        return;
                      }

                      // Gửi feedback
                      print("Rating: $customerRating");
                      print("Tags: $selectedTags");
                      print("Comment: ${commentController.text}");
                      await submitFeedback();
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: redColor,
                      padding: const EdgeInsets.symmetric(
                        horizontal: 24,
                        vertical: 12,
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                    ),
                    child: const Text(
                      "Submit",
                      style: TextStyle(color: Colors.white, fontSize: 18),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: 0,
        selectedItemColor: tagColor,
        unselectedItemColor: Colors.black,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: ''),
          BottomNavigationBarItem(
            icon: Icon(Icons.chat_bubble_outline),
            label: '',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: ''),
          BottomNavigationBarItem(icon: Icon(Icons.notifications), label: ''),
        ],
      ),
    );
  }
}
