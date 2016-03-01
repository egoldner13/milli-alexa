package milli;

public class MilliDevice {

	public String mac_addr;
	public String name;
	public String type;
	public String nextAction;
	public String nextText;
	
	public MilliDevice(String name, String mac_addr, String type){
		this.name = name;
		this.mac_addr = mac_addr;
		this.type = type;
		this.nextAction = null;
		this.nextText = null;
	}
	public void setNextAction(String nextAction){
		this.nextAction = nextAction;
	}
	
	public void setNextText(String nextText){
		this.nextText = nextText;
	}
	
	public String getMac_addr()  { return mac_addr; }
	public String getName() { return name; } 
	public String getType() { return type; }
	public String getNextAction() { return nextAction; }
	public String getNextText() { return nextText; }
	
}
