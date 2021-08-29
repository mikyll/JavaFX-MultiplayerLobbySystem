package networking;

import java.io.IOException;

public interface IClient {
	public void sendMessage(String content);
	public void sendReady(boolean ready);
}
