# JavaTunes-Audio-Player
Decoders Ogg Vorbis, FLAC, MP3, AAC, ALAC and WavPack

Tags supported: ID3v1, ID3v2, Ogg Comments, APEv2 and QuickTime

Features: Shourcast, Replay Gain, Gapless playback, lyrics, cover art, multiple artists and genres


		File file = new File(...);
		Track track = new TagReaderManager().read(file);
		System.out.println(track.toString());
		
		AudioPlayer audioPlayer = new AudioPlayer();
		AudioInfo ai = audioPlayer.play(track, true, false);
				
		while (true)
		{
			Thread.sleep(1000);	
			synchronized (ai)
			{
				System.out.println(ai.toString());
			}
		}
