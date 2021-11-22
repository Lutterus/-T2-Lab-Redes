package Receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {
	private static int timeoutSeconds = 2;
	private static int receiverPort = 9876;
	private static int senderPort = 9878;
	private static DatagramSocket ReceiverSocket;
	private static DatagramPacket receivedPacket;
	private static InetAddress IPAddress;
	private static String message = "ACK";

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setReceiverSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		System.out.println("Pronto para receber");
		while (true) {
			// Recebe o pacote
			receiveMessage();
			setIp();
			sendMessage();
			break;
		}
		ReceiverSocket.close();
	}

	private static void sendMessage() {
		byte[] sendData = new byte[1024];
		sendData = message.getBytes();
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
		System.out.println("message: " + new String(receivedPacket.getData()));

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
