# Draughts

<img src="https://github.com/AkashbirSingh/Draughts/blob/main/icon.png" width="100" alt="Your Image">

Draughts is a popular variation of the game of Checkers.

## Installation

Either clone the repository manually, or use your shell:

```bash
git clone https://github.com/AkashbirSingh/Draughts.git
```

## Running Draughts

Simply compile 'Driver.java' and run it.
```python
javac Driver.java
java Driver.java
```

Note that there must be two instances running to play, whether on the same machine, or not.

Ensure that both instances are running under the same network as running them on different networks has not been tested.

## How to play

Some important features to note for this version are:
* The starting player is decided at random.
* White always plays first.
* All captures are mandatory.

The general rules can be found [here.](https://draughts.org/the-rules-of-draughts)

## Networking Capabilities

In this version, there is no way to play Draughts offline. (i.e. with a computer player)

When starting the game, you will need to decide if you are the Server, or the Client:
* The Server will provide a local IP address that the client must connect to.
* The Client will ask for an IP address where an instance is running as a Server.

Once this step is done, the game window will appear for both parties, and the game setup will be displayed in the console.

Here is an example for what is displayed in the Server instance:

![image](https://github.com/user-attachments/assets/f8cf8e30-d706-4874-84d0-9db8dfea89bf)

## Other Features

The game supports:

* Forefeiting, where if any party presses the forefeit button that party will lose and the winner will be announced to both parties.

* Draw requests, where both parties must accept the draw request to end the game as a Tie.

## Screenshots

![image](https://github.com/user-attachments/assets/48c58907-616d-4539-871f-19288eafa7ee)

![image](https://github.com/user-attachments/assets/a3d8332f-02f8-4194-a7cf-dd5ffe417be2)

