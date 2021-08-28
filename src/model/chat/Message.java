package model.chat;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private MessageType msgType;
	private String timestamp;
	private String nickname;
	private String content;
	
	public Message(String type, String timestamp, String nickname, String content)
	{
		this.msgType = MessageType.valueOf(type);
		this.timestamp = timestamp;
		this.nickname = nickname;
		this.content = content;
	}
	public Message(MessageType type, String timestamp, String nickname, String content)
	{
		this.msgType = type;
		this.timestamp = timestamp;
		this.nickname = nickname;
		this.content = content;
	}
	
	public MessageType getMsgType() {return msgType;}
	public void setMsgType(MessageType msgType) {this.msgType = msgType;}
	public String getTimestamp() {return timestamp;}
	public void setTimestamp(String timestamp) {this.timestamp = timestamp;}
	public String getNickname() {return nickname;}
	public void setNickname(String nickname) {this.nickname = nickname;}
	public String getContent() {return content;}
	public void setContent(String content) {this.content = content;}
	
	public static void printMessage(Message msg)
	{
		System.out.println(msg.getTimestamp() + " " + msg.getNickname() + "(" + msg.getMsgType().toString() + "): " + msg.getContent());
	}
}
