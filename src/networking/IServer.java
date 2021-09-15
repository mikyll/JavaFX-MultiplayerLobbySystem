package networking;

public interface IServer {
	public void sendChatMessage(String content);
	public void sendKickUser(String nickname);
	public void sendClose();
	public boolean checkCanStartGame();
}
