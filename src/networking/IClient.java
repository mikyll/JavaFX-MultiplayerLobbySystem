package networking;

public interface IClient {
	public void sendMessage(String content);
	public void sendReady(boolean ready);
	public void sendClose();
}
