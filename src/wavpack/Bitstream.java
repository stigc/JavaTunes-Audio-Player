/*
** Bitstream.java
**
** Copyright (c) 2007 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)
**
*/
package wavpack;

class Bitstream
{
    short end, ptr;	// was uchar in c
    long file_bytes, sr;	// was uint32_t in C
    int error, bc;
    java.io.DataInputStream file;
    int bitval = 0;
    byte[] buf = new byte[1024];
    int buf_index = 0;
}