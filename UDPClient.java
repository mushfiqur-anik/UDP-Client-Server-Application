package udp3;

import static java.nio.channels.SelectionKey.OP_READ;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;

public class UDPClient {
	
	// GET REQUEST 
	public void getRequest(SocketAddress routerAddr, InetSocketAddress serverAddress, String clientRequest) throws IOException {
		// Variables
		String clientPayload;
		String method = "GET";
		String url;
		String version = "HTTP/1.0";
		String header = null;
		String[] splitRequest;
		int sequenceNumber = 1;
		int length;
		
		// Preparing http client request
		splitRequest = clientRequest.split(" ");
		length = splitRequest.length;
		
		url = splitRequest[length-1];
		
		for(int i = 0; i < length; i++) { 
			if(splitRequest[i].contains("-h")) { 
				header = splitRequest[i+1];
			}
		}
		
		// http client payload
		clientPayload = method + " " + url + " " + version +  "\\r\\n" + header + "\\r\\n\\r\\n";
		
		//------------------ENTIRE GET REQUEST OPERATION------------------------
		try(DatagramChannel channel = DatagramChannel.open()) {
			boolean booleanValue = false;
			sequenceNumber++;

			// Three way handshake
			do {
				String msg = "SYN";
				
				// Creating packet
				Packet p = new Packet.Builder()
						.setType(1)
						.setSequenceNumber(sequenceNumber)
						.setPortNumber(serverAddress.getPort())
						.setPeerAddress(serverAddress.getAddress())
						.setPayload(msg.getBytes())
						.create();
				channel.send(p.toBuffer(), routerAddr);
				System.out.println("CLIENT: Sending SYN request to the router address: " + routerAddr);

				// Try to receive a packet within timeout.
				channel.configureBlocking(false);
				Selector selector = Selector.open();
				channel.register(selector, OP_READ);
				System.out.println("Waiting for the response... ");
				selector.select(15000);

				Set<SelectionKey> keys = selector.selectedKeys();

				// If there is no response within timeout
				if(keys.isEmpty()){
					System.out.println("No response after timeout");
				}

				else {

				// We just want a single response.
				ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
				SocketAddress router = channel.receive(buf);
				buf.flip();
				Packet resp = Packet.fromBuffer(buf);
				String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

				System.out.println("CLIENT: Received SYN-ACK from server and connected to server!");
				System.out.println("");

				if(resp.getType() == 2 && resp.getSequenceNumber() == sequenceNumber) { 
					booleanValue = true;
				}

					keys.clear();
				} 

			} while(booleanValue == false);  
			//-------------------End of three way handshake-------------------

		
			// ------------------Sending Get Request--------------------------
			boolean booleanValue2 = false;
			
			do {
				sequenceNumber++;

				Packet p = new Packet.Builder()
						.setType(0)
						.setSequenceNumber(1L)
						.setPortNumber(serverAddress.getPort())
						.setPeerAddress(serverAddress.getAddress())
						.setPayload(clientPayload.getBytes())
						.create();
				channel.send(p.toBuffer(), routerAddr);

				System.out.println("CLIENT: Sending this message: " + clientPayload + " to the router address" + routerAddr);

				// Try to receive a packet within timeout.
				channel.configureBlocking(false);
				Selector selector = Selector.open();
				channel.register(selector, OP_READ);
				System.out.println("Waiting for the response....");
				System.out.println(" ");
				selector.select(15000);

				Set<SelectionKey> keys = selector.selectedKeys();

				if(keys.isEmpty()){
					System.out.println("No response after timeout");
				}

				else {
					// We just want a single response.
					ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
					SocketAddress router = channel.receive(buf);
					buf.flip();
					Packet resp = Packet.fromBuffer(buf);
					String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

					System.out.println("CLIENT: Payload: " + payload);
					System.out.println("");
					
					booleanValue2 = true;

					keys.clear();
				} 

			} while(booleanValue2 == false);
			
			
			// --------------Closing connection-------------------------
			boolean booleanValue3 = false;
			
			do {
				String msg = "FIN";
				sequenceNumber++;
				
				Packet p = new Packet.Builder()
						.setType(3)
						.setSequenceNumber(sequenceNumber)
						.setPortNumber(serverAddress.getPort())
						.setPeerAddress(serverAddress.getAddress())
						.setPayload(msg.getBytes())
						.create();
				channel.send(p.toBuffer(), routerAddr);
				System.out.println("CLIENT: Sending FIN request to the router address: " + routerAddr);

				// Try to receive a packet within timeout.
				channel.configureBlocking(false);
				Selector selector = Selector.open();
				channel.register(selector, OP_READ);
				System.out.println("Waiting for the response... ");
				selector.select(15000);

				Set<SelectionKey> keys = selector.selectedKeys();

				if(keys.isEmpty()){
					System.out.println("No response after timeout");
				}

				else {

				// We just want a single response.
				ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
				SocketAddress router = channel.receive(buf);
				buf.flip();
				Packet resp = Packet.fromBuffer(buf);
				String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

				System.out.println("CLIENT: Received FIN-ACK from server and connected to server!");
				System.out.println("");

				if(resp.getType() == 4 && resp.getSequenceNumber() == sequenceNumber) { 
					booleanValue3 = true;
				}

					keys.clear();
				} 

			} while(booleanValue3 == false); 
			//-------------END OF CLOSING CONNECTION-------------------
			
			System.out.println("END OF CLIENT");

		} // End Of Get Request
	}
	
	    // POST REQUEST 
		public void postRequest(SocketAddress routerAddr, InetSocketAddress serverAddress, String clientRequest) throws IOException {
			// Variables
			String clientPayload;
			String entityBody = null;
			String method = "POST";
			String url;
			String version = "HTTP/1.0";
			String header = null;
			String[] splitRequest;
			int sequenceNumber = 1;
			int length;
			
			splitRequest = clientRequest.split(" ");
			length = splitRequest.length;
			
			url = splitRequest[length-1];
			
			for(int i = 0; i < length; i++) { 
				if(splitRequest[i].contains("-h")) { 
					header = splitRequest[i+1];
				}
				
				if(splitRequest[i].contains("-d")) {
					entityBody = splitRequest[i+1];
				}
			}
			
			clientPayload = method + " " + url + " " + version +  "\\r\\n" + header + "\\r\\n\\r\\n" + entityBody;
			
			// Sending to Server 
			try(DatagramChannel channel = DatagramChannel.open()) {
				boolean booleanValue = false;
				sequenceNumber++;

				// Three way handshake
				do {
					String msg = "SYN";
				
					Packet p = new Packet.Builder()
							.setType(1)
							.setSequenceNumber(sequenceNumber)
							.setPortNumber(serverAddress.getPort())
							.setPeerAddress(serverAddress.getAddress())
							.setPayload(msg.getBytes())
							.create();
					channel.send(p.toBuffer(), routerAddr);
					System.out.println("CLIENT: Sending SYN request to the router address: " + routerAddr);

					// Try to receive a packet within timeout.
					channel.configureBlocking(false);
					Selector selector = Selector.open();
					channel.register(selector, OP_READ);
					System.out.println("Waiting for the response... ");
					selector.select(15000);

					Set<SelectionKey> keys = selector.selectedKeys();

					if(keys.isEmpty()){
						System.out.println("No response after timeout");
					}

					else {

					// We just want a single response.
					ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
					SocketAddress router = channel.receive(buf);
					buf.flip();
					Packet resp = Packet.fromBuffer(buf);
					String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

					System.out.println("CLIENT: Received SYN-ACK from server and connected to server!");
					System.out.println("");

					if(resp.getType() == 2 && resp.getSequenceNumber() == sequenceNumber) { 
						booleanValue = true;
					}

						keys.clear();
					} 

				} while(booleanValue == false);  // End of three way handshake


				// ------------------Sending Post Request----------------------------
				boolean booleanValue2 = false;
				sequenceNumber++;
				
				do {	
					Packet p = new Packet.Builder()
							.setType(0)
							.setSequenceNumber(sequenceNumber)
							.setPortNumber(serverAddress.getPort())
							.setPeerAddress(serverAddress.getAddress())
							.setPayload(clientPayload.getBytes())
							.create();
					channel.send(p.toBuffer(), routerAddr);

					System.out.println("CLIENT: Sending this message: " + clientPayload + " to the router address" + routerAddr);

					// Try to receive a packet within timeout.
					channel.configureBlocking(false);
					Selector selector = Selector.open();
					channel.register(selector, OP_READ);
					System.out.println("Waiting for the response....");
					System.out.println(" ");
					selector.select(15000);

					Set<SelectionKey> keys = selector.selectedKeys();

					if(keys.isEmpty()){
						System.out.println("No response after timeout");
					}

					else {
						// We just want a single response.
						ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
						SocketAddress router = channel.receive(buf);
						buf.flip();
						Packet resp = Packet.fromBuffer(buf);
						String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

						System.out.println("CLIENT: Payload: " + payload);
						System.out.println("");
						
						booleanValue2 = true;

						keys.clear();
					} 

				} while(booleanValue2 == false);
				// ------------------END of Sending Post Request-------------------
				

				// ---------------------Closing connection-------------------------
				boolean booleanValue3 = false;
				sequenceNumber++;
				
				do {
					String msg = "FIN";
					
					Packet p = new Packet.Builder()
							.setType(3)
							.setSequenceNumber(sequenceNumber)
							.setPortNumber(serverAddress.getPort())
							.setPeerAddress(serverAddress.getAddress())
							.setPayload(msg.getBytes())
							.create();
					channel.send(p.toBuffer(), routerAddr);
					System.out.println("CLIENT: Sending FIN request to the router address: " + routerAddr);

					// Try to receive a packet within timeout.
					channel.configureBlocking(false);
					Selector selector = Selector.open();
					channel.register(selector, OP_READ);
					System.out.println("Waiting for the response... ");
					selector.select(15000);

					Set<SelectionKey> keys = selector.selectedKeys();

					if(keys.isEmpty()){
						System.out.println("No response after timeout");
					}

					else {

					ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
					SocketAddress router = channel.receive(buf);
					buf.flip();
					Packet resp = Packet.fromBuffer(buf);
					String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

					System.out.println("CLIENT: Received FIN-ACK from server and connected to server!");
					System.out.println("");

					if(resp.getType() == 4 && resp.getSequenceNumber() == sequenceNumber) { 
						booleanValue3 = true;
					}

						keys.clear();
					} 

				} while(booleanValue3 == false);  
				// ---------------------END Of Closing connection-----------------------
				
				System.out.println("END OF CLIENT");
			
			} 
			
		} // End of post request

	public static void main(String[] args) throws IOException {
		
		try {
		
			int serverPort = 8210; // Server port
			InetSocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName("localhost"), serverPort); // Server Address
			SocketAddress routerAddr = new InetSocketAddress("localhost", 3000); // router
			
			UDPClient udpc = new UDPClient();
			Scanner keyboard = new Scanner(System.in);
			String clientRequest = null;
			
			System.out.print("Please enter you client request: ");
			clientRequest = keyboard.nextLine();
			
			if(clientRequest.contains("get") || clientRequest.contains("GET")) { 
				udpc.getRequest(routerAddr, serverAddress, clientRequest);
			}
			
			else { 
				udpc.postRequest(routerAddr, serverAddress, clientRequest);
			}
			

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}