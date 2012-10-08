import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class CLM_ui {
	private Scanner scanner = new Scanner( System.in );
	private smack_connection connection;
	public static void main(String[] args){
		CLM_ui ui = new CLM_ui();
		ui.start();
	}
	public void start(){
		
		this.connection = new smack_connection();
		
		if(this.try_connection()){
			boolean flag = true;
			while(flag){
				System.out.print("Please insert \"admin\" , \"guest\" or \"exit\" : ");
				String str = this.scanner.next();
				if(str.equals("admin")||str.equals("guest")){
					while(!login(str));
					this.connection.init_after_connection();
			    	boolean console_result = console();
			    	this.connection.disconnect();
			    	if(console_result)System.out.println("logout!");
			    	else flag = false;
				}
				else if(str.equals("exit"))flag = false; 
			}
		}
		System.out.println("Exit!");
	}
	public boolean try_connection(){
		int i = 5;
		boolean flag = false;
		while(!flag && i > 0){
			flag = this.connection.connect();
			if(flag)return true;
			System.out.println("Connection failed!");
			System.out.println("Reconnecting...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i--;
		}
		return false;
	}
	public void connection_failed(){
		System.out.println("Connection failed!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reconnecting...");
	}
    public boolean console(){	// true for exit
		String str = "default";
		System.out.println("\"/help\" for help.");
		while (str != null){
			//System.out.println("Talk to " + this.connection.get_target() + ":");
			str = this.scanner.next();
			if(str.equals("/help")){
				if(this.connection.is_admin()){
					System.out.println("/list");
					System.out.println("/target");
				}
				System.out.println("/logout");
				System.out.println("/exit");
			}
			else if(str.equals("/list")){
				if(this.connection.is_admin()){
					list();
				}
			}
			else if(str.equals("/target")){
				if(this.connection.is_admin()){
					target();
				}
			}
			else if(str.equals("/logout"))return true;
			else if(str.equals("/exit")) return false;
			else this.connection.send_message(str);
		}
		return false;
	}
	public boolean login(String str){
		this.connection = new smack_connection();
		this.connection.connect();
		String username;
		String password;
		String resource;
		username = str;
		System.out.print("Password : ");
		password = this.scanner.next();
		System.out.print("Resource : ");
		resource = this.scanner.next();
		boolean is_login=this.connection.login(username,password,resource);
		if(is_login)System.out.println("Login as : "+username);
		else System.out.println("Invalid username or password or account not enabled!");
		return is_login;
	}
	public void list(){
		List<String> list = this.connection.get_list();
		Iterator<String> iterator = list.iterator();
		
		//for(String resource : list){
		while(iterator.hasNext()){
			String str = iterator.next();
			if(str.equals(this.connection.get_target())){
				System.err.println(str);
			}
			else {
				System.out.println(str);
			}
		}
	}
	public void target(){
		boolean flag = true;
		while(flag){
			System.out.println("Please select a target :");
			list();
			String str = this.scanner.next();
			if(this.connection.get_list().contains(str)){
				this.connection.set_target(str);
				flag = false;
			}
		}
	}
}
