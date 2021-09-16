# JavaFX-MultiplayerLobbySystem

JavaFX lobby system for multiplayer games with chat, using socket TCP (can be extended to UDP).

Features:
- chat;
- ready button;
- kick user button (only the server, which created the room, can do this).

Chat support using JavaFX and socket TCP (stream) to be used for Cluedo app


to add:
- ~~update User list in server, when someone send the READY.~~
- ~~add arrow to identify which user a particular client is~~
- ~~refactor: use only one listView and put an HBox inside it(?)~~
- ~~add on close function to disconnect and stop the threads before closing the app~~
- ~~add "start game" button which enables when the room has the minimum users required~~
- ~~add open/close checkbox to allow users to join~~
- ~~change the "this.client != null" check with NavState.MP_CLIENT (same for the server)~~
- fix Join Existing Room validation
- move Controller methods (in a proper order)
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

references:
- [JavaFX-Chat](https://github.com/DomHeal/JavaFX-Chat)
