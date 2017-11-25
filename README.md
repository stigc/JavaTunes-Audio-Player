# JavaTunes-Audio-Player
This is the playback and tag parsing code extracted from my very old JavaTunes project (http://stigc.dk/projects/JavaTunes/). Some of the code is from before 2004 (Java 1.4). All playback is native Java code. No libs required.

Decoders

	Ogg Vorbis, FLAC, MP3, AAC, ALAC and WavPack

Tags supported

	ID3v1, ID3v2, Ogg Comments, APEv2 and QuickTime

Features

	SHOUTcast, Replay Gain, Gapless playback, lyrics, cover art, multiple artists and genres, FLAC encoder

Usage

	File file = new File("WavPack\\8bit.wv");
	Track track = new TagReaderManager().read(file);
	System.out.println(track.toString());
	
	AudioPlayer player = new AudioPlayer();
	AudioInfo ai = player.play(track, false);
	
	while (player.isPlaying()) 
	{
		System.out.println(ai.toString());
		Thread.sleep(1000);
	}

or without parsing tags, 1 line of code

	new AudioPlayer().play("my file");

Pipe everything to FLAC file

	AudioPlayer player = new AudioPlayer();
	player.enableFlacOutput(new File("output.flac"));
	player.setOutputToMixer(false); //uncomment to disable sound in speakers 
	player.play("ALAC\\08 Lilac.m4a");
	
	while (player.isPlaying()) 
		Thread.sleep(1000);

	player.finishFlacOutput();

Note that seeking is not supported.
