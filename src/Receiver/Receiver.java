package Receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {
	// Condição de parada
	static boolean stop = false;
	static int timeoutSeconds = 2;
	static int receiverPort = 9876;
	static int senderPort = 9878;
	static DatagramSocket ReceiverSocket;
	static DatagramPacket receivedPacket;
	static InetAddress IPAddress;
	static int ACK = 100;
	static int SlowStart = 1;
	// Qual o ultimo arquivo recebido
	static int lastReadFile = 0;
	// Manipulador de arquivos
	static FileLumper fs = new FileLumper();

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setReceiverSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		System.out.println("Pronto para receber");
		while (!stop) {
			// Recebe N mensagens, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				if (!stop) {
					receiveMessage();
				}
			}
			// Seta ip do sender para confirmar
			setIp();
			// Envia N conformações, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				if (!stop) {
					sendMessage();
				}
			}
			if (SlowStart < 10) {
				SlowStart = SlowStart * 2;
			}
		}
		ReceiverSocket.close();
		// Monta o arquivo
		try {
			fs.mergeFiles();
			System.out.println("Arquivo final pronto");
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao mergear os arquivos");
			e.printStackTrace();
		}
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

	// Recebimento de mensagem
	private static void receiveMessage() {
		try {
			ReceiverSocket.receive(receivedPacket);
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao receber a mensagem");
		}
		try {
			lastReadFile++;
			String message = new String(receivedPacket.getData());
			if (message.contains("DONE")) {
				stop = true;
			} else {
				fs.saveFile(lastReadFile, receivedPacket.getData());
			}
		} catch (Exception e) {
			System.out.println("Ocorreu um erro ao escrevr o arquivo");
		}
	}

	private static void setIp() {
		// Ip do sender
		try {
			IPAddress = receivedPacket.getAddress();
		} catch (Exception e) {
			System.out.println("Ocorreu um erro ao obter o ip de destino");
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
