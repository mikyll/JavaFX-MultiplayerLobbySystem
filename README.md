# ChatClientServer

JavaFX lobby system for a game using socket TCP (can be extended to UDP).

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

references:
- [JavaFX-Chat](https://github.com/DomHeal/JavaFX-Chat)
