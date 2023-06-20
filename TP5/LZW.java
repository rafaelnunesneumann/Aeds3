import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class LZW {

	public HashMap<String, Integer> dicionario;
	private String[] charArray;
	private int count;

	public LZW() {
		dicionario = new HashMap<String, Integer>();
	}

	public void comprimir(String arquivoEntrada, String arquivoSaida) throws IOException {

		/** Cria as variaveis */

		charArray = new String[4096];
		for (int i = 0; i < 256; i++) {
			dicionario.put(Character.toString((char) i), i);
			charArray[i] = Character.toString((char) i);
		}
		count = 256;

		/** Ponteiro dos arquivos de entrada e saida */
		DataInputStream read = new DataInputStream(new BufferedInputStream(
				new FileInputStream(arquivoEntrada)));

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(arquivoSaida)));

		Byte input_byte;
		String temp = "";
		byte[] buffer = new byte[3];
		boolean onleft = true;

		try {

			/** Le o primeiro caracter */
			input_byte = read.readByte();
			int i = input_byte.intValue();
			if (i < 0) {
				i += 256;
			}
			char c = (char) i;
			temp = "" + c;

			/** Le todos os caracteres um por um */
			while (true) {
				input_byte = read.readByte();
				i = input_byte.intValue();

				if (i < 0) {
					i += 256;
				}
				c = (char) i;

				if (dicionario.containsKey(temp + c)) {
					temp = temp + c;
				} else {
					String s12 = to12bit(dicionario.get(temp));
					/**
					 * Salva os 12 bits em um array, depois escreve no arquivo de saida
					 */

					if (onleft) {
						buffer[0] = (byte) Integer.parseInt(
								s12.substring(0, 8), 2);
						buffer[1] = (byte) Integer.parseInt(
								s12.substring(8, 12) + "0000", 2);
					} else {
						buffer[1] += (byte) Integer.parseInt(
								s12.substring(0, 4), 2);
						buffer[2] = (byte) Integer.parseInt(
								s12.substring(4, 12), 2);
						for (int b = 0; b < buffer.length; b++) {
							out.writeByte(buffer[b]);
							buffer[b] = 0;
						}
					}
					onleft = !onleft;
					if (count < 4096) {
						dicionario.put(temp + c, count++);
					}
					temp = "" + c;
				}
			}

		} catch (EOFException e) {
			String temp_12 = to12bit(dicionario.get(temp));
			if (onleft) {
				buffer[0] = (byte) Integer.parseInt(temp_12.substring(0, 8), 2);
				buffer[1] = (byte) Integer.parseInt(temp_12.substring(8, 12)
						+ "0000", 2);
				out.writeByte(buffer[0]);
				out.writeByte(buffer[1]);
			} else {
				buffer[1] += (byte) Integer
						.parseInt(temp_12.substring(0, 4), 2);
				buffer[2] = (byte) Integer
						.parseInt(temp_12.substring(4, 12), 2);
				for (int b = 0; b < buffer.length; b++) {
					out.writeByte(buffer[b]);
					buffer[b] = 0;
				}
			}
			read.close();
			out.close();
		}

	}

	/** Converter de 8 bit para 12 */
	public String to12bit(int i) {
		String temp = Integer.toBinaryString(i);
		while (temp.length() < 12) {
			temp = "0" + temp;
		}
		return temp;
	}

	public int getValue(byte b1, byte b2, boolean onleft) {
		String temp1 = Integer.toBinaryString(b1);
		String temp2 = Integer.toBinaryString(b2);
		while (temp1.length() < 8) {
			temp1 = "0" + temp1;
		}
		if (temp1.length() == 32) {
			temp1 = temp1.substring(24, 32);
		}
		while (temp2.length() < 8) {
			temp2 = "0" + temp2;
		}
		if (temp2.length() == 32) {
			temp2 = temp2.substring(24, 32);
		}

		/** Onleft quando verdadeiro */
		if (onleft) {
			return Integer.parseInt(temp1 + temp2.substring(0, 4), 2);
		} else {
			return Integer.parseInt(temp1.substring(4, 8) + temp2, 2);
		}

	}

	public void descomprimir(String input, String output) throws IOException {
		/** Cria as variaveis */

		charArray = new String[4096];
		for (int i = 0; i < 256; i++) {
			dicionario.put(Character.toString((char) i), i);
			charArray[i] = Character.toString((char) i);
		}
		count = 256;

		DataInputStream in = new DataInputStream(new BufferedInputStream(
				new FileInputStream(input)));

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(output)));

		int currword, priorword;
		byte[] buffer = new byte[3];
		boolean onleft = true;
		try {

			/**
			 * Pega a primeira palavra e salva seu caracter correspondente
			 */
			buffer[0] = in.readByte();
			buffer[1] = in.readByte();

			priorword = getValue(buffer[0], buffer[1], onleft);
			onleft = !onleft;
			out.writeBytes(charArray[priorword]);

			/**
			 * Le de 3 em 3 bytes, e gera o caracter correspondente
			 */
			while (true) {

				if (onleft) {
					buffer[0] = in.readByte();
					buffer[1] = in.readByte();
					currword = getValue(buffer[0], buffer[1], onleft);
				} else {
					buffer[2] = in.readByte();
					currword = getValue(buffer[1], buffer[2], onleft);
				}
				onleft = !onleft;
				if (currword >= count) {

					if (count < 4096)
						charArray[count] = charArray[priorword]
								+ charArray[priorword].charAt(0);
					count++;
					out.writeBytes(charArray[priorword]
							+ charArray[priorword].charAt(0));
				} else {

					if (count < 4096)
						charArray[count] = charArray[priorword]
								+ charArray[currword].charAt(0);
					count++;
					out.writeBytes(charArray[currword]);
				}
				priorword = currword;
			}

		} catch (EOFException e) {
			in.close();
			out.close();
		}

	}
}