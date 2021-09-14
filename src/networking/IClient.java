package networking;

public interface IClient {
	public void sendChatMessage(String content);
	public void sendReady(boolean ready);
	public void sendClose();
}
