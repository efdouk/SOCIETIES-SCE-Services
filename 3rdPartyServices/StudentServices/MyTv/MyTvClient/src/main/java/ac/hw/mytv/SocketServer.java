/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ac.hw.mytv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.societies.api.useragent.monitoring.IUserActionMonitor;

import ac.hw.mytv.MyTvClient.CommandHandler;

public class SocketServer extends Thread{

	private ServerSocket server;
	private Socket client;
	private PrintWriter out;
	private BufferedReader in;
	private int port = 4321;
	private boolean listening = true;

	private static final String GUI_STARTED = "GUI_STARTED";
	private static final String GUI_STOPPED = "GUI_STOPPED";
	private static final String USER_ACTION = "USER_ACTION";
	private static final String CHANNEL_REQUEST = "CHANNEL_REQUEST";
	private static final String MUTED_REQUEST = "MUTED_REQUEST";
	private static final String RECEIVED = "RECEIVED";
	private static final String FAILED = "FAILED";
	private static final String START_MSG = "START_MSG";
	private static final String END_MSG = "END_MSG";

	private CommandHandler commandHandler;

	public SocketServer(CommandHandler commandHandler){
		this.commandHandler = commandHandler;
	}

	@Override
	public void run(){
		while(listening){
			listenSocket();
		}
	}

	public void listenSocket(){
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port "+port);
			e.printStackTrace();
		}

		try {
			System.out.println("Waiting for connection from GUI on port: "+port);
			client = server.accept();
		} catch (IOException e) {
			System.out.println("Accept failed: "+port);
			e.printStackTrace();
		}

		System.out.println("Connection accepted from GUI!");

		try {
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			System.out.println("Accept failed: "+port);
			e.printStackTrace();
		}

		try{
			String start = in.readLine();
			System.out.println("Got new input: "+start);
			if(start.equalsIgnoreCase(START_MSG)){
				System.out.println("Processing new message...");

				//loop to get rest of message
				String message = "";
				boolean reading = true;
				while(reading){
					String next = in.readLine();
					System.out.println("next = "+next);
					if(!next.equalsIgnoreCase(END_MSG)){
						message = message+next+"\n";
					}else{
						reading  = false;
					}
				}
				System.out.println("message = "+message);

				//handle message
				String[] splitData = message.split("\n");
				System.out.println("splitData length = "+splitData.length);
				String command = splitData[0];
				if (command.equalsIgnoreCase(GUI_STARTED)){
					System.out.println(GUI_STARTED+" message received");
					out.println(RECEIVED);
					String gui_ip = splitData[1];
					commandHandler.connectToGUI(gui_ip);

				}else if (command.equalsIgnoreCase(USER_ACTION)){
					System.out.println(USER_ACTION+" message received");
					out.println(RECEIVED);
					String parameterName = splitData[1];
					String value = splitData[2];
					commandHandler.processUserAction(parameterName, value);

				}else if(command.equalsIgnoreCase(CHANNEL_REQUEST)){
					System.out.println(CHANNEL_REQUEST+" message received");
					String response = commandHandler.getChannelPreference();
					out.println(response);

				}else if(command.equalsIgnoreCase(MUTED_REQUEST)){
					System.out.println(MUTED_REQUEST+" message received");
					String response = commandHandler.getMutedPreference();
					out.println(response);

				}else if (command.equalsIgnoreCase(GUI_STOPPED)){
					System.out.println(GUI_STOPPED+" message received");
					out.println(RECEIVED);
					commandHandler.disconnectFromGUI();
				}
				finalize();
			}
		} catch (IOException e) {
			System.out.println("Read failed");
			out.println(FAILED);
			finalize();
		}
	}


	@Override
	protected void finalize(){
		//Clean up 
		try{
			in.close();
			out.close();
			server.close();
		} catch (IOException e) {
			System.out.println("Could not close.");
		}
	}
}
