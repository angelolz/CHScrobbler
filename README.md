# CHScrobbler
CHScrobbler is a small Java app designed to get the currently playing song information from exported text files of Clone Hero, ScoreSpy, and YARG, utilizing this data to scrobble to last.fm. 
This app was created by a request of a friend who is a big user of last.fm. I decided to put this out in public for those that may be interested in scrobbling their songs when playing these games.

## Installation/Usage

**Note: If you are using YARG, skip to step 2.**

0. Before starting the app, please make sure that **"Export Current Song"** is enabled in your Clone Hero/ScoreSpy Settings.
1. Go to your Clone Hero/ScoreSpy data folder and edit `settings.ini`. 
   Near the bottom, you should find a `custom_song_export` property. Add `%n%b` to it. It should look like `custom_song_export = %s%n%a%n%c%n%b`. 
   You may need to restart Clone Hero after changing these settings. Refer below for the locations of the default data folders.
   - Windows
     - Clone Hero: `%HOMEPATH%\Documents\Clone Hero`
     - ScoreSpy: `%PROGRAMFILES%\ScoreSpy Launcher\GameData\100`
   - Mac
     - Clone Hero: `~/Clone Hero`
     - ScoreSpy: *n/a*
   - Linux
     - Clone Hero: `~/.clonehero`
     - ScoreSpy: `~/ScoreSpy/100`

2. Download the latest version of **CHScrobbler**.
3. Open the program. Windows will use the `.exe` file, while Mac and Linux users will need to use the `.jar` file.
   - For Mac and Linux users, use this command: `java -jar <path to .jar file>`
4. Upon opening the program, it will prompt you to provide your last.fm credentials so that it may be able to scrobble under your last.fm account.
   - You can get your last.fm api details [here](https://www.last.fm/api/account/create).
   - The program may ask you for locations of your data folders if it can't find the default ones.
5. After getting the app set up, you can play a song. After playing the song for 30 seconds, the app shows a log of
   the song you're currently playing and lets you know if a song was successfully scrobbled.


## Dependencies Used
- [lastfm-java](https://github.com/jkovacs/lastfm-java)

## Support/Help
If you encounter any bugs or would like to provide improvements or feedback, please tweet it at me ([@angelolz1](https://twitter.com/angelolz1)) or open up an issue!

## Contributions
If you have any improvements that you think would benefit the app a lot, please feel free to open a pull request and describe the changes you've made. I'll try my best to look at it as soon as possible!

## License
This project uses the MIT license.

# FAQ
### CHScrobbler keeps showing that I'm playing a song every second and nothing scrobbles! (Clone Hero/ScoreSpy)
- This is probably because CHScrobbler is unable to properly read your current playing format. Use these formats:
  - Scrobbling with album name:
    `custom_song_export = %s%n%a%n%c%n%b`
  - Scrobbling **without** album name: `custom_song_export = %s%n%a%n%c`

### The app won't open for me!
- This is probably because you don't have Java installed yet. If you did install Java, your environment variables
  might've not been set properly. Follow the steps for
  Windows in [this site](https://www.geeksforgeeks.org/how-to-set-java-path-in-windows-and-linux/) to set Java in your `PATH` system variable.

### CHScrobbler has the song/artist/album swapped around!
- You may have changed your `custom_song_export` in your `settings.ini` file and forgot to restart your game and/or CHScrobbler.

### Why did CHScrobbler tell me it couldn't scrobble the current playing song?
- This may be because of inaccurate metadata being given to last.fm. 
  In Clone Hero/ScoreSpy if there's no album/artist in the song info, it would show "Unknown Artist" or "Unknown Album" and last.fm ignores these.