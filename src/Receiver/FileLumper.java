package Receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class FileLumper {
	// Caminho relativo para o arquivo
	String path = "src/Receiver/files/";
	// Nome do arquivo gerado
	String fileName = "File.pdf";

	public FileLumper() {
		// TODO Auto-generated constructor stub
	}

	// Salva um arquivo recebido
	public void saveFile(int lastReadFileNumer, byte[] fileByteArray) throws IOException {
		// Corrige nome do arquivo
		String lastReadFile = Integer.toString(lastReadFileNumer);
		while (lastReadFile.length() < 3) {
			lastReadFile = "0" + lastReadFile;
		}
		System.out.println("arquivo: " + lastReadFile);
		String composedName = path + fileName + "." + lastReadFile;
		File f = new File(composedName); // Creating the file
		FileOutputStream outToFile = new FileOutputStream(f);
		outToFile.write(fileByteArray);
		outToFile.close();
	}

	public void mergeFiles() throws IOException {
		System.out.println("mergeando arquivos");
		String composedName = path + fileName;
		try (FileOutputStream fos = new FileOutputStream(composedName);
				BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
			for (File f : listOfFilesToMerge()) {
				Files.copy(f.toPath(), mergingStream);
			}
		}

	}

	public List<File> listOfFilesToMerge() {
		String composedName = path + fileName + ".001";
		File tmpFile = new File(composedName);
		String tmpName = tmpFile.getName();// {name}.{number}
		String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));// remove .{number}
		File[] files = tmpFile.getParentFile()
				.listFiles((File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
		Arrays.sort(files);// ensuring order 001, 002, ..., 010, ...
		return Arrays.asList(files);
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
