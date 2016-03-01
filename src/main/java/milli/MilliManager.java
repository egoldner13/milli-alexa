package milli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import milli.SkillContext;
import milli.MilliDevice;

public class MilliManager {
    private static MilliDevice CURRENT_APPLIANCE;

    private static final String cmd_prefix = "ssh ssn@hacklap net_mgr -d ";
    public  final String help_text = "With Milli, you can extend the capibilities of your smart home"
			  		+ " and get information about the status of your appliances."
					+ "You could say did I close the garage door? or are my bedroom lights on?"
					+ "Now, how can I help you?" ;
    /**
     *
     * Initialize all known devices, this action would be replaced with 
     * a mysql db if trying to expand product outwards 
	 */
    
    public final MilliDevice GARAGE_DOOR = new MilliDevice("garage door", "fd04:7c3e:be2f:1:213:5005:ff06:6213", "door");
    public final MilliDevice LIGHT = new MilliDevice("light", "fd04:7c3e:be2f:1:213:5005:ff06:621c", "switch");
    public final MilliDevice TEMP = new MilliDevice("temperature", "fd04:7c3e:be2f:1:213:5005:ff06:6259", "gauge");
    public final MilliDevice GAS = new MilliDevice("gas", "fd04:7c3e:be2f:1:213:5005:ff06:6217", "gauge");    
    public final MilliDevice CAR = new MilliDevice("car", "fd04:7c3e:be2f:1:213:5005:ff06:6214", "state");  
     
    /**
     * Prepares the speech to reply to the user. Obtain status of appliance specified by the user
     * and prepares statement to ask user if they want to change that status.
     * 
     * @param intent
     *            the intent object which contains the date slot
     * @param session
     *            the session object
     * @return getAskSpeechletResponse() to get response from user
     */
    public SpeechletResponse handleGetStatusRequest(Intent intent, Session session) {	
    	Slot applianceSlot = intent.getSlot("appliance");
    	String name = applianceSlot.getValue();
    	
        String speech_output = "";
        if (name.equals("garage door")){
        	CURRENT_APPLIANCE = GARAGE_DOOR;
        } else if (name.equals("light")){
        	CURRENT_APPLIANCE = LIGHT;
        } else {
        	//appliance name was not recognized 
        	speech_output = "I'm sorry, but I could not find an appliance named " + name;
            return getTellSpeechletResponse(speech_output);
        }
        String action;
        String reaction;
        String changeStatusAction;
        String changeStatusText;
        //int status = getStatusOfAppliance(CURRENT_APPLIANCE);
        int status = 1;
        if (status == 1){
        	action = CURRENT_APPLIANCE.getType().equals("door") ? "open" : "off";
        	reaction = CURRENT_APPLIANCE.getType().equals("door") ? "close it?" : "turn it on?";
        	speech_output = "the " + CURRENT_APPLIANCE.getName() + " is " + action + ", would you like me to " + reaction;
        	
        	changeStatusAction = "clr 16";
        	changeStatusText = CURRENT_APPLIANCE.getType().equals("door") ? "closing the garage door" : "turning on the light";
        	
        } else {
        	action = CURRENT_APPLIANCE.getType().equals("door") ? "closed" : "on";
        	reaction = CURRENT_APPLIANCE.getType().equals("door") ? "open it?" : "turn it off?";
        	speech_output = "the " + CURRENT_APPLIANCE.getName() + " is " + action + ", would you like me to " + reaction;
        	
        	changeStatusAction = "set 16";
        	changeStatusText = CURRENT_APPLIANCE.getType().equals("door") ? "opening the garage door" : "turning off the light";
        }
        
        CURRENT_APPLIANCE.setNextAction(changeStatusAction);
        CURRENT_APPLIANCE.setNextText(changeStatusText);
        return getAskSpeechletResponse(speech_output, help_text);
     }
    
    /**
     * Sends net_mgr command to get status of appliance
     * 
     * @param current appliance 
     *            the object which we are sending the command
     *            
     * @return int status of appliance (1 = off/closed, 0 = on/open)
     */
    public int getStatusOfAppliance(MilliDevice CURRENT_APPLIANCE){
    	String cmd = cmd_prefix + CURRENT_APPLIANCE.getMac_addr() + " mnic io get 17"; 
    	//execute net manager command
    	Runtime rt = Runtime.getRuntime();
    	Process pr = null;
    	try {
			pr = rt.exec(cmd);
		} catch (IOException e1) {
			//To Do: log error
		}
    	if (pr.equals(null)) return -1;
    	
    	InputStream stdin = pr.getInputStream();
    	InputStreamReader isr = new InputStreamReader(stdin);
    	BufferedReader br = new BufferedReader(isr);
    	String response = null;
		try {
			response = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		if (response.equals(null)) return -1;
		
    	//net_mgr -d fd04:7c3e:be2f:1:213:5005:ff06:621c mnic io get 17
    	//Response is “IO 17 = 1” for light is off.
		return response.charAt(response.length()-1);
    
    }

    /**
     * Prepares the speech to reply to the user. User has indicated that they would like to change status
     * of appliance.  Issues net manager command and change status of appliance in the db.
     *
     * @param session
     *            object containing session attributes with events list and index
     * @return SpeechletResponse object with voice/card response to return to the user
     * 
     */
    public SpeechletResponse handleChangeStatusRequest(Session session) {
    	String speechText = "Ok" + CURRENT_APPLIANCE.getNextAction();

    	//prepare net manager command
    	String cmd = cmd_prefix + CURRENT_APPLIANCE.getMac_addr() + " mnic io " + CURRENT_APPLIANCE.getNextAction();

    	//execute net manager command
    	Runtime rt = Runtime.getRuntime();
    	Process pr = null;
    	try {
			pr = rt.exec(cmd);
		} catch (IOException e1) {
			//To Do: log error
		}
    	if (pr.equals(null)) return getTellSpeechletResponse("Failure to issue net manager command");

    	//UNCOMMENT WHEN LOGGING ADDED
//    	InputStream stdin = pr.getInputStream();
//    	InputStreamReader isr = new InputStreamReader(stdin);
//    	BufferedReader br = new BufferedReader(isr);
//    	String response = null;
//		try {
//			response = br.readLine();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

    	return getTellSpeechletResponse(speechText);	

     }
    
    public SpeechletResponse handleGetTempRequest(Intent intent, Session session) {
    	String speechText = "the temperature is ";

    	//prepare net manager command
    	String cmd = cmd_prefix + "fd04:7c3e:be2f:1:213:5005:ff06:6259 mnic io adc 30";

    	//execute net manager command
    	Runtime rt = Runtime.getRuntime();
    	Process pr = null;
    	try {
			pr = rt.exec(cmd);
		} catch (IOException e1) {
			//To Do: log error
		}
    	if (pr.equals(null)) return getTellSpeechletResponse("Failure to issue net manager command");

    	InputStream stdin = pr.getInputStream();
    	InputStreamReader isr = new InputStreamReader(stdin);
    	BufferedReader br = new BufferedReader(isr);
    	String response = null;
		try {
			response = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response.equals(null)) return getTellSpeechletResponse("Error: Unable in net manager command response");
		String temp = response.substring(0, response.length()-2);
		
		speechText = speechText + temp + "milli volts";

    	return getTellSpeechletResponse(speechText);	

    }


    public SpeechletResponse handleGetGasLevelRequest(Intent intent, Session session) {
    	String speechText = "the gas level is at";

    	//prepare net manager command
    	String cmd = cmd_prefix + "fd04:7c3e:be2f:1:213:5005:ff06:6217 mnic io adc 23";

    	//execute net manager command
    	Runtime rt = Runtime.getRuntime();
    	Process pr = null;
    	try {
			pr = rt.exec(cmd);
		} catch (IOException e1) {
			//To Do: log error
		}
    	if (pr.equals(null)) return getTellSpeechletResponse("Failure to issue net manager command");

    	InputStream stdin = pr.getInputStream();
    	InputStreamReader isr = new InputStreamReader(stdin);
    	BufferedReader br = new BufferedReader(isr);
    	String response = null;
		try {
			response = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response.equals(null)) return getTellSpeechletResponse("Error: Unable in net manager command response");
		String gasLevel = response.substring(0, response.length()-2);
		
		speechText = speechText + gasLevel + "milli volts";

    	return getTellSpeechletResponse(speechText);	

    }
    
    public SpeechletResponse handleStatusOfCarRequest(Intent intent, Session session) {
    	//prepare net manager command
    	String cmd = cmd_prefix + "fd04:7c3e:be2f:1:213:5005:ff06:6214 mnic io get 17";

    	//execute net manager command
    	Runtime rt = Runtime.getRuntime();
    	Process pr = null;
    	try {
			pr = rt.exec(cmd);
		} catch (IOException e1) {
			//To Do: log error
		}
    	if (pr.equals(null)) return getTellSpeechletResponse("Failure to issue net manager command");

    	InputStream stdin = pr.getInputStream();
    	InputStreamReader isr = new InputStreamReader(stdin);
    	BufferedReader br = new BufferedReader(isr);
    	String response = null;
		try {
			response = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response.equals(null)) return getTellSpeechletResponse("Error: Unable in net manager command response");
		int status = response.charAt(response.length()-1);
		
		String speechText = status == 0 ? "the car is outside the garage" : "the car is not outside the garage";

    	return getTellSpeechletResponse(speechText);	
    	
    }

    /**
     * Returns an ask Speechlet response for a speech and reprompt text.
     *
     * @param speechText
     *            Text for speech output
     * @param repromptText
     *            Text for reprompt output
     * @return ask Speechlet response for a speech and reprompt text
     */
    public SpeechletResponse getAskSpeechletResponse(String speechText, String repromptText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
        
    }

   /**
     * Returns a tell Speechlet response for a speech and reprompt text.
     *
     * @param speechText
     *            Text for speech output
     * @return a tell Speechlet response for a speech and reprompt text
     */
    private SpeechletResponse getTellSpeechletResponse(String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

  /**
     * Creates and returns response for the help intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @param skillContext
     *            {@link SkillContext} for this request
     * @return response for the help intent
     */
    public SpeechletResponse getHelpIntentResponse(Intent intent, Session session,
            SkillContext skillContext) { //CHECK IF THIS IS RIGHT?
        return skillContext.needsMoreHelp() ? getAskSpeechletResponse(help_text, help_text)
                               : getTellSpeechletResponse(help_text);
    }

}
 