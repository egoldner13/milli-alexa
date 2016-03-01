package milli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;

import milli.MilliManager;

public class MilliSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(MilliSpeechlet.class);

    private MilliManager milliManager;
    private SkillContext skillContext;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        initializeComponents();

        // if user said a one shot command that triggered an intent event,
        // it will start a new session, and then we should avoid speaking too many words.
        skillContext.setNeedsMoreHelp(false);
    }
 

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        skillContext.setNeedsMoreHelp(true);

        return getWelcomeResponse();
    }

    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
    	String speech_output = "Welcome to Milli Squad. My name is Milli, how can I help you??";
            
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        String reprompt_text = milliManager.help_text;

        return milliManager.getAskSpeechletResponse(speech_output, reprompt_text);
    }


	@Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        initializeComponents();

        Intent intent = request.getIntent();
        if ("GetStatusOfAppliance".equals(intent.getName())) {
            return milliManager.handleGetStatusRequest(intent, session);

        } else if ("ChangeStatusOfAppliance".equals(intent.getName())) {
            return milliManager.handleChangeStatusRequest(session);
            
        } else if ("GetTemperatureIntent".equals(intent.getName())) {
            return milliManager.handleGetTempRequest(intent, session);

        } else if ("GetGasLevelIntent".equals(intent.getName())) {
            return milliManager.handleGetGasLevelRequest(intent, session);
            
        } else if ("GetStatusOfCar".equals(intent.getName())) {
            return milliManager.handleStatusOfCarRequest(intent, session);

        } else if ("AMAZON.HelpIntent".equals(intent.getName())) {
            return milliManager.getHelpIntentResponse(intent, session, skillContext);

        } else if ("AMAZON.StopIntent".equals(intent.getName())) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");
            return SpeechletResponse.newTellResponse(outputSpeech);
            
        } else if ("AMAZON.CancelIntent".equals(intent.getName())) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");
            return SpeechletResponse.newTellResponse(outputSpeech);

        } else {
            throw new IllegalArgumentException("Unrecognized intent: " + intent.getName());
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Initializes the instance components if needed.
     */
    private void initializeComponents() {
            milliManager = new MilliManager();
            skillContext = new SkillContext();
    }
}


