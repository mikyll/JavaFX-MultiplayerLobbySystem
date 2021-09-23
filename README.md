[![Downloads][downloads-shield]][downloads-url]
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]
[![GitHub followers][github-shield]][github-url]

<h1 align="center">JavaFX-MultiplayerLobbySystem</h1>
JavaFX lobby system for multiplayer games with chat, ready toggle and kick buttons, using socket TCP by default.

### Demo
<table style="border: none">
  <tr>
    <td width="49.9%"><img src="example-server.gif" alt="Server"/></td>
    <td width="49.9%"><img src="example-client.gif" alt="Client"/></td>
  </tr>
  <tr>
    <td>Create New Room (Server)</td>
    <td>Join Existing Room (Client)</td>
  </tr>
</table>


### Execution
1. Download the [latest release](https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/releases/latest).
2. Extract the content of the archive.
3. Execute the Launcher.

### Features
<table>
	<tr>
		<td><b>Feature</b></td>
		<td><b>Description</b></td>
		<td width="40%"><b>Demo</b></td>
	</tr>
	<tr>
		<td><b>Listen server<b/></td>
    		<td>The network topology that has been used is the <i>Listen server</i>: <b>a client can become the server, and will host the game for other clients</b>. Basically, when a client creates a new room it becomes a server, hosting the connection for other clients that will send a request and join the room. This solution could be a problem in case of a game that requires low latency (e.g. FPS), since the client that hosts the match doesn't experience any, but for a boardgame it's perfect, considering it can be built on TCP and the latency isn't a issue at all.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/listen-server-with-legend-whitebg.png" alt="Listen server scheme"/></td>
	</tr>
	<tr>
		<td><b>Nickname & IP address validation</b></td>
		<td>The nickname and address validations happen when the user types a key in the textfields as well as when he confirms (that's because the user could cut the text from the field, without typing any key, and the button would remain enabled).</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-validation.gif" alt="Validation example"/></td>
	</tr>
	<tr>
		<td><b>Server: set min and max room size</b></td>
		<td>Before creating the room, the server can choose its size: how many users it can contain and how many are required to start the game.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-room-size.gif" alt="Room size example"/></td>
	</tr>
	<tr>
		<td><b>Server: open/close the room</b></td>
		<td>The server can decide whether to close or open the room at will, preventing new users from joining or not.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-open-close-room.gif" alt="Open/close room example"/></td>
	</tr>
	<tr>
		<td><b>Client: set ready/not ready</b></td>
		<td>The clients can set their state to ready or not ready. If everyone is ready, and the minimum user required is reached, the game can start.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-ready.gif" alt="Ready/Not ready example"/></td>
	</tr>
	<tr>
		<td><b>Server: kick a user</b></td>
		<td>The server can kick any user out of the room. This is useful when the minimum users required to start the game is reached, but someone won't set their state to ready.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-kick.gif" alt="Kick user example"/></td>
	</tr>
	<tr>
		<td><b>Server: ban a user</b></td>
		<td>The server can ban any user out of the room.</td>
		<td width="40%"><img src="" alt="Ban user example"/></td>
	</tr>
	<tr>
		<td><b>Chat</b></td>
		<td>The chat allows users to communicate in real time, showing the timestamp of the messages and the user that sent it. The chat also shows when users connect or disconnect and when they get kicked out from the server.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-chat.gif" alt="Chat example"/></td>
	</tr>
	<tr>
		<td><b>Disconnection</b></td>
		<td>When the server leave the room or close the application, each connected user get disconnected.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/example-disconnection.gif" alt="Disconnection example"/></td>
	</tr>
</table>

### Documentation
Read the [full docs](https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/tree/main/docs).

+ main schemes

### Roadmap
to add:
- ~~update User list in server, when someone send the READY.~~
- ~~add arrow to identify which user a particular client is~~
- ~~refactor: use only one listView and put an HBox inside it(?)~~
- ~~add on close function to disconnect and stop the threads before closing the app~~
- ~~add "start game" button which enables when the room has the minimum users required~~
- ~~add open/close checkbox to allow users to join~~
- ~~check if the users get disconnected properly when closing the app by the Launcher~~
- ~~change the "this.client != null" check with NavState.MP_CLIENT (same for the server)~~
- ~~fix Join Existing Room validation~~
- ~~move Controller methods (in a proper order)~~
- ~~fix KickUser (removes the server too). The problem was that goBack(), instead of switchToMP, closed the connection, so client sent KICK and DISCONNECT before closing the socket~~
- ~~fix exception print stack (handle them in a more proper way)~~
- ~~catch Connection Reset~~
- ~~SocketException: Interrupted function call: accept failed, thrown when we back from the server room, when no one has been accepted yet~~
- ~~add a ban list (nickname/IP)~~
- ~~automatic textarea scrolling, to last message~~
- add banned user throws null pointer exception on Linux, fix it
- show private and public IP address and be able to copy it
- chat text message check length before sending (not more than 200 char?)
- add copy in clipboard when clicking on a username (inside a room)
- server can enable/disable the chat(?)
- add timer after sending READY message
- fix gui components to have proper dimension
- add Datagram (UDP) variant
- add HTTP/websocket variant(?)
- create a logger (?) and log messages to file too
- create a class for my Spinner (MikyllSpinner ?)
- add an headless server to handle the room list (when a server creates a room it's inserted into the list, and the client can access that list to see those rooms)
- use this template for CluedoApp

### Built With
For the implementation I used Java 11 and JavaFX 11, Eclipse IDE (2020-03 (4.15.0)), and SceneBuilder to create the gui (FXML).

Java version: JavaSE-11 (jdk-11.0.11)<br/>
JavaFX version: JavaFX 11 (javafx-sdk-11.0.2)

### References
* Idea from: [JavaFX-Chat](https://github.com/DomHeal/JavaFX-Chat) by DomHeal
* Icons: [Icons8](https://icons8.com/)
* [Getting the IP address](https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java)


[downloads-shield]: https://img.shields.io/github/downloads/mikyll/JavaFX-MultiplayerLobbySystem/total
[downloads-url]: https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/releases/latest
[contributors-shield]: https://img.shields.io/github/contributors/mikyll/JavaFX-MultiplayerLobbySystem
[contributors-url]: https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/mikyll/JavaFX-MultiplayerLobbySystem
[forks-url]: https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/network/members
[stars-shield]: https://img.shields.io/github/stars/mikyll/JavaFX-MultiplayerLobbySystem
[stars-url]: https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/stargazers
[issues-shield]: https://img.shields.io/github/issues/mikyll/JavaFX-MultiplayerLobbySystem
[issues-url]: https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/issues
[license-shield]: https://img.shields.io/github/license/mikyll/JavaFX-MultiplayerLobbySystem
[license-url]: https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/master/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?logo=linkedin&colorB=0077B5
[linkedin-url]: https://www.linkedin.com/in/michele-righi/?locale=it_IT
[github-shield]: https://img.shields.io/github/followers/mikyll.svg?style=social&label=Follow
[github-url]: https://github.com/mikyll
