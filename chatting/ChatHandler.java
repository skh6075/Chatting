package chatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatHandler extends Thread{

	Socket socket; //소켓 객체

	BufferedReader input; //입력 스트림

	PrintWriter output; //출력 스트림

	ChatServer server; //서버 객체

	ChatClient chatClient; //채팅 객체


	/** 생성자, 소켓객체로부터 입출력 스트림을 얻어냄 */
	public ChatHandler(ChatServer server, Socket socket) throws IOException{
		this.server = server;
		this.socket = socket;

		input = new BufferedReader(
				new InputStreamReader(socket.getInputStream())
		);
		output = new PrintWriter(
				new OutputStreamWriter(socket.getOutputStream())
		);
	}

	/** 클라이언트가 보낸 메세지를 읽는 메소드 */
	public void run() {
		String name = "";
		try {
			/* 클라이언트가 보낸 메세지를 읽음 */
			name = input.readLine();
			broadcastMessage(name + "님 입장.");

			/* 무한히 클라이어트가 보낸 메세지를 받을 수 있도록 무한루프로 처리 */
			while (true) {
				/* 클라이언트가 보낸 메세지 읽음 */
				String message = input.readLine();
				broadcastMessage(name + " > " + message);
			}
		} catch (Exception ex) {
			System.out.println(name + "님 퇴장, " + "  IP: " + socket.getInetAddress());
		} finally { //채팅창의 닫기단주를 클릭하면 수행
			server.handlers.removeElement(this);
			broadcastMessage(name + " 님 퇴장");

			try {
				input.close();
				output.close();
				socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/** 현재 접속한 모든 클라이언트에게 메세지를 보냄 */
	protected void broadcastMessage(String message) {
		//모든 사용자들에게 메세지를 중계하는 동안
		//백터에서 클라이언트의 추가와 제거가 안됨

		synchronized (server.handlers) {
			// 현재 백터안에 있는 클라이언트의 수를 얻어냄
			int onlineCount = server.handlers.size();

			// 접속한 모든 사용자에게 메세지를 보내기 위해 사용자의 수만큼 반복
			for (int i = 0; i < onlineCount; i ++) {
				ChatHandler handler = server.handlers.elementAt(i); // 클라이언트 하나를 얻어냄
				try {
					synchronized (handler.output) {
						handler.output.println(message); // 클라이언트에게 메세지를 보냄
					}
					handler.output.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
