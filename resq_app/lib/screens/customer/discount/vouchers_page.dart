import 'package:flutter/material.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class VouchersPage extends StatefulWidget {
  const VouchersPage({super.key});

  @override
  State<VouchersPage> createState() => _VouchersPageState();
}

class _VouchersPageState extends State<VouchersPage>
    with SingleTickerProviderStateMixin {
  int? userId = loginResponse?.userId;
  late TabController _tabController;
  final List<String> _tabTitles = [
    "App discounts",
    "Rank discounts",
    "My discounts",
  ];

  List<dynamic> _appDiscounts = [];
  List<dynamic> _rankDiscounts = [];
  List<dynamic> _myDiscounts = [];
  bool _loading = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _tabTitles.length, vsync: this);
    _fetchAppDiscounts();
    _tabController.addListener(() {
      if (_tabController.indexIsChanging) {
        switch (_tabController.index) {
          case 0:
            _fetchAppDiscounts();
            break;
          case 1:
            _fetchRankDiscounts();
            break;
          case 2:
            _fetchMyDiscounts();
            break;
        }
      }
    });
  }

  Future<void> _fetchAppDiscounts() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await CustomerService.getAppDiscount(userId!);
      setState(() {
        _appDiscounts = data;
      });
    } catch (e) {
      setState(() {
        _error = "Failed to load discounts: $e";
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  Future<void> _fetchRankDiscounts() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await CustomerService.getRankDiscount(userId!);
      setState(() {
        _rankDiscounts = data;
      });
    } catch (e) {
      setState(() {
        _error = "Failed to load discounts: $e";
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  Future<void> _fetchMyDiscounts() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await CustomerService.getMyDiscount(userId!);
      setState(() {
        _myDiscounts = data;
      });
    } catch (e) {
      setState(() {
        _error = "Failed to load discounts: $e";
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  Future<void> _claimDiscount(int discountId) async {
    try {
      String result = await CustomerService.claimDiscount(discountId, userId!);
      if (result == "success") {
        _showDialog("Claim Success", "You have claimed new discount!");
        _fetchAppDiscounts();
        _fetchRankDiscounts();
        _fetchMyDiscounts();
      } else if (result == "no-change-left") {
        _showDialog(
          "Claim Failed",
          "You have no discount redemptions left for this month!",
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Failed to claim: $e')));
    }
  }

  void _showDialog(String title, String message) {
    final isFailed = title.toLowerCase().contains("failed");
    showDialog(
      context: context,
      builder:
          (_) => AlertDialog(
            title: Center(
              child: Text(
                title,
                style: TextStyle(
                  color: isFailed ? Colors.red[700] : Colors.green[900],
                  fontWeight: FontWeight.bold,
                  fontSize: 23,
                ),
              ),
            ),
            content: Text(message),
          ),
    );

    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        Navigator.of(context, rootNavigator: true).pop();
      }
    });
  }

  Widget _buildDiscountItem(dynamic discount, {bool showClaimButton = false}) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Stack(
        children: [
          Positioned.fill(
            child: Opacity(
              opacity: 0.75,
              child: Image.asset(
                'assets/images/discount_bg.png',
                fit: BoxFit.cover,
              ),
            ),
          ),
          Card(
            color: Colors.transparent,
            elevation: 0,
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  Padding(
                    padding: const EdgeInsets.only(left: 16),
                    child: SizedBox(
                      width: 100,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children:
                            discount['percent'] == true
                                ? [
                                  Text(
                                    "${discount['amount'] ?? 0}%",
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 16,
                                    ),
                                  ),
                                ]
                                : [
                                  Text(
                                    NumberFormat.decimalPattern(
                                      'vi_VN',
                                    ).format(discount['amount'] ?? 0),
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 16,
                                    ),
                                  ),
                                  const Text(
                                    'VND',
                                    style: TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ],
                      ),
                    ),
                  ),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          discount['code'] ?? 'No code',
                          style: const TextStyle(
                            fontFamily: "Lexend",
                            fontSize: 15,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          discount['name'] ?? 'No name',
                          style: const TextStyle(
                            fontFamily: "Lexend",
                            fontSize: 13,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (showClaimButton)
                    ElevatedButton(
                      onPressed: () => _claimDiscount(discount['id']),
                      child: const Text("Claim"),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF013171),
                        foregroundColor: Colors.white,
                        textStyle: const TextStyle(
                          fontFamily: "Raleway",
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                        ),
                        minimumSize: const Size(10, 25),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 6,
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDiscountList(
    List<dynamic> discounts, {
    required bool showClaim,
  }) {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    } else if (_error != null) {
      return Center(child: Text(_error!));
    } else if (discounts.isEmpty) {
      return const Center(
        child: Text(
          "No discount",
          style: TextStyle(
            fontFamily: 'Lexend',
            fontSize: 18,
            color: Colors.black54,
            fontStyle: FontStyle.italic,
          ),
        ),
      );
    } else {
      return ListView.builder(
        itemCount: discounts.length,
        itemBuilder: (context, index) {
          return _buildDiscountItem(
            discounts[index],
            showClaimButton: showClaim,
          );
        },
      );
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: _tabTitles.length,
      child: Scaffold(
        backgroundColor: Colors.white,
        appBar: const CommonAppBar(title: 'Discounts'),
        body: Column(
          children: [
            Container(
              height: 48,
              color: Colors.white,
              child: Theme(
                data: Theme.of(context).copyWith(
                  splashColor: Colors.transparent,
                  highlightColor: Colors.transparent,
                ),
                child: TabBar(
                  controller: _tabController,
                  indicator: const BoxDecoration(
                    border: Border(
                      bottom: BorderSide(color: Color(0xFF013171), width: 3.0),
                    ),
                  ),
                  indicatorColor: Colors.transparent,
                  labelColor: const Color(0xFF013171),
                  unselectedLabelColor: Colors.black38,
                  tabs: List.generate(_tabTitles.length, (index) {
                    return Tab(
                      child: Text(
                        _tabTitles[index],
                        style: TextStyle(
                          fontFamily: "Raleway",
                          fontSize: 14,
                          fontWeight:
                              _tabController.index == index
                                  ? FontWeight.w900
                                  : FontWeight.w700,
                        ),
                      ),
                    );
                  }),
                ),
              ),
            ),
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  _buildDiscountList(_appDiscounts, showClaim: true),
                  _buildDiscountList(_rankDiscounts, showClaim: true),
                  _buildDiscountList(_myDiscounts, showClaim: false),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
