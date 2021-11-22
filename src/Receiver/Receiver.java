package Receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {
	private static int timeoutSeconds = 1;
	private static int serverPort = 9876;
	private static DatagramPacket receivePacket;
	private static String message = "";
	private static InetAddress IPAddress;
	private static int port;
	private static DatagramSocket serverSocket;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o cliente
		setClientSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		// Obtem endereco ip do servidor com o DNS
		setIp();
		System.out.println("Pronto para receber");
		while (true) {
			// Recebe o pacote
			receiveMessage();
			break;
		}
		serverSocket.close();
	}

	private static void receiveMessage() {
		// Recebimento de mensagem
		try {
			serverSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao receber a mensagem");
		}
		// Mensagem enviada
		message = new String(receivePacket.getData());
		System.out.println("message: " + message);

	}

	private static void setIp() {
		// Ip do cliente
		try {
			IPAddress = receivePacket.getAddress();
		} catch (Exception e) {
			System.out.println();
		}
		// Porta
		port = receivePacket.getPort();

	}

	private static void declarePackage() {
		byte[] receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);

	}

	private static void setClientSocket() {
		// DatagramSocket
		try {
			serverSocket = new DatagramSocket(serverPort);
		} catch (SocketException e1) {
			System.out.println("Ocorreu um erro ao setar o datagram");
		}
		// Configuração de timeout
//		try {
//			serverSocket.setSoTimeout(timeoutSeconds * 1000);
//		} catch (SocketException e) {
//			System.out.println("Ocorreu um erro ao setar o time out");
//		}

	}
}
