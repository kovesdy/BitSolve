package com.bitsolve.client;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.JFrame;

class TCPClient
{
	private static Socket clientSocket;
	private static Socket dataSocket;
	private static DataOutputStream clientStatus, dataOut;
	private static BufferedReader dataIn;
	private static boolean hasStream, bufferOverload;
	private static ArrayList<Integer> dataBuffer;
	private static Algorithm algorithm;
	
	public static void main(String argv[]) throws Exception
 	{
		hasStream = false;
		bufferOverload = false;
		init();
		while(true){
			clientStatus.write(createNewStatusPacket(false));
			if(dataIn.readLine() != null) dataBuffer.add(Integer.parseInt(dataIn.readLine()));
			if(algorithm.canAcceptData() && dataBuffer.size()>0){
				algorithm.acceptData(dataBuffer.get(0));
				dataBuffer.remove(0);
			}
			if(algorithm.canReleaseData()){
				dataOut.writeInt(algorithm.takeReleaseBuffer());
			}
			//Thread.sleep(500);
		}
	 // outToServer.writeBytes(sentence + '\n');
	 // modifiedSentence = inFromServer.readLine();
	  //System.out.println("FROM SERVER: " + modifiedSentence);
	  //centralGUI.outputLabel.setText("FROM SERVER: " + modifiedSentence);
	  //clientSocket.close();
 	}
	
	private static void init() throws UnknownHostException, IOException{
		dataBuffer = new ArrayList<Integer>();
		clientSocket = new Socket("localhost", 9912);
		dataSocket = new Socket("localhost", 9913);
		clientStatus = new DataOutputStream(clientSocket.getOutputStream());
		dataOut = new DataOutputStream(dataSocket.getOutputStream());
		dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
		
		//start the GUI
		BasicFrame centralGUI = new BasicFrame();
		centralGUI.setTitle("Client");
		centralGUI.setSize(300,300);
		centralGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		centralGUI.setVisible(true);
		
		algorithm = new Algorithm();
		algorithm.start();
	}
	
	private static byte[] createNewStatusPacket(boolean close){
		byte[] payload = new byte[2];
		//0-0-0 is idle
		//0-0-1 is data stream go
		//0-1-0 is data stream wait
		//0-1-1 is data stream end
		if(close){
			payload[0] = 1;
			payload[1] = 1;
			return payload;
		}
		if(!hasStream){
			payload[0] = 0;
			payload[1] = 0;
		} else{
			if(!bufferOverload){
				payload[0] = 1;
				payload[1] = 0;
			} else{
				payload[0] = 0;
				payload[1] = 1;
			}
		}
		return payload;
	}
}