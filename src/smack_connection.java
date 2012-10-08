import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class smack_connection {
	private static String admin = "admin";
	private static String guest = "guest";
	private static String domain = "vopenfire";
	private static String host = "192.168.200.19";
	
	private ConnectionConfiguration config;
	private Connection connection;
	private String username;
	private String password;
	private String resource;
	private String oppo;
	private String target = null;
	private List<String> target_list = new ArrayList<String>();
	private Chat chat;
	private RosterListener roster_listener;
	private MessageListener myMessageListener;
	public smack_connection(){
	   	// Create the configuration for this new connection
		config = new ConnectionConfiguration( smack_connection.host , 5222 );
	   	config.setCompressionEnabled(true);
	   	config.setSASLAuthenticationEnabled(true);
	   	this.connection = new XMPPConnection(config);
	   	//XMPPConnection.DEBUG_ENABLED = true;
	}
	public boolean connect(){
	   	while(true){
		   	try {
				this.connection.connect();
				return true;
			} catch (XMPPException e1) {
				return false;
			}
	   	} 
	}
	public void init_after_connection(){
    	build_roster_listener();
    	
		// create chat
		this.myMessageListener = new MessageListener() {
    	    public void processMessage(Chat chat, Message message) {
				if (message.getType() == Message.Type.chat) {
					String from = message.getFrom();
					String resource = from.substring(from.lastIndexOf("/") + 1);
					add_target(resource);
					
					if( message.getBody() != null ){
						System.err.print(message.getBody());
						System.out.println(" (" + message.getFrom() + ")");
					}
				}
    	    }
    	};
    	this.chat = this.connection.getChatManager().createChat(this.oppo, this.myMessageListener);
	}
	public boolean login(String username, String password){
		this.username = username;
		this.password = password;
    	// Log into the server
    	try {
			this.connection.login(
				this.username ,
				this.password ,
				this.resource
			);
		} catch (XMPPException e1) {
	    	return false;
		}
    	if(this.username.equals(smack_connection.admin)){
    		this.oppo = smack_connection.guest + "@" + smack_connection.domain;
    	}
    	else{
    		this.oppo = smack_connection.admin + "@" + smack_connection.domain;
    	}
    	return true;
	}
	public boolean is_admin(){
		if(this.username.equals(smack_connection.admin))return true;
		else return false;
	}
	public String get_target(){
		return this.target;
	}
	public void build_roster_listener(){
			
		this.roster_listener = new RosterListener() {
			// Ignored events public void entriesAdded(Collection<String> addresses) {}
		    public void entriesDeleted(Collection<String> addresses) {
		    }
		    public void entriesUpdated(Collection<String> addresses) {
		    }
		    public void presenceChanged(Presence presence) {
		    	String from = presence.getFrom();
		    	String resource = from.substring(from.lastIndexOf("/") + 1);
		    	if(presence.isAvailable()){
		    		add_target(resource);
		    	}
		    	else{
		    		remove_target(resource);
		    	}
		    }
			@Override
			public void entriesAdded(Collection<String> arg0) {
			}
		};
		this.connection.getRoster().addRosterListener(roster_listener);
	}
	boolean add_target(String resource){
    	if(!this.target_list.contains(resource)){
    		this.target_list.add(resource);
    		return true;
    	}
    	else return false;
	}
	boolean remove_target(String resource){
		if(this.target_list.contains(resource)){
			this.target_list.remove(resource);
			if(this.target.equals(resource)){
				this.target = null;
			}
			return true;
		}else {
			return false;
		}
	}
	void set_target(String resource){
    	this.target = resource;
	}
	public List<String> get_list(){
		return this.target_list;
	}
	public void send_message(String str){
		if( this.target != null || !is_admin()){
			Message newMessage = new Message();
			newMessage.setBody(str);
			newMessage.setProperty("favoriteColor", "red");
			Chat this_chat;
			if(is_admin()){
				this_chat = this.connection.getChatManager().createChat( this.oppo + "/" + this.target , this.myMessageListener );
			}
			else{
				this_chat = this.chat;
			}
			try {
				this_chat.sendMessage(newMessage);
			} catch (XMPPException e) {
				System.out.println("Send message failed!");
			}
		}
	}
	public void disconnect(){
		this.myMessageListener = null;
		this.connection.getRoster().removeRosterListener(this.roster_listener);
		this.connection.disconnect();
	}
}
