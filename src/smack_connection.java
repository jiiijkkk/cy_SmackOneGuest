import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class smack_connection {
	private String admin = "admin";
	private String guest = "guest";
	private ConnectionConfiguration config;
	private Connection connection;
	private String resource;
	private String domain = "v-virtualbox";
	private String username;
	private String password;
	private String oppo;
	private String target = null;
	private List<String> target_list = new ArrayList<String>();
	private Chat chat;
	private ChatManager chatmanager;
	private RosterListener roster_listener;
	private MessageListener myMessageListener;
	public smack_connection(String resource , CLM_ui ui){
	   	// Create the configuration for this new connection
		this.resource=resource;
		config = new ConnectionConfiguration(this.resource, 5222);
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
//	   	build_connection_listener();
    	this.chatmanager = this.connection.getChatManager();
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
    	this.chat = chatmanager.createChat(this.oppo, myMessageListener);
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
			// Auto-generated catch block
			//e1.printStackTrace();
	    	return false;
		}
    	if(this.username.equals(this.admin)){
    		this.oppo = this.guest + "@" + this.domain;
    	}
    	else{
    		this.oppo = this.admin + "@" + this.domain;
    	}
    	return true;
	}
	public boolean is_admin(){
		if(this.username.equals(this.admin))return true;
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
				// Auto-generated catch blockl
				e.printStackTrace();
			}
		}
	}
	public void disconnect(){
		this.myMessageListener = null;
		this.connection.getRoster().removeRosterListener(this.roster_listener);
		this.connection.disconnect();
	}
}
