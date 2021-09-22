package networking;

import model.User;

public interface IServer {
	public void sendChatMessage(String content);
	public void sendKickUser(String kickNickname);
	public User sendBanUser(String banNickname);
	public boolean sendBanUser(String banNickname, String banAddress);
	public boolean removeBan(String address);
	public boolean checkCanStartGame();
	public void sendClose();
}
