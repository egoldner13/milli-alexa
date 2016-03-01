package milli;

import com.amazon.speech.speechlet.servlet.SpeechletServlet;

public class MilliServlet extends SpeechletServlet {
	 
	  public MilliServlet() {
	    this.setSpeechlet(new MilliSpeechlet());
	  }
}


