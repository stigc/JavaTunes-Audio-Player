# JavaTunes-Audio-Player
This is the playback and tag parsing code extracted from my very old JavaTunes project (http://stigc.dk/projects/JavaTunes/). Some of the code is from before 2004 (Java 1.4). All playback is native Java code. No libs required.

Decoders

	Ogg Vorbis, Opus, FLAC, MP3, AAC, ALAC and WavPack

Tags supported

	ID3v1, ID3v2, Ogg Comments, APEv2 and QuickTime

Other features

	SHOUTcast, HLS (AAC ADTS only), Replay Gain, Gapless playback, lyrics, cover art,
	multiple artists and genres, FLAC encoder

Usage

	File file = new File("my file");
	Track track = new TagReaderManager().read(file);
	write(track.toString());

	AudioPlayer player = new AudioPlayer();
	player.play(track, false);

	while (player.isPlaying())
	{
		write(player.getAudioInfo().toString());
		Thread.sleep(1000);
	}

or without parsing tags, 1 line of code

	new AudioPlayer().play("http://some radio station");

Record everything the AudioPlayer plays to a FLAC file

	AudioPlayer player = new AudioPlayer();
	player.enableFlacOutput(new File("output.flac"));
	player.setOutputToMixer(false);
	player.play("my file");
	player.waitUntilCurrentAudioHasEndeded();
	player.finishFlacOutput();

	player.setOutputToMixer(true); 
	player.play("output.flac");
	while (player.isPlaying()) 
	{
		write(player.getAudioInfo().toString());
		Thread.sleep(1000);
	}
