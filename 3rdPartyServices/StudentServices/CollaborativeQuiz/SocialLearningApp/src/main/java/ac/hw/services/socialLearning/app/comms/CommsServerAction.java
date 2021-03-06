package ac.hw.services.socialLearning.app.comms;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ac.hw.services.socialLearning.api.ISocialLearningService;

public class CommsServerAction implements Runnable{
	
	private ISocialLearningService socialApp;
	private Socket clientSocket;
	
    private static final Logger log = LoggerFactory.getLogger(CommsServerAction.class);
    
    private static final String REQUEST_SERVER = "REQUEST_SERVER";
    private static final String REQUEST_USER_INTERESTS = "REQUEST_USER_INTERESTS";
    private static final String NULL_REPLY = "NULL";

	
	public CommsServerAction(ISocialLearningService socialApp, Socket clientSocket)
	{
		this.socialApp = socialApp;
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		
		try{
			
			BufferedReader stdIn = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			
			PrintWriter out = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()),true);
			String result;
			while(true)
			{
				result = stdIn.readLine();
				if(result.matches(REQUEST_SERVER))
				{
					String reply = socialApp.getServerIPPort();
					if(reply!=null)
					{
						out.println(reply);
					}
					else
					{
						out.println(NULL_REPLY);
					}
				}
				else if(result.matches(REQUEST_USER_INTERESTS))
				{
					String reply = objectToJSON(socialApp.getUserInterests());
					if(reply!=null)
					{
						out.println(reply);
					}
					else
					{
						out.println(NULL_REPLY);
					}
				}
			}
			}catch(Exception e)
			{
				log.debug("Error: " + e.toString());
			}
		
	}

		public String objectToJSON(Object data)
		{
			log.debug("Converting Object to JSON!");
			String jsonData = new Gson().toJson(data);
			if(jsonData!=null)
			{
				return jsonData;
			}
			else
			{
				return null;
			}
		}
}
