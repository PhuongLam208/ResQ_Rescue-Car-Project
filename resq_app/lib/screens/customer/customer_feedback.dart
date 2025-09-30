import 'package:flutter/material.dart';
import '../../services/customer_service.dart';
import '../../services/api_result.dart';

class CustomerFeedbackScreen extends StatefulWidget {
  final int rrid;
  const CustomerFeedbackScreen({Key? key, required this.rrid})
    : super(key: key);

  @override
  State<CustomerFeedbackScreen> createState() => _CustomerFeedbackScreenState();
}

class _CustomerFeedbackScreenState extends State<CustomerFeedbackScreen> {
  int resRating = 0;
  int partnerRating = 0;
  final TextEditingController commentController = TextEditingController();

  final List<String> tags = ["Professional", "Enthusiastic", "Quick"];
  final Set<String> selectedTags = {};

  void toggleTag(String tag) {
    setState(() {
      if (selectedTags.contains(tag)) {
        selectedTags.remove(tag);
        final pattern = RegExp(r'(\s*,?\s*)' + RegExp.escape(tag));
        commentController.text =
            commentController.text.replaceAll(pattern, '').trim();
        commentController.text = commentController.text.replaceAll(
          RegExp(r'^,+|,+\$'),
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
          padding: const EdgeInsets.symmetric(horizontal: 2),
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

  Future<void> submitFeedback() async {
    final service = CustomerService();
    ApiResult result = await service.customerFeedback(
      rrid: widget.rrid,
      RescueRate: resRating,
      PartnerRate: partnerRating,
      RescueDescription: commentController.text.trim(),
    );

    if (result.isSuccess) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Feedback submitted successfully!")),
      );
      Navigator.pushReplacementNamed(context, '/home_profile');
    } else {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Error: ${result.message}")));
    }
  }

  @override
  Widget build(BuildContext context) {
    const tagColor = Color(0xFF013171);
    const redColor = Color(0xFFBB0000);
    final defaultStyle = TextStyle(fontSize: 20, color: tagColor);

    return Scaffold(
      backgroundColor: const Color(0xFFF4F6FA),
      appBar: AppBar(
        backgroundColor: tagColor,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
        centerTitle: true,
        title: const Text(
          'Feedback',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(30, 40, 30, 30),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text("Rate This Rescue:", style: defaultStyle),
                  buildStarRating(
                    resRating,
                    (val) => setState(() => resRating = val),
                  ),
                ],
              ),
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text("Rate This Partner:", style: defaultStyle),
                  buildStarRating(
                    partnerRating,
                    (val) => setState(() => partnerRating = val),
                  ),
                ],
              ),
              const SizedBox(height: 32),
              const Text(
                "Write your feedback:",
                style: TextStyle(fontSize: 20, color: tagColor),
              ),
              const SizedBox(height: 20),
              Wrap(
                spacing: 9,
                children:
                    tags.map((tag) {
                      final isSelected = selectedTags.contains(tag);
                      return GestureDetector(
                        onTap: () => toggleTag(tag),
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 11,
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
                      );
                    }).toList(),
              ),
              const SizedBox(height: 20),
              TextField(
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
                  contentPadding: const EdgeInsets.all(15),
                ),
              ),
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton(
                    onPressed: () {
                      Navigator.pushReplacementNamed(context, '/home_profile');
                    },
                    style: TextButton.styleFrom(foregroundColor: tagColor),
                    child: const Text("Pass", style: TextStyle(fontSize: 16)),
                  ),
                  const SizedBox(width: 12),
                  ElevatedButton(
                    onPressed: () async {
                      bool isEmpty =
                          resRating == 0 &&
                          partnerRating == 0 &&
                          commentController.text.trim().isEmpty &&
                          selectedTags.isEmpty;

                      if (isEmpty) {
                        bool? confirm = await showDialog<bool>(
                          context: context,
                          builder:
                              (context) => AlertDialog(
                                title: const Text("Skip Feedback?"),
                                content: const Text(
                                  "Are you sure you want to skip giving feedback?",
                                ),
                                actions: [
                                  TextButton(
                                    onPressed:
                                        () => Navigator.pop(context, false),
                                    child: const Text("Cancel"),
                                  ),
                                  TextButton(
                                    onPressed:
                                        () => Navigator.pop(context, true),
                                    child: const Text("OK"),
                                  ),
                                ],
                              ),
                        );

                        if (confirm == true) {
                          Navigator.pushReplacementNamed(
                            context,
                            '/home_profile',
                          );
                        }
                        return;
                      }

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
