package Sender;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class FileSplitter {
	// Caminho relativo para o arquivo
	String path = "src/Sender/files/";
	// Nome do arquivo
	String fileName = "";
	// Para criar os arquivos com nome "arq001";
	int fileNameCounter = 1;
	// Tamanho fixo para cara arquivo
	byte[] buffer = new byte[1000];
	// Arquivo montado
	File file;

	public FileSplitter(String fileName) {
		String composedName = path + fileName;
		this.fileName = fileName;
		file = new File(composedName);
	}

	// Quebra o arquivo em pedaços de tamanho fixo
	public int split() throws IOException {
		try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
			int bytesAmount = 0;
			while ((bytesAmount = bis.read(buffer)) > 0) {
				String filePartName = String.format("%s.%03d", fileName, fileNameCounter++);
				File newFile = new File(file.getParent(), filePartName);
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					out.write(buffer, 0, bytesAmount);
				}
			}
		}
		return fileNameCounter;
	}

	// Recupera o pedaço N do arquivo
	public File getFile(int lastReadFileNumer) {
		// Corrige nome do arquivo
		String lastReadFile = Integer.toString(lastReadFileNumer);
		while (lastReadFile.length() < 3) {
			lastReadFile = "0" + lastReadFile;
		}
		System.out.println("arquivo: " + lastReadFile);
		String composedName = path + fileName + "." + lastReadFile;
		return new File(composedName);
	}

	public void printMD5() throws Exception {
		String composedName = path + fileName;
		byte[] bytes = createMd5(composedName);
		String string = Base64.getEncoder().encodeToString(bytes);
		System.out.println(string);
	}

	public byte[] createMd5(String composedName) throws Exception {
		MessageDigest md_1 = MessageDigest.getInstance("MD5");
		InputStream is_1 = new FileInputStream(composedName);
		try {
			is_1 = new DigestInputStream(is_1, md_1);
		} finally {
			is_1.close();
		}
		byte[] digest_1 = md_1.digest();
		return digest_1;
	}
}
