package model;

public class User {
	private String nickname;
	private boolean isReady;
	private int id;
	
	
	public User(String nickname)
	{
		this.nickname = nickname;
		this.isReady = false;
	}
	public User(String nickname, int id)
	{
		this.nickname = nickname;
		this.isReady = false;
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
