package Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class Sender {
	static int timeoutSeconds = 2;
	static int receiverPort = 9876;
	static int senderPort = 9878;
	static DatagramSocket senderSocket;
	static DatagramPacket receivedPacket;
	static InetAddress IPAddress;
	static ArrayList<String> messages;
	static int SlowStart = 1;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setSenderSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		// obtem endereco ip do receiver com o DNS
		setIp();
		System.out.println("pronto para enviar");
		int testando = 0;
		while (testando < 5) {
			// Define o array de mensagens a enviar
			getMessage();
			// Envia as mensagens, N vezes, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				sendMessage(i);
			}
			// Recebe as confirmações N vezes, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				receiveACK();
			}
			SlowStart = SlowStart * 2;
			testando++;
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
		String ACK = new String(receivedPacket.getData());
		System.out.println("ACK: " + ACK);

	}

	private static void sendMessage(int i) {
		byte[] sendData = new byte[1024];
		sendData = messages.get(i).getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
		try {
			senderSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao receiver");
			e.printStackTrace();
		}
	}

	private static void getMessage() {
		messages = new ArrayList<String>();
		// TODO: Pedaços do arquivo
		String newMessage = "Mensagem: " + SlowStart + " - ";
		for (int i = 0; i < SlowStart; i++) {
			messages.add(newMessage + i);
		}
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
