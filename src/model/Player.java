package model;

public class Player extends User{
	private int turn;
	
	public Player(String nickname, int id, int turn)
	{
		super(nickname, id);
		this.turn = turn;
	}
	public Player(String nickname, int turn)
	{
		super(nickname);
		this.turn = turn;
	}
}
