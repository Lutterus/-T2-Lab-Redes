package Sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
	static ArrayList<File> messages;
	// Manipulador de arquivos
	static FileSplitter fs;
	static int fileChunks = 0;
	// Slow start
	static int SlowStart = 1;
	// Qual o ultimo arquivo enviado
	static int lastReadFile = 0;

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
		while (lastReadFile < fileChunks) {
			// Define o array de mensagens a enviar
			getMessage();
			// Envia as mensagens, N vezes, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				sendMessage(i);
			}
			// Recebe uma confirmação
			receiveACK();

			if (SlowStart < 10) {
				SlowStart = SlowStart * 2;
			}
		}
		sendFinalMessage();
		senderSocket.close();
	}

	// Recebimento de ACK
	// Confirmação do cliente de que recebeu corretamente os arquivos
	private static void receiveACK() {
		try {
			senderSocket.receive(receivedPacket);
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao receber a mensagem");
		}
	}

	// Envio de mensagens
	// Envia N pedaços do arquivo, de acordo com slow start
	private static void sendMessage(int index) {
		byte[] sendData = new byte[1024];
		FileInputStream fis = null;
		try {
			if (index < messages.size()) {
				fis = new FileInputStream(messages.get(index));
				fis.read(sendData);
				fis.close();
			}

		} catch (IOException e1) {
			System.out.println("Ocorreu um erro ao obter o arquivo para enviar");
			e1.printStackTrace();
		}
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
		try {
			senderSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao receiver");
			e.printStackTrace();
		}
	}

	// Logica de obtenção dos pedaços do arquivo a serem enviados
	private static void getMessage() {
		messages = new ArrayList<File>();
		for (int i = 0; i < SlowStart; i++) {
			lastReadFile++;
			if (lastReadFile > fileChunks) {
				break;
			}
			messages.add(fs.getFile(lastReadFile));
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
			senderSocket.setSoTimeout(timeoutSeconds * 1000);
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
