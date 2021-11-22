package Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Sender {
	private static int timeoutSeconds = 2;
	private static int receiverPort = 9876;
	private static int senderPort = 9878;
	private static DatagramSocket senderSocket;
	private static DatagramPacket receivedPacket;
	private static InetAddress IPAddress;
	private static String message = "";

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setSenderSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		// obtem endereco ip do receiver com o DNS
		setIp();
		System.out.println("pronto para enviar");
		while (true) {
			// Envia o pacote
			getMessage();
			sendMessage();
			receiveACK();
			break;
		}
		senderSocket.close();
	}

	private static void receiveACK() {
		// Recebimento de mensagem
		try {
			senderSocket.receive(receivedPacket);
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao receber a mensagem");
		}
		// Mensagem enviada
		message = new String(receivedPacket.getData());
		System.out.println("message: " + message);

	}

	private static void sendMessage() {
		byte[] sendData = new byte[1024];
		sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
		try {
			senderSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao receiver");
			e.printStackTrace();
		}
	}

	private static void getMessage() {
		message = "aaa";
	}

	private static void setIp() {
		try {
			IPAddress = InetAddress.getByName("localhost");
			System.out.println("Conectado ao receiver com sucesso");
		} catch (UnknownHostException e) {
			System.out.println("");
			System.out.println("erro durante obtencao do ip do receiver");
			e.printStackTrace();
		}
	}

	private static void declarePackage() {
		byte[] receiveData = new byte[1024];
		receivedPacket = new DatagramPacket(receiveData, receiveData.length);
	}

	private static void setSenderSocket() {
		try {
			senderSocket = new DatagramSocket(senderPort);
			// senderSocket.setSoTimeout(timeoutSeconds * 1000);
		} catch (SocketException e) {
			System.out.println("Erro! porta ja em uso");
		}
	}
}
