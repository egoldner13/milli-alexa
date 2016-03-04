package milli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import milli.SkillContext;
import milli.MilliDevice;
import milli.ConnectSSH;

public class MilliManager {
   
	private int STATUS_LIGHT = -1;
	
	public final String[] response_text = new String[]{"You should try asking Siri to do that!", "All done! Its ok if you are impressed!",
				"Tadaaa!", "I am done with your request, impressive, huh?", "All done, no need to thank me", "Task complete, you humans are just so lazy these days"};
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
     
    
    public int getRandomNumber(){
    	Random r = new Random();
    	int low = 0; //inclusive
    	int high = 7; //exclusive
    	return r.nextInt(high-low) + low;
    }
    
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
    public SpeechletResponse handleGetStatusDoorRequest(Session session) {	
    	String speech_output = "";
    	
    	ConnectSSH connect = new ConnectSSH();
        List<String> output = connect.executeFile("/home/ciq/getStatusOfDoor.sh");
        String outputStr = null;
        for (String line : output){
        	outputStr = outputStr + line;
		}
        
        int status = outputStr.contains("1") ? 1 : -1;
        status = outputStr.contains("0") ? 0 : status;

        if (status == 1){
        	speech_output = "the garage door is open, would you like me to close it for you?";
     
        } else if (status == 0 ){
        	speech_output = "the garage door is closed, would you like me to open it for you?";
        	
        } else {
        	speech_output = speech_output + " I'm sorry, there was a problem issuing the net manager command for the garage door";
            return getTellSpeechletResponse(speech_output);
        }

        return getAskSpeechletResponse(speech_output, help_text);
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
    public SpeechletResponse handleChangeStatusDoorRequest(Session session) {
    	String speechText = response_text[getRandomNumber()]; 

    	ConnectSSH connect = new ConnectSSH();
    	List<String> output = connect.executeFile("/home/ciq/toggleDoor.sh");	
    	
    	return getTellSpeechletResponse(speechText);	

     }
   
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

	public SpeechletResponse handleGetStatusLightRequest(Session session) {	
		String speech_output = "";
		
		ConnectSSH connect = new ConnectSSH();
	    List<String> output = connect.executeFile("/home/ciq/getStatusOfLight.sh");
	    String outputStr = null;
	    for (String line : output){
	    	outputStr = outputStr + line;
		}
	    
	    int status = outputStr.contains("1") ? 1 : -1;
	    status = outputStr.contains("0") ? 0 : status;
	
	    if (status == 1){
	    	speech_output = "the light is off, would you like me to turn it on for you?";
	    	STATUS_LIGHT = 1;
	  	    	
	    } else if (status == 0 ){
	    	speech_output = "the light is on, would you like me to turn if off for you?";
	    	STATUS_LIGHT = 0;

	    } else {
	    	speech_output = speech_output + " I'm sorry, there was a problem issuing the net manager command for the light";
	        return getTellSpeechletResponse(speech_output);
	    }

	    return getAskSpeechletResponse(speech_output, help_text);
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
    public SpeechletResponse handleChangeStatusLightRequest(Session session) {
    	String speechText = response_text[getRandomNumber()]; 

    	ConnectSSH connect = new ConnectSSH();
    	List<String> output = null;
    	if (STATUS_LIGHT == 1){
    		output = connect.executeFile("/home/ciq/turnOnLight.sh");
    	} else if (STATUS_LIGHT == 0){
   			output = connect.executeFile("/home/ciq/turnOffLight.sh");
   		} else {
   			speechText = "Im sorry";
   		}
    	
    	return getTellSpeechletResponse(speechText);	

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
    public SpeechletResponse handleChangeStatusLightRequest(Intent intent, Session session) {
    	String speechText = response_text[getRandomNumber()]; 
    	STATUS_LIGHT = intent.getSlot("status").getValue().equals("off") ? 1 : 0;

    	ConnectSSH connect = new ConnectSSH();
    	List<String> output;
    	if (STATUS_LIGHT == 1){
    		output = connect.executeFile("/home/ciq/turnOnLight.sh");
    	} else if (STATUS_LIGHT == 0){
   			output = connect.executeFile("/home/ciq/turnOffLight.sh");
   		} else {
   			speechText = "Im sorry";
   		}
    	
    	return getTellSpeechletResponse(speechText);	

     }
    
    public SpeechletResponse handleGetTempRequest(Intent intent, Session session) {
    	String speechText = "the temperature is ";

    	ConnectSSH connect = new ConnectSSH();
    	List<String> output = connect.executeFile("/home/ciq/getTemp.sh");
        String outputStr = null;
        for (String line : output){
        	outputStr = outputStr + line;
		}
    	
		if (outputStr.equals(null)) return getTellSpeechletResponse("Error: Unable in net manager command response");

		int index = outputStr.indexOf("=");
		String tempStr = outputStr.substring(index+2, outputStr.length()-3);
		double temp = Math.round((Double.parseDouble(tempStr) - 1035) / -5.5 );
		speechText = speechText + String.valueOf(temp) + " degrees Celcius";

    	return getTellSpeechletResponse(speechText);	

    }


    public SpeechletResponse handleGetGasLevelRequest(Intent intent, Session session) {
    	String speechText = "the gas level is at";

    	ConnectSSH connect = new ConnectSSH();
    	List<String> output = connect.executeFile("/home/ciq/getGas.sh");
        String outputStr = null;
        for (String line : output){
        	outputStr = outputStr + line;
		}
    	
		if (outputStr.equals(null)) return getTellSpeechletResponse("Error: Unable in net manager command response");
		
		int index = outputStr.indexOf("=");
		String gasStr = outputStr.substring(index+2, outputStr.length()-3);
		speechText = speechText + gasStr + " milli volts";

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
 
