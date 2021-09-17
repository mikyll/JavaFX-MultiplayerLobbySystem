package model;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class User {
	private String nickname;
	private boolean isReady;
	private InetAddress address;
	
	
	public User(String nickname)
	{
		this.nickname = nickname;
		this.isReady = false;
	}
	public User(String nickname, InetAddress address)
	{
		this.nickname = nickname;
		this.address = address;
		this.isReady = false;
	}
	public User(String nickname, String address)
	{
		this.nickname = nickname;
		try {
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			System.out.println("Invalid address");
		}
		this.isReady = false;
	}

	public String getNickname() {return nickname;}
	public void setNickname(String nickname) {this.nickname = nickname;}
	public boolean isReady() {return isReady;}
	public void setReady(boolean isReady) {this.isReady = isReady;}
	public InetAddress getAddress() {return address;}
	public void setAddress(InetAddress address) {this.address = address;}
	
}
