package networking;

import model.User;

public interface IServer {
	public void sendChatMessage(String content);
	public void sendKickUser(String nickname);
	public void sendClose();
	public boolean checkCanStartGame();
	public User sendBanUser(String banNickname);
	public void removeBan(String address);
}
