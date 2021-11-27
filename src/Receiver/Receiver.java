package Receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {
	static int timeoutSeconds = 2;
	static int receiverPort = 9876;
	static int senderPort = 9878;
	static DatagramSocket ReceiverSocket;
	static DatagramPacket receivedPacket;
	static InetAddress IPAddress;
	static int ACK = 100;
	static int SlowStart = 1;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setReceiverSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		System.out.println("Pronto para receber");
		int testando = 0;
		while (testando < 5) {
			// Recebe N mensagens, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				receiveMessage();
			}
			// Seta ip do sender para confirmar
			setIp();
			// Envia N conformações, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				sendMessage();
			}
			SlowStart = SlowStart * 2;
			testando++;
		}
		ReceiverSocket.close();
	}

	private static void sendMessage() {
		byte[] sendData = new byte[1024];
		String newACK = Integer.toString(ACK + SlowStart);
		sendData = newACK.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, senderPort);
		try {
			ReceiverSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao sender");
			e.printStackTrace();
		}
	}

	private static void receiveMessage() {
		// Recebimento de mensagem
		try {
			ReceiverSocket.receive(receivedPacket);
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao receber a mensagem");
		}
		// Mensagem enviada
		System.out.println("Mensagem: " + new String(receivedPacket.getData()));

	}

	private static void setIp() {
		// Ip do sender
		try {
			IPAddress = receivedPacket.getAddress();
		} catch (Exception e) {
			System.out.println();
		}

	}

	private static void declarePackage() {
		byte[] receiveData = new byte[1024];
		receivedPacket = new DatagramPacket(receiveData, receiveData.length);

	}

	private static void setReceiverSocket() {
		// DatagramSocket
		try {
			ReceiverSocket = new DatagramSocket(receiverPort);
		} catch (SocketException e1) {
			System.out.println("Ocorreu um erro ao setar o datagram");
		}
		// Configuração de timeout
//		try {
//			ReceiverSocket.setSoTimeout(timeoutSeconds * 1000);
//		} catch (SocketException e) {
//			System.out.println("Ocorreu um erro ao setar o time out");
//		}

	}
}
