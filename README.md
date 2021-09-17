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


### Documentation


### Roadmap
Features da aggiungere e sviluppi futuri:
* deploy su MacOS
* aggiungere file di configurazione (xml o JSON) per rendere le modifiche alle impostazioni persistenti
* aggiungere test domande duplicate
* creare tool per inserire domande nuove (che sfrutta il test per le domande duplicate)
* fare il porting su mobile (magari Android)

### Built With
Per l'implementazione ho utilizzato Java 11 e JavaFX 11, come IDE Eclipse (versione 2020-03 (4.15.0)), e SceneBuilder per la creazione della grafica (file FXML). Vedere i passi seguiti per il [setup del progetto](https://github.com/mikyll/ROQuiz/blob/main/Project%20Setup.md).

versione Java: JavaSE-11 (jdk-11.0.11)<br/>
versione JavaFX: JavaFX 11 (javafx-sdk-11.0.2)

### References
* Guida a classe Timeline usata per realizzare il countdown: [Timers in JavaFX and ReactFX](https://tomasmikula.github.io/blog/2014/06/04/timers-in-javafx-and-reactfx.html)
* Lavorare coi moduli Java: [Java 9 Modules in Eclipse](https://blogs.oracle.com/java/post/how-to-develop-modules-with-eclipse-ide)
* Creare jre custom con JavaFX (jlink): [Custom jre with JavaFX 11](https://stackoverflow.com/questions/52966195/custom-jre-with-javafx-11) e [How to use jlink to create a Java image with javafx modules](https://github.com/javafxports/openjdk-jfx/issues/238)
* JavaFX ottenere HostService senza riferimento alla classe Application (Main extends Application): [Open a link in a browser without reference to Application](https://stackoverflow.com/questions/33094981/javafx-8-open-a-link-in-a-browser-without-reference-to-application)


[downloads-shield]: https://img.shields.io/github/downloads/mikyll/ROQuiz/total
[downloads-url]: https://github.com/mikyll/ROQuiz/releases/latest
[contributors-shield]: https://img.shields.io/github/contributors/mikyll/ROQuiz
[contributors-url]: https://github.com/mikyll/ROQuiz/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/mikyll/ROQuiz
[forks-url]: https://github.com/mikyll/ROQuiz/network/members
[stars-shield]: https://img.shields.io/github/stars/mikyll/ROQuiz
[stars-url]: https://github.com/mikyll/ROQuiz/stargazers
[issues-shield]: https://img.shields.io/github/issues/mikyll/ROQuiz
[issues-url]: https://github.com/mikyll/ROQuiz/issues
[license-shield]: https://img.shields.io/github/license/mikyll/ROQuiz
[license-url]: https://github.com/mikyll/ROQuiz/blob/master/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?logo=linkedin&colorB=0077B5
[linkedin-url]: https://www.linkedin.com/in/michele-righi/?locale=it_IT
[github-shield]: https://img.shields.io/github/followers/mikyll.svg?style=social&label=Follow
[github-url]: https://github.com/mikyll


# JavaFX-MultiplayerLobbySystem

JavaFX lobby system for multiplayer games with chat, using socket TCP (can be extended to UDP).

Features:
- chat;
- ready button;
- kick user button (only the server, which created the room, can do this).

Chat support using JavaFX and socket TCP (stream) to be used for Cluedo app


### Demo
showcase video with a server and multiple clients;

table with feature, description and gif

roadmap


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

references:
- [JavaFX-Chat](https://github.com/DomHeal/JavaFX-Chat)
