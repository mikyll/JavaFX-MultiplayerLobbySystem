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

Features:
- chat;
- ready button;
- kick user button (only the server, which created the room, can do this).

Chat support using JavaFX and socket TCP (stream) to be used for Cluedo app

### Demo

<table style="border: none">
  <tr>
    <td width="49.9%"><img src=".gif" alt="Server"/></td>
    <td width="49.9%"><img src=".gif" alt="Client"/></td>
  </tr>
  <tr>
    <td>Create New Room</td>
    <td>Join Existing Room</td>
  </tr>
</table>

<a href="#contribuire">contribuire</a> 


### Execution


### Features
table "feature-description-gif"
schemes taken from the docs

<table>
	<tr>
		<td><b>Feature</b></td>
		<td><b>Description</b></td>
		<td width="40%"><b>Demo</b></td>
	</tr>
	<tr>
		<td>Listen server</td>
    		<td>The network topology that has been used is the <i>Listen server</i>: <b>a client can become the server, and will host the game for other clients</b>. Basically, when a client creates a new room it becomes a server, hosting the connection for other clients that will send a request and join the room. This solution could be a problem in case of a game that requires low latency (e.g. FPS), since the client that hosts the match doesn't experience any, but for a boardgame it's perfect, considering it can be built on TCP and the latency isn't a issue at all.</td>
		<td width="40%"><img src="https://github.com/mikyll/JavaFX-MultiplayerLobbySystem/blob/main/gfx/client-hosted%20(listen%20server).png" alt="Listen server scheme"/></td>
	</tr>
	<tr>
		<td>Nickname & IP address validation</td>
		<td>The nickname and address validations happen when the user types a key the textfields as well as when he confirms</td>
		<td width="40%"><img src="" alt="."/></td>
	</tr>
	<tr>
		<td>Server: set min and max room size</td>
		<td>Before creating the room, the server can choose its size: how many users it can contain and how many are required to start the game</td>
		<td width="40%"><img src="" alt="."/></td>
	</tr>
	<tr>
		<td>Server: open/close the room</td>
		<td></td>
		<td width="40%"><img src="" alt="."/></td>
	</tr>
	<tr>
		<td>Server: kick a user</td>
		<td></td>
		<td width="40%"><img src="" alt="."/></td>
	</tr>
	<tr>
		<td>Client: set ready/not ready</td>
		<td></td>
		<td width="40%"><img src="" alt="."/></td>
	</tr>
	<tr>
		<td>Chat</td>
		<td></td>
		<td width="40%"><img src="" alt="."/></td>
	</tr>
</table>

### Documentation


### Roadmap
to add:
- ~~update User list in server, when someone send the READY.~~
- ~~add arrow to identify which user a particular client is~~
- ~~refactor: use only one listView and put an HBox inside it(?)~~
- ~~add on close function to disconnect and stop the threads before closing the app~~
- ~~add "start game" button which enables when the room has the minimum users required~~
- ~~add open/close checkbox to allow users to join~~
- ~~change the "this.client != null" check with NavState.MP_CLIENT (same for the server)~~
- ~~fix Join Existing Room validation~~
- ~~move Controller methods (in a proper order)~~
- ~~fix KickUser (removes the server too). The problem was that goBack(), instead of switchToMP, closed the connection, so client sent KICK and DISCONNECT before closing the socket~~
- catch Connection Reset & SocketException: Interrupted function call: accept failed, thrown when we back from the server room, when no one has been accepted yet
- automatic textarea scrolling, to last message;
- fix exception print stack (handle them in a more proper way).
- add timer after sending READY message
- fix gui components to have proper dimension
- add Datagram (UDP) variant
- add HTTP/websocket variant(?)
- create a logger (?) and log messages to file too
- create a class for my Spinner (MikyllSpinner ?)
- add an headless server to handle the room list (when a server creates a room it's inserted into the list, and the client can access that list to see those rooms)
- add a ban list (nickname/IP)
- check if the users get disconnected properly when closing the app by the Launcher

### Built With
Per l'implementazione ho utilizzato Java 11 e JavaFX 11, come IDE Eclipse (versione 2020-03 (4.15.0)), e SceneBuilder per la creazione della grafica (file FXML). Vedere i passi seguiti per il [setup del progetto](https://github.com/mikyll/ROQuiz/blob/main/Project%20Setup.md).

versione Java: JavaSE-11 (jdk-11.0.11)<br/>
versione JavaFX: JavaFX 11 (javafx-sdk-11.0.2)

### References
* [JavaFX-Chat](https://github.com/DomHeal/JavaFX-Chat)



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
