# JavaFX-MultiplayerLobbySystem

JavaFX lobby system for multiplayer games with chat, using socket TCP (can be extended to UDP).

Features:
- chat;
- ready button;
- kick user button (only the server, which created the room, can do this).

Chat support using JavaFX and socket TCP (stream) to be used for Cluedo app


to add:
- automatic textarea scrolling, to last message;
- ~~update User list in server, when someone send the READY.~~
- fix exception print stack (handle them in a more proper way).
- add timer after sending READY message
- add arrow to identify which user a particular client is
- refactor: use only one listView and put an HBox inside it(?)
- fix gui components to have proper dimension
- add on close function to disconnect and stop the threads before closing the app
- add "start game" button which enables when the room has the minimum users required
- add open/close checkbox to allow users to join
- add Datagram (UDP) variant

references:
- [JavaFX-Chat](https://github.com/DomHeal/JavaFX-Chat)
