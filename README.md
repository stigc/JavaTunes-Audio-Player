# JavaTunes-Audio-Player
This is the playback and tag parsing code extracted from my very old JavaTunes project (http://stigc.dk/projects/JavaTunes/). Some of the code is from before 2004 (Java 1.4). All playback is native Java code. No libs required.

Decoders

	Ogg Vorbis, FLAC, MP3, AAC, ALAC and WavPack

Tags supported

	ID3v1, ID3v2, Ogg Comments, APEv2 and QuickTime

Features

	SHOUTcast, Replay Gain, Gapless playback, lyrics, cover art, multiple artists and genres

Usage

		File file = new File("my file");
		Track track = new TagReaderManager().read(file);
		System.out.println(track.toString());
		
		AudioPlayer audioPlayer = new AudioPlayer();
		AudioInfo ai = audioPlayer.play(track, true, false);
				
		while (audioPlayer.playing)
		{
			Thread.sleep(1000);	
			synchronized (ai)
			{
				System.out.println(ai.toString());
			}
		}

or without parsing tags, 1 line of code

	new AudioPlayer().play("my file");

Note that seeking is not supported.
