# CHScrobbler
CHScrobbler is a simple Java app that reads the exported currently playing song text file from Clone Hero and uses that information for scrobbling to last.fm.

This app was created by a request of a friend who is a big user of last.fm. I decided to put this out in public for those that may be interested in scrobbling their songs in Clone Hero.

## Installation/Usage
0. Before starting the app, please make sure that **"Export Current Song"** is enabled in your Clone Hero Settings.
1. Download the latest version of **CHScrobbler**.
2. Place the exe file inside your own **Clone Hero directory**.
3. You may open the program. Upon opening the program, it will prompt you to provide your last.fm credentials so that it may be able to scrobble under your name.  

    ***Note***: If you don't want to do the setup, create a file called `config.txt` in the same directory as the .exe and put these properties in it and fill it in: 
```
lastfm_apikey=
lastfm_secret=
lastfm_username=
lastfm_password=
```

You can get your last.fm api details [here](https://www.last.fm/api/account/create).

4. After getting the app set up, feel free to start playing a song. After playing the song for quite a while, the app will start scrobbling your current song. The app provides a log of the song you're currently playing and lets you know if a song was successfully scrobbled.


## Dependencies Used
- [lastfm-java](https://github.com/jkovacs/lastfm-java)

## Support/Help
If you encounter any bugs or would like to provide improvements or feedback, please tweet it at me ([@angelolz1](https://twitter.com/angelolz1))!

## Contributions
If you have any improvements that you think would benefit the app a lot, please feel free to open a pull request and describe the changes you've made. I'll try my best to look at it as soon as possible!

## License
This project uses the MIT license.
