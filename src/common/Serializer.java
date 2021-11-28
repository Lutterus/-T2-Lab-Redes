package common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class Serializer {

	public byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		byte[] data = baos.toByteArray();
		return data;
	}

	public Object deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
		Object readObject = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(bais);
		try {
			readObject = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No object could be read from the received UDP datagram.");
		}
		return readObject;
	}

}