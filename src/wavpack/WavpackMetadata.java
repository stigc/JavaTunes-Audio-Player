/*
** WavpackMetadata.java
**
** Copyright (c) 2007 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
package wavpack;

class WavpackMetadata
{
    int byte_length;
    byte data[];
    short id;	// was uchar in C
    int hasdata = 0;	//0 does not have data, 1 has data
    int status = 0;	// 0 ok, 1 error
}