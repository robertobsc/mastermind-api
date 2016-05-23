# mastermind-api
Mastermind api for Axiomz challenge

Based on Axiomz Challenge: http://careers.axiomzen.co/challenge

## How it was built

This project was built with spring and spring-boot.
I used a @RestController at SinglePlayer api.

At MultiPlayer api I used:
 @Controller
 
 ResponseBodyEmitter to Streaming data between players
 
 ResponseEntity to encapsulate the ResponseBodyEmitter with headers 
 so the browser can understand what is going on.

## How to Run

This may be easily run in Eclipse Luna or Mard. Just import the project as a maven project. After the import process go to the package com.axiomzen.mastermind.config, right-click at it, and choose "Run as" -> Java Application.

# APIs

There are singleplayer and multiplayer apis with this system. If you start the application as above just type in your browser:
http://localhost:8080/mastermind-api/  and complete with the specs below.

## /singleplayer
The singleplayer api is quite simple. There are two posts methods. You can submit them with a client rest application in chrome or by curl command line in linux.

/singleplayer/new_game

Input:
{ "user": "Roberto Costa" }

Expected result:
{
  "users": [
    "Roberto Costa"
  ],
  "colors": [
    "R",
    "B",
    "G",
    "Y",
    "O",
    "P",
    "C",
    "M"
  ],
  "code_length": 8,
  "game_key": "pc73mccpf3033gpnuasv",
  "solved": false
}


/singleplayer/guess
Input: 
{ 
    "code": "YCPOMRGB", 
    "game_key": "pc73mccpf3033gpnuasv" 
}
Excpected Result in case your guess is not correct:
``
{
  "users": [
    "Roberto Costa"
  ],
  "colors": [
    "R",
    "B",
    "G",
    "Y",
    "O",
    "P",
    "C",
    "M"
  ],
  "code_length": 8,
  "game_key": "pc73mccpf3033gpnuasv",
  "solved": false,
  "result": [
    {
      "guess": "RPYGOGOP",
      "near": 8
    }
  ]
}
``

Excpected Result in case your guess IS correct (you won!):
{
  "users": [
    "Roberto Costa"
  ],
  "colors": [
    "R",
    "B",
    "G",
    "Y",
    "O",
    "P",
    "C",
    "M"
  ],
  "code_length": 8,
  "game_key": "pc73mccpf3033gpnuasv",
  "num_guesses": 6,
  "past_results": [
    {
      "guess": "RPYGOGOP",
      "near": 8
    },
    {
      "guess": "RPYGOGOP",
      "near": 8
    },
    {
      "guess": "RPYGOGOP",
      "near": 8
    },
    {
      "guess": "RPYGOGOP",
      "near": 8
    },
    {
      "exactly": 8,
      "guess": "YCPOMRGB"
    }
  ],
  "solved": true,
  "result": [
    {
      "exactly": 8,
      "guess": "YCPOMRGB"
    }
  ],
  "time_taken": 348.291,
  "winner": "Roberto Costa"
}

## /multiplayer
The multiplayer api is more complex. 
The client rest applications (like the chrome extensions) don't work with the technology that I am using (i am using spring streaming to keep notifying users). 

I designed this api to support N users, but I only tested with two.

I created some really simple pages to make possible to test the apis, since just the browser itself can streaming data.

Here's the apis:

**/multiplayer/new_game**  (Alternatively invoked from a simple html page, that can be acessed typing /multiplayer/new_page at the browser)

{ "user": "Roberto Costa" ,  "numPlayers" : "2"}

Expected Result:

	You will receive **N+1** messages depending of the number of users that was specified at the input:
	1 - A confirmation message with your userKey 
	{"message":"Your user key is p8i7eb8s8vtkar64ahkb. It is needed for /guess endpoint."}
	
	2 - A message to each user that joined your game
	{"message":"Waiting for other user to join the game with key e95vb086nk0lj21hboms ...\n"}
	
	3 - If the game has more than two users, than you receive the message below for each user that joined the game:
	{"message":"New user joined! Wainting for more 1"}

	4 - The last message with the summarization of the players party process:
 
	Example with 2 players:
	
	{"message":"Now you can start guessing at /multiplayer/guess endpoint! We will wait for both guesses to return an anwser.","users":["Roberto","Antonio"],"colors":["R","B","G","Y","O","P","C","M"],"code_length":8,"game_key":"e95vb086nk0lj21hboms","solved":false}

	Example with 3 players:
	{"message":"Now you can start guessing at /multiplayer/guess endpoint! We will wait for both guesses to return an anwser.","users":["Roberto","Juuba","Antonio"],"colors":["R","B","G","Y","O","P","C","M"],"code_length":8,"game_key":"dfqe5itvkip53smpghul","solved":false}


	
**/multiplayer/join_game** (Alternatively invoked from a simple html page, that can be acessed typing /multiplayer/join_game at the browser)
Input:

{ "user": "Roberto Costa" ,  "gameKey" : "pc73mccpf3033gpnuasv"}

Expected Result:

	If it is a game with only two users you will receive two messages:
	
	{"message":"Your user key is 3kmnaqoct893t5e3jnlf. It is needed for /guess endpoint."}
	{"message":"Now you can start guessing at /multiplayer/guess endpoint! We will wait for both guesses to return an anwser.","users":["Roberto","Antonio"],"colors":["R","B","G","Y","O","P","C","M"],"code_length":8,"game_key":"e95vb086nk0lj21hboms","solved":false}

	If the play has more than 2 users, than you will receive the two messages above and some messages of the remaining users entering until the party is complete:
	{"message":"New user joined! Wainting for more 1"}
	
	If you try to join a full party you receive an error:
	{"message": "There are some error(s) with your request.",
		"errors": ["Number of users exceeded for this game. Please choose another one."],
		"gameKey": "cbg3e3n0190js8u1e665"
	}

	
**/multiplayer/guess** (Alternatively invoked from a simple html page, that can be acessed typing /multiplayer/guess at the browser)

{ "user_key": "3kmnaqoct893t5e3jnlf" ,  "game_key" : "pc73mccpf3033gpnuasv", "code" : "YCPOMRGB" }

Expected Result:

	After you make your guess will you receive the notification of the players that still didint guess.
	{"message":"Waiting for user(s) to guess [Roberto, Antonio] ..."}
	{"message":"Waiting for user(s) to guess [Roberto] ..."}

	When all players finished the guessing you will receive the sumarization of guesses:
	{"message":"End of turn. Prepare for another round at /multiplayer/guess endpoint!","users":["Roberto","Antonio","robertobsc@gmail.com"],"colors":["R","B","G","Y","O","P","C","M"],"code_length":8,"game_key":"cbg3e3n0190js8u1e665","num_guesses":3,"past_results":[{"guess":"RBGYOPCM","near":8,"user":"robertobsc@gmail.com"},{"guess":"BCPYRMOG","near":8,"user":"Antonio"}],"solved":false,"result":[{"guess":"RBGYOPCM","near":8,"user":"robertobsc@gmail.com"},{"guess":"BCPYRMOG","near":8,"user":"Antonio"},{"guess":"RBGYOPCM","near":8,"user":"Roberto"}]}
	
	If someone wins we have a summarization of the game:
	{"message":"Seems we've got a winner!!!","users":["Roberto","Antonio"],"colors":["R","B","G","Y","O","P","C","M"],"code_length":8,"game_key":"e95vb086nk0lj21hboms","num_guesses":8,"past_results":[{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Roberto"},{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Antonio"},{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Roberto"},{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Antonio"},{"exactly":8,"guess":"BCPYRMOG","user":"Roberto"},{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Antonio"},{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Antonio"}],"solved":true,"result":[{"exactly":1,"guess":"RBGYOPCM","near":7,"user":"Antonio"},{"exactly":8,"guess":"BCPYRMOG","user":"Roberto"}],"time_taken":1492.394,"winner":"Roberto"}
