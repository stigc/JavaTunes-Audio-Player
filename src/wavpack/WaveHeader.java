/*
** WaveHeader.java
**
** Copyright (c) 2007 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)
**
*/
package wavpack;

class WaveHeader
{
    int FormatTag, NumChannels;		// was ushort in C
    long SampleRate, BytesPerSecond;	// was uint32_t in C
    int BlockAlign, BitsPerSample;		// was ushort in C
}