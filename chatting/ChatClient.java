package chatting;

import java.awt.*;
import java.awt.event.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends JFrame implements Runnable, ActionListener {

	private static final long serialVersionUID = 1L;

	BufferedReader input;   //입력 스트림

	PrintWriter output;     //출력 스트림

	Thread handler;         //ChatHandler 와 메세지를 주고 받기 위한 쓰레드

	Container container;    //Container 레이아웃 메인 지정

	JTextArea display;      //채팅창에서 대화를 표시

	JTextField id;          //사용자 id

	JTextField inData;      //사용자가 메세지를 입력하는 필드

	JLabel displayId;       //채팅창에 id를 표시하는 레이블

	JButton send;           //보내기 전송 버튼

	CardLayout window;


	public static final String PROXY_NETWORK = "127.0.0.1";

	public static final int PROXY_PORT = 5000;


	/** 생성자로 채팅창의 UI를 구성함 */
	public ChatClient() {
		super("채팅 클라이언트");

		container = getContentPane();
		window = new CardLayout();
		container.setLayout(window);

		/* 로그인 창을 구성 */
		JPanel login = new JPanel(new BorderLayout());
		JPanel button = new JPanel();
		JLabel idLabel = new JLabel("로그인");

		/* 아이디 입력필드의 생성과 리스너 등록 */
		id = new JTextField(15);
		id.addActionListener(this);

		/* 로그인창의 컴포넌트 배치 */
		button.add(idLabel);
		button.add(id);
		login.add("South", button);
		login.setBackground(Color.CYAN);

		/* 채팅창을 구성 */
		JPanel chat = new JPanel(new BorderLayout());

		/* 채팅창의 대화 표시 텍스트에리어 생성 및 스크롤바 추가, 배치 */
		display = new JTextArea(10, 30);
		JScrollPane scrollPane = new JScrollPane(display);
		display.setBackground(Color.PINK);
		chat.add("Center", scrollPane); //패널 추가
		display.setEditable(false); //대화표시 화면에 임의로 입력 금지

		/* 채팅창의 메세지입력과 보내기 버튼 생성 및 배치 */
		JPanel panel = new JPanel();
		panel.add(new JLabel("메세지"));

		/* 메세지 입력필드의 생성과 리스너 등록, 배치 */
		inData = new JTextField(20);
		panel.add(inData);

		/* 보내기 버튼의 생성과 리스너 등록, 배치 */
		send = new JButton("보내기");
		panel.add(send);
		send.addActionListener(this);

		/* 채팅창의 컴포넌트 배치 */
		chat.add("South", panel);
		displayId = new JLabel();
		chat.add("North", displayId);
		chat.add("chat", chat);
		window.show(chat, "login");

		setSize(400, 400);
		setVisible(true);
	}

	/** ChatHandler 와 메세지를 주고받는 일을 하는 쓰레드를 생성 후 실행시킴 */
	public void onRunClientThread() {
		handler = new Thread(this);
		handler.start();
	}

	/** 쓰레드 핸들러인 ChatHandler 가 보낸 메세지를 받아서 대화화면에 표시 */
	public void execute() {
		try{
			while (true) { //무한히 메세지를 받음
				/* 받은 메세지를 대화화면에 표시 */
				String line = input.readLine();
				display.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			/* 메세지 받는 것이 중단될때 수행 */
			stop();
		}
	}

	public void stop() {
		if (handler != null) {
			try {
				if (output != null) {
					input.close();
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* 사용자 쓰레드 제거 */
		handler = null;
	}

	public static void main(String[] args) {
		ChatClient chatClient = new ChatClient();
		chatClient.setDefaultCloseOperation(EXIT_ON_CLOSE);
		chatClient.onRunClientThread();
	}

	/** 사용자의 아이디 처리와 쓰레드 핸들러로 메세지를 보내는 것을 처리 */
	@Override
	public void actionPerformed(ActionEvent event) {
		/* 이벤트가 발생한 컴포넌트 얻어냄 */
		Component component = (Component) event.getSource();

		/* 이벤트가 발생한 컴포넌트가 아이디 입력필드이면 수행 */
		if (component == id) {
			/* 아이디를 얻어내 채팅창의 제일위에 표시 */
			String name = id.getText().trim();
			displayId.setText(name);

			/* 아이디를 입력하지 않으면 실행중단. */
			if (name.length() == 0) {
				return;
			}

			/* 쓰레드 핸들러인 ChatHandler 로 메세지 보냄 */
			output.println(name);
			output.flush();

			/* 채팅창이 표시되도록함 */
			window.show(container, "chat");
			inData.requestFocus();
		} else if (component == inData || component == send) { //이벤트의 발생이 메세지 이볅 필드나 보내기 버튼이면 수행
			/* 쓰레드 핸들러인 ChatHandler 로 메세지 보냄 */
			output.println(inData.getText());
			output.flush();
		}
	}

	/** 쓰레드를 실행시 자동으로 실행 */
	@Override
	public void run() {
		try {
			/* 소켓객체 생성 */
			Socket socket = new Socket(PROXY_NETWORK, PROXY_PORT);

			/* 입출력스트림 얻어냄 */
			input = new BufferedReader(
					new InputStreamReader(socket.getInputStream())
			);
			output = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream())
			);

			/* 쓰레드 핸들러가 보낸 메세지를 받는 execute() 메소드 */
			execute();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}