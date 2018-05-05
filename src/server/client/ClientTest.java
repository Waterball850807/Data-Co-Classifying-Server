package server.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class ClientTest {
	private static boolean run = true;
	private static OutputStream dataOutputStream;
	private static BufferedReader dataInputStream;
	private static String address;
	public static void main(String[] argv) throws UnknownHostException, IOException {
		final Scanner scanner = new Scanner(System.in);
		System.out.println("Please input the address: ");
		address = scanner.nextLine();
		System.out.println("Port: ");
		int port = scanner.nextInt();
		scanner.nextLine();
		final Socket client = new Socket(address, port);
		dataInputStream = new BufferedReader((new InputStreamReader(client.getInputStream(), "UTF-8")));
		dataOutputStream = client.getOutputStream();
		runListener();
		new Thread(){
			public void run() {
				try {
					while(run)
					{
						System.out.println("->" + address + " : ");
						String next = scanner.nextLine() + "\n";
						dataOutputStream.write(next.getBytes("UTF-8"));
						dataOutputStream.flush();
						if (next.equals("quit"))
							run = false;
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					run = false;
					try {
						client.close();
						scanner.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	
	private static void runListener(){
		new Thread(){
			@Override
			public void run() {
				while(run)
				{
					try {
						String message = dataInputStream.readLine();
						System.out.println("From " + address + " : " + message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
