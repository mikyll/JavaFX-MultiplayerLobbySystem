package networking;

public interface IServer {
	public void sendMessage(String content);
	public void kickUser(String nickname);
	public void sendClose();
}
