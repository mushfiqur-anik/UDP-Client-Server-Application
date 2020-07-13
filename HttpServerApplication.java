package udp3;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class HttpServerApplication {
	
	public static void main(String[] args) throws IOException{ 
		boolean verbose = false; // Default verbose setting
		int port = 8080; // Default port number
		String directory = "/Users/mushfiquranik/Documents/Java Workspace/TestFolder"; // Default directory
		
		UDPServer server = new UDPServer(); // server object
		Scanner keyboard = new Scanner(System.in); // User input
		String httpsRequest;
		
		System.out.print("Please enter your https command (Enter 'quit' to quit application): ");
		httpsRequest = keyboard.nextLine();
		
		while(!httpsRequest.equalsIgnoreCase("quit")) { 
			
				String[] splitRequest = httpsRequest.split(" ");
				
				// Setting verbose, port, & directory
				for(int i = 0; i < splitRequest.length; i++) { 
					// Case:1
					if(splitRequest[i].contains("-v")) { 
						verbose = true;
						System.err.println(verbose);
					}
					
					// Case:2 
					if(splitRequest[i].contains("-p")) {
						port = Integer.parseInt(splitRequest[i+1]);
					}
					
					// Case:3
		            if(splitRequest[i].contains("-d")) {
						for (int j = i+1; j < splitRequest.length; j++) {
							directory += splitRequest[j];
						}
					} 
				}
				
				// For testing only
				System.out.println("");
				System.out.println("Starting server...");
				System.out.println("Verbose " + verbose);
				System.out.println("Running at port: " + port);
				System.out.println("Directory: " + directory);
				System.out.println(""); 
				
				// Connect to Server
				server.listenAndServe(verbose, port, directory);
				
				System.out.print("Please enter 'quit' if you want to quit else keep going by inputing any other command: ");
				httpsRequest = keyboard.nextLine();
			}
		
			System.out.println("Application terminated!!!");
		}
	}