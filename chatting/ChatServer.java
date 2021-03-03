package chatting;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer{

	Vector<ChatHandler> handlers;


	public ChatServer(int port) {
		try {
			ServerSocket socket = new ServerSocket(port); //서버 소켓 객체생성, 사용자는 최대 50까지 받을 수 있음
			ChatClient client = new ChatClient();
			handlers = new Vector<ChatHandler>(); 		  //클라이언트를 관리하는 백테생성
			System.out.println("채팅 서버 준비 완료");

			while (true) { // 무한히 클라이언트를 받을 수 있도록 무한루프 처리
				Socket accept = socket.accept(); //소켓객체 얻어냄
				System.out.println("접속한 클라이언트 IP: " + accept.getInetAddress());
				ChatHandler handler = new ChatHandler(this, accept); // 클라이언트당 1개씩 ChatHandler 객체 생성
				handlers.addElement(handler); //클라이언트 관리 백터에 접속한 클라이언트 추가
				handler.start(); //ChatHandler 클래스의 run()메소드가 호출됨
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ChatServer(ChatClient.PROXY_PORT);
	}
}