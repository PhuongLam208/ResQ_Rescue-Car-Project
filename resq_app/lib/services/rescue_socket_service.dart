import 'dart:convert';

import 'package:stomp_dart_client/stomp_dart_client.dart';

class RescueSocketService {
  final int partnerId;
  final void Function(Map<String, dynamic>) onRescueRequestReceived;

  late StompClient _client;

  RescueSocketService({
    required this.partnerId,
    required this.onRescueRequestReceived,
  });

  void connect() {
    _client = StompClient(
      config: StompConfig.sockJS(
        // url: 'http://172.16.2.3:9090/ws', // thay đổi thành host của bạn
        url: 'http://10.0.2.2:9090/ws',
        onConnect: _onConnect,
        onWebSocketError: (error) => print('❌ WebSocket error: $error'),
        onStompError: (frame) => print('❗ STOMP error: ${frame.body}'),
        onDisconnect: (frame) => print('🔌 Disconnected'),
        onDebugMessage: (msg) => print('🐛 STOMP Debug: $msg'),
        heartbeatOutgoing: const Duration(seconds: 10),
        heartbeatIncoming: const Duration(seconds: 10),
      ),
    );

    _client.activate();
  }

  void _onConnect(StompFrame frame) {
    print('✅ STOMP Connected');

    _client.subscribe(
      destination: '/topic/rescue/partner/$partnerId',
      callback: (frame) {
        if (frame.body != null) {
          final data = json.decode(frame.body!);
          onRescueRequestReceived(data);
        }
      },
    );
  }

  void disconnect() {
    _client.deactivate();
  }
}
