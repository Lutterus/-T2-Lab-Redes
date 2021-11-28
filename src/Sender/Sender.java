package Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.NetPackage;
import common.Serializer;

public class Sender {
	// Nome do arquivo a ser lido.
	// OBS: O arquivo deve estar na mesma pasta /files
	static String fileName = "enunciado.pdf";
	// Configs do socket
	static int timeoutSeconds = 2;
	static int receiverPort = 9876;
	static int senderPort = 9878;
	static DatagramSocket senderSocket;
	static DatagramPacket receivedPacket;
	static InetAddress IPAddress;
	// Mensagens a serem lida em uma iteração
	static ArrayList<NetPackage> messages;
	// Manipulador de arquivos
	static FileSplitter fs;
	static int fileChunks = 0;
	static Serializer ser = new Serializer();
	// Slow start
	static int SlowStart = 1;
	// Qual o ultimo arquivo enviado
	static int seq = 0;
	// ACK
	static int ACK = 101;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// -------------------------- //
		System.out.print("Quebrando o arquivo ");
		System.out.print(fileName);
		System.out.print(" em pedaços");
		System.out.println("");
		fs = new FileSplitter(fileName);
		try {
			fileChunks = fs.split() - 1;
			System.out.println("Arquivo quebrado em " + fileChunks + " com sucesso");
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao manipular o arquivo");
		}
		// -------------------------- //
		System.out.println("Realizando configurações");
		// Configura socket para o sender
		setSenderSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		// Seta ip do receiver
		setIp();
		System.out.println("Pronto para enviar");
		// -------------------------- //

		// Fluxo de envio de mensagens e confirmações de entrega
		boolean okToContinue = true;
		while (seq < fileChunks) {
			okToContinue = true;
			System.out.println("/////////////////////");
			System.out.println("seq: " + seq);
			System.out.println("SlowStart: " + SlowStart);
			System.out.println("ACK: " + ACK);
			// Define o array de mensagens a enviar
			getMessage();
			// Envia as mensagens, N vezes, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				sendMessage(i);
			}
			// Recebe uma confirmação
			okToContinue = receiveACK();

			if (okToContinue && SlowStart < 10) {
				SlowStart = SlowStart * 2;
			}
			System.out.println("/////////////////////");
		}
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");
		sendFinalMessage();
		senderSocket.close();
	}

	// Recebimento de ACK
	// Confirmação do cliente de que recebeu corretamente os arquivos
	private static boolean receiveACK() {
		try {
			senderSocket.receive(receivedPacket);
			String receivedACK = new String(receivedPacket.getData());
			receivedACK = receivedACK.replaceAll("[^\\d.]", "");
			ACK = Integer.parseInt(receivedACK);
			System.out.println("ACK recebida: " + ACK);
			return true;

		} catch (Exception e) {
			System.out.println("--------------");
			System.out.println("Erro ao receber ACK");
			recoverFromError();
			return false;
		}
	}

	// Logica de recuperação em caso de erro
	private static void recoverFromError() {

	}

	// Envio de mensagens
	// Envia N pedaços do arquivo, de acordo com slow start
	private static void sendMessage(int index) {
		try {
			if (index < messages.size()) {
				byte[] sendData = ser.serialize(messages.get(index));
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
				senderSocket.send(sendPacket);
			}

		} catch (

		IOException e1) {
			System.out.println("Ocorreu um erro ao enviar a mensagem");
			e1.printStackTrace();
		}
	}

	// Logica de obtenção dos pedaços do arquivo a serem enviados
	private static void getMessage() {
		messages = new ArrayList<NetPackage>();
		for (int i = 0; i < SlowStart; i++) {
			seq++;
			if (seq > fileChunks) {
				break;
			}
			NetPackage np = new NetPackage();
			np.createObj(ACK, seq, fs.getFile(seq));
			messages.add(np);
		}

	}

	// Config de IP
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

	// Config de pacote
	private static void declarePackage() {
		byte[] receiveData = new byte[1024];
		receivedPacket = new DatagramPacket(receiveData, receiveData.length);
	}

	// Config do socket
	private static void setSenderSocket() {
		try {
			senderSocket = new DatagramSocket(senderPort);
			// senderSocket.setSoTimeout(timeoutSeconds * 1000);
		} catch (SocketException e) {
			System.out.println("Erro! porta ja em uso");
		}
	}

	// Envio da mensagem de condição de parada
	private static void sendFinalMessage() {
		byte[] sendData = new byte[1024];
		String message = "DONE";
		sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
		try {
			senderSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao receiver");
			e.printStackTrace();
		}
	}
}
