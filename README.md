# CHScrobbler
CHScrobbler is a simple Java app that reads the exported currently playing song text file from Clone Hero and uses that information for scrobbling to last.fm.

This app was created by a request of a friend who is a big user of last.fm. I decided to put this out in public for those that may be interested in scrobbling their songs in Clone Hero.

## Installation/Usage
0. Before starting the app, please make sure that **"Export Current Song"** is enabled in your Clone Hero Settings.
1. Go to your Clone Hero data folder settings and edit `settings.ini`. Near the bottom, you should find a `custom_song_export` property. Add `%n%b` to it. It should look like `custom_song_export = %s%n%a%n%c%n%b`. You may need to restart Clone Hero after changing these settings.
   - For Windows, the data folder is in `%USERPROFILE%\AppData\LocalLow\srylain Inc_\Clone Hero`.
   - For Mac, the data folder is in `~/Clone Hero`.
   - For Linux, the data folder is in `~/.clonehero`.

2. Download the latest version of **CHScrobbler**.
3. Place the `.exe`/`.jar` file inside your own **Clone Hero directory**.
4. You may open the program. Upon opening the program, it will prompt you to provide your last.fm credentials so that it may be able to scrobble under your name.  

    ***Note***: If you don't want to do the setup, create a file called `config.txt` in the same directory as the .exe and put these properties in it and fill it in: 
    ```
    lastfm_apikey=
    lastfm_secret=
    lastfm_username=
    lastfm_password=
    scrobble_threshold_seconds=30
    ```

You can get your last.fm api details [here](https://www.last.fm/api/account/create).

5. After getting the app set up, you can play a song. After playing the song for 30 seconds (or whatever `scrobble_threshold_seconds` is set to), the app provides a log of the song you're currently playing and lets you know if a song was successfully scrobbled.


## Dependencies Used
- [lastfm-java](https://github.com/jkovacs/lastfm-java)

## Support/Help
If you encounter any bugs or would like to provide improvements or feedback, please tweet it at me ([@angelolz1](https://twitter.com/angelolz1)) or open up an issue!

## Contributions
If you have any improvements that you think would benefit the app a lot, please feel free to open a pull request and describe the changes you've made. I'll try my best to look at it as soon as possible!

## License
This project uses the MIT license.

# FAQ
### CHScrobbler keeps showing that I'm playing a song every second and nothing scrobbles!
- This is probably because CHScrobbler is unable to properly read your current playing format. Use these formats:
  - Scrobbling with album name:
    `custom_song_export = %s%n%a%n%c%n%b`
  - Scrobbling **without** album name: `custom_song_export = %s%n%a%n%c`

### The app won't open for me!
- This is probably because you don't have Java installed yet. If you did install Java, your environment variables
  might've not been set properly. Follow the steps for
  Windows in [this site](https://www.geeksforgeeks.org/how-to-set-java-path-in-windows-and-linux/) to set Java in your `PATH` system variable.

### CHScrobbler has the song/artist/album swapped around!
- You may have changed your `custom_song_export` in your `settings.ini` file and forgot to restart Clone Hero and/or CHScrobbler. 