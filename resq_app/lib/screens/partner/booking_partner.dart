import 'package:flutter/material.dart';
import '../../services/partner_service.dart';
import './partner_feedback.dart';

class BookingPartnerScreen extends StatefulWidget {
  final int rrId;
  final int partnerId;
  final String paymentMethod;
  const BookingPartnerScreen({
    super.key,
    required this.rrId,
    required this.partnerId,
    required this.paymentMethod,
  });

  @override
  State<BookingPartnerScreen> createState() => _BookingPartnerScreenState();
}

class _BookingPartnerScreenState extends State<BookingPartnerScreen> {
  Offset position = const Offset(50, 100);
  bool isMenuOpen = false;

  void toggleMenu() {
    setState(() => isMenuOpen = !isMenuOpen);
  }

  void closeMenu() {
    if (isMenuOpen) setState(() => isMenuOpen = false);
  }

  Future<void> markArrived() async {
    //  / // await PartnerService().partnerArrived(widget.rrId);
  }

  void showCancelRescuePopup() {
    final TextEditingController _reasonController = TextEditingController();
    String? errorText;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
              ),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text(
                    "You will be charged a cancellation fee!",
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                      color: Color(0xFF013171),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Align(
                    alignment: Alignment.centerLeft,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          "+ Cancellation fee: 50,000 VND",
                          style: TextStyle(fontSize: 16),
                        ),
                        Text(
                          "+ Reason: You canceled after being assigned",
                          style: TextStyle(fontSize: 16),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _reasonController,
                    decoration: InputDecoration(
                      labelText: "Enter cancellation reason",
                      labelStyle: const TextStyle(color: Color(0xFF013171)),
                      focusedBorder: const OutlineInputBorder(
                        borderSide: BorderSide(color: Color(0xFF013171)),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderSide: BorderSide(
                          color: errorText != null ? Colors.red : Colors.grey,
                        ),
                      ),
                    ),
                    maxLines: 2,
                  ),
                  if (errorText != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 4.0, left: 4.0),
                      child: Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          errorText!,
                          style: const TextStyle(
                            color: Colors.red,
                            fontSize: 12,
                          ),
                        ),
                      ),
                    ),
                  const SizedBox(height: 20),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFFBB0000),
                        ),
                        onPressed: () => Navigator.pop(context),
                        child: const Text(
                          "Decline",
                          style: TextStyle(color: Colors.white),
                        ),
                      ),
                      const SizedBox(width: 20),
                      ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF013171),
                        ),
                        onPressed: () async {
                          if (_reasonController.text.trim().isEmpty) {
                            setState(() {
                              errorText = "Reason is required to cancel";
                            });
                            return;
                          }
                          // await PartnerService().partnerCancel(
                          //   rrid: widget.rrId,
                          //   partnerId: widget.partnerId,
                          //   cancelNote: _reasonController.text.trim(),
                          // );
                          Navigator.pop(context);
                          closeMenu();
                        },
                        child: const Text(
                          "Confirm",
                          style: TextStyle(color: Colors.white),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Future<void> completeRescue() async {
    // await PartnerService().partnerComplete(widget.rrId);
    // Navigator.push(
    //   context,
    //   MaterialPageRoute(
    //     builder:
    //         (context) => PartnerFeedbackScreen(
    //           rrid: widget.rrId,
    //           partnerId: widget.partnerId,
    //           paymentMethod: widget.paymentMethod,
    //         ),
    //   ),
    // );
  }

  Widget menuItem(String title) {
    return TextButton(
      onPressed: () async {
        closeMenu();
        if (title == "Arrived") {
          await markArrived();
        } else if (title == "Cancel Trip") {
          showCancelRescuePopup();
        } else if (title == "Complete") {
          await completeRescue();
        } else {
          print("Selected: $title");
        }
      },
      child: Text(title, style: const TextStyle(fontSize: 16)),
    );
  }

  // Từ đây lên  => đẩy bill
  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final screenHeight = MediaQuery.of(context).size.height;

    const double menuWidth = 150;
    const double menuHeight = 200;
    const double screenPadding = 10;
    const double buttonSize = 40;
    const double menuTopSpacing = 8;

    double dropdownLeft = position.dx;
    double dropdownTop = position.dy + buttonSize + menuTopSpacing;

    if (dropdownLeft + menuWidth > screenWidth - screenPadding) {
      dropdownLeft = screenWidth - menuWidth - screenPadding;
    }
    if (dropdownLeft < screenPadding) dropdownLeft = screenPadding;
    if (dropdownTop + menuHeight > screenHeight - screenPadding) {
      dropdownTop = screenHeight - menuHeight - screenPadding;
    }

    return Scaffold(
      body: Stack(
        children: [
          Positioned.fill(
            child: Container(
              color: Colors.grey[200],
              child: const Center(
                child: Text(
                  'Booking Partner Screen',
                  style: TextStyle(fontSize: 24),
                ),
              ),
            ),
          ),

          // Lấy hết xuống => trên cùng
          if (isMenuOpen)
            Positioned.fill(
              child: GestureDetector(
                onTap: closeMenu,
                behavior: HitTestBehavior.translucent,
                child: Container(color: Colors.transparent),
              ),
            ),
          Positioned(
            left: position.dx,
            top: position.dy,
            child: GestureDetector(
              onPanUpdate: (details) {
                const double margin = 10;
                const double buttonSize = 56;

                setState(() {
                  double newX = position.dx + details.delta.dx;
                  double newY = position.dy + details.delta.dy;

                  if (newX < margin) newX = margin;
                  if (newX > screenWidth - buttonSize - margin) {
                    newX = screenWidth - buttonSize - margin;
                  }
                  if (newY < margin) newY = margin;
                  if (newY > screenHeight - buttonSize - margin) {
                    newY = screenHeight - buttonSize - margin;
                  }

                  position = Offset(newX, newY);
                });
              },
              child: FloatingActionButton(
                onPressed: toggleMenu,
                mini: true,
                backgroundColor: Colors.blue,
                child: const Icon(Icons.menu),
              ),
            ),
          ),
          if (isMenuOpen)
            Positioned(
              left: dropdownLeft,
              top: dropdownTop,
              child: Container(
                width: menuWidth,
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(8),
                  boxShadow: [
                    BoxShadow(
                      blurRadius: 5,
                      color: Colors.black26,
                      offset: Offset(2, 2),
                    ),
                  ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    menuItem("Arrived"),
                    // menuItem("Message"),
                    menuItem("Cancel Trip"),
                    menuItem("Complete"),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}
