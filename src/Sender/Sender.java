package Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Sender {
	private static int timeoutSeconds = 2;
	private static int serverPort = 9876;
	private static int clientPort = 9877;
	private static InetAddress IPAddress;
	private static DatagramSocket clientSocket;
	private static String myName = "";
	private static String message = "";
	private static String question = "";
	private static String lastReceivedMessage = "";
	private static int maxAttempsToRegister = 10;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o cliente
		setClientSocket();
		// obtem endereco ip do servidor com o DNS
		setIp();

		System.out.println("pronto para enviar");
		// Envia o pacote
		while (true) {
			// Recebe o pacote
			sendMessage();
		}
	}

	private static void sendMessage() {
		String sentence = "aaaa";
		byte[] sendData = new byte[1024];
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao servidor");
			e.printStackTrace();
		}

	}

	private static void setIp() {
		try {
			IPAddress = InetAddress.getByName("localhost");
			System.out.println("Conectado ao servidor com sucesso");
		} catch (UnknownHostException e) {
			System.out.println("");
			System.out.println("erro durante obtencao do ip do servidor");
			e.printStackTrace();
		}
	}

	private static void setClientSocket() {
		boolean foundAvailablePort = false;
		while (!foundAvailablePort) {
			System.out.println("Tentando a porta: " + clientPort);
			try {
				clientSocket = new DatagramSocket(clientPort);
				foundAvailablePort = true;
				clientSocket.setSoTimeout(timeoutSeconds * 1000);
			} catch (SocketException e) {
				System.out.println("Erro! porta ja em uso");
				clientPort++;
			}
		}
		System.out.println("Porta livre encontrada");
	}
}
