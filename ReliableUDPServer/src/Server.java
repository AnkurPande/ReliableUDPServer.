import java.net.*;
import java.io.*;



public class Server {
	DatagramSocket DGSock = null;
	ProtogramPacket lastReceived = new ProtogramPacket();
	ProtogramPacket lastSent = new ProtogramPacket();;
	DatagramPacket packetDock = null;
	byte[] bbuff = null;
	byte[] bytes = null;
	String response = "ProtogramPacket lastReceived = new ProtogramPacket()ProtogramPacket lastSent = new ProtogramPacket();fkdshfkbs;";
	String request = null;
	long responseSize = 0;
	int port = 0;
	int bytesRead = 0;
	int bookmark = 0;
	final int MAX_SIZE = 80;
	
	
	public void recieve(){
		//usage checking
    	try { port = Integer.parseInt("4444"); } catch ( Exception e ) {
    	    System.out.println("Usage: java UrftServer [port]");
    	    System.exit(-1);
    	}
    	System.out.println("Server Started at port"+port);
    	
    	//setup
    	try {
    	    bbuff = new byte[1000];
    	    DGSock = new DatagramSocket(port);
    	    packetDock = new DatagramPacket(bbuff, bbuff.length);
    	    
    	    try {
    	    	DGSock.receive(packetDock);
	    		DGSock.setSoTimeout(1000);
	    		lastReceived = ProtogramPacket.fromDatagram(packetDock);
	    		request = new String(lastReceived.payload);
	    
	    	} catch ( Exception e ) {
    	    	System.out.println("Received bad request!\nTerminating program...");
    	    	System.exit(-1);
    	    }
    	    
    	    long offset = 0;
    	    responseSize = response.length();
    	
    	    //traffic loop
		    while ( lastReceived.end < responseSize ) {
			
			//create next packet to send		
			bytes = new byte[MAX_SIZE+32];
			offset = responseSize -lastReceived.end;
			if(offset>=MAX_SIZE) {
				bytes = response.substring(bookmark, MAX_SIZE).getBytes();
				bytesRead = response.substring(bookmark, MAX_SIZE).length();
			}
			else{
				bytes = response.substring(bookmark, response.length()-1).getBytes();
				bytesRead = response.length() -bookmark ;
			} 
			
			
			lastSent = new ProtogramPacket(bookmark, bookmark+bytesRead, bytes,
										lastReceived.asDatagram().getAddress(),
										lastReceived.asDatagram().getPort());
	
			//guarantee delivery
			while (true) {
			    try {
				DGSock.send(lastSent.asDatagram());
				DGSock.receive(packetDock);
				lastReceived = ProtogramPacket.fromDatagram(packetDock);
				if ( lastReceived.end < lastSent.end ) continue;
				break;
			    } catch ( SocketTimeoutException e ) {
			    } catch ( Exception e ) {
				e.printStackTrace();
			    }
			}
			if ( (bookmark < responseSize) && (bytesRead > 0) ) bookmark += bytesRead;
		    }
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 }
    	
    	//FIN cycle
    	lastSent = new ProtogramPacket(-1, -1, lastReceived.asDatagram().getAddress(), lastReceived.asDatagram().getPort());
    	for ( int i=0; i<20; i++ ) {
    	   try {
    			DGSock.send(lastSent.asDatagram());
    			break;
    		} catch ( SocketTimeoutException e ) {
    		    	System.out.println("Client hung. Will terminate momentarily.");
    		} catch ( Exception e ) { e.printStackTrace(); }
    		}    	
	}
	
	public static void main (String[] args) {
    	    	
    }
}
