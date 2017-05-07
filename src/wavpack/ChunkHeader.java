/*
** ChunkHeader.java
**
** Copyright (c) 2007 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
*/

package wavpack;

class ChunkHeader
{
    char ckID[] = new char[4];
    long ckSize;	// was uint32_t in C
}