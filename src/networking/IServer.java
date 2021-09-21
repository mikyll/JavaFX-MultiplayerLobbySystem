package networking;

import model.User;

public interface IServer {
	public void sendChatMessage(String content);
	public void sendKickUser(String nickname);
	public User sendBanUser(String banNickname);
	public boolean removeBan(String address);
	public boolean checkCanStartGame();
	public void sendClose();
}
