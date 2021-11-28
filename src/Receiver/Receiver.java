package Receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {
	// Condição de parada
	static boolean stop = false;
	// Configs do socket
	static int timeoutSeconds = 2;
	static int receiverPort = 9876;
	static int senderPort = 9878;
	static DatagramSocket receiverSocket;
	static DatagramPacket receivedPacket;
	static InetAddress IPAddress;
	// Manipulador de arquivos
	static FileLumper fs = new FileLumper();
	// ACK
	static int ACK = 100;
	// Slow Star
	static int SlowStart = 1;
	// Qual o ultimo arquivo recebido
	static int lastReadFile = 0;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setReceiverSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		// Seta ip do sender
		setIp();
		System.out.println("Pronto para receber");
		while (!stop) {
			// Recebe N mensagens, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				if (!stop) {
					receiveMessage();
				}
			}
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
		receiverSocket.close();
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
			receiverSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao sender");
			e.printStackTrace();
		}
	}

	private static void receiveMessage() {
		try {
			receiverSocket.receive(receivedPacket);
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
		try {
			IPAddress = InetAddress.getByName("localhost");
		} catch (Exception e) {
			System.out.println("Ocorreu um erro ao obter o ip de destino");
		}

	}

	private static void declarePackage() {
		byte[] receiveData = new byte[1024];
		receivedPacket = new DatagramPacket(receiveData, receiveData.length);

	}

	private static void setReceiverSocket() {
		try {
			receiverSocket = new DatagramSocket(receiverPort);
			receiverSocket.setSoTimeout(timeoutSeconds * 1000);
		} catch (SocketException e1) {
			System.out.println("Ocorreu um erro ao setar o datagram");
		}
	}
}
