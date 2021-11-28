package Receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import common.NetPackage;
import common.Serializer;

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
	static Serializer ser = new Serializer();
	// Slow Star
	static int SlowStart = 1;
	// Qual o ultimo arquivo recebido
	static int seq = 0;
	// ACK
	static int ACK = 100;

	public static void main(String[] args) {
		System.out.println("Iniciando...");
		// Configura socket para o sender
		setReceiverSocket();
		// Declara o pacote a ser recebido
		declarePackage();
		// Seta ip do sender
		setIp();
		System.out.println("Pronto para receber");

		// Fluxo de recebimento de mensagens e confirmações de entrega
		boolean okToContinue = true;
		while (!stop) {
			okToContinue = true;
			System.out.println("/////////////////////");
			System.out.println("seq: " + seq);
			System.out.println("SlowStart: " + SlowStart);
			System.out.println("ACK: " + ACK);
			// Recebe N mensagens, de acordo com slow start
			for (int i = 0; i < SlowStart; i++) {
				// Confição de parada
				if (stop || !okToContinue) {
					break;
				}
				// Recebimento de mensagem
				okToContinue = receiveMessage();

			}
			if (!okToContinue) {
				recoverFromError();
			}
			// Envia uma confirmação
			sendMessage();

			if (okToContinue && SlowStart < 10) {
				SlowStart = SlowStart * 2;
			}
			System.out.println("/////////////////////");
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

	// Envio de mensagem
	// Confirma que recebeu os arquivos
	private static void sendMessage() {
		System.out.println("entrou");
		byte[] sendData = new byte[1024];
		ACK = 100 + SlowStart;
		System.out.println("enviando ACK:" + ACK);
		String newACK = Integer.toString(ACK);
		sendData = newACK.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, senderPort);
		try {
			receiverSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao sender");
			e.printStackTrace();
		}
	}

	// Recebimento de mensagens
	// Recebe N pedaços do arquivo, de acordo com slow start
	private static boolean receiveMessage() {
		try {
			// Recebe uma mensagem
			byte[] buffer = new byte[1024 * 4];
			receiverSocket.receive(new DatagramPacket(buffer, buffer.length));
			// Verifica se é um aviso de parada
			if (shouldStop(buffer)) {
				System.out.println("era parada");
				stop = true;
			} else {
				// Se nao deve, é um novo arquivo
				NetPackage np = (NetPackage) ser.deserialize(buffer);
				seq++;
				fs.saveFile(seq, np.getFileArray());
			}
			return true;
		} catch (Exception e) {
			System.out.println("--------------");
			e.printStackTrace();
			System.out.println("Ocorreu um erro ao receber a mensagem");
			return false;
		}
	}

	// Logica de recuperação em caso de erro
	private static void recoverFromError() {
		
	}

	// Verificação de condição de parada
	private static boolean shouldStop(byte[] buffer) {
		String message = new String(buffer);
		if (message.contains("DONE")) {
			return true;
		}
		return false;
	}

	// Config de IP
	private static void setIp() {
		try {
			IPAddress = InetAddress.getByName("localhost");
		} catch (Exception e) {
			System.out.println("Ocorreu um erro ao obter o ip de destino");
		}

	}

	// Config de pacote
	private static void declarePackage() {
		byte[] receiveData = new byte[1024];
		receivedPacket = new DatagramPacket(receiveData, receiveData.length);

	}

	// Config do socket
	private static void setReceiverSocket() {
		try {
			receiverSocket = new DatagramSocket(receiverPort);
			// receiverSocket.setSoTimeout(timeoutSeconds * 1000);
		} catch (SocketException e1) {
			System.out.println("Ocorreu um erro ao setar o datagram");
		}
	}
}
