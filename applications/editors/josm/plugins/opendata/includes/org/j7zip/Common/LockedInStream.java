package org.j7zip.Common;

import org.j7zip.SevenZip.IInStream;

public class LockedInStream {
    IInStream _stream;
    
    public LockedInStream() {
    }
    
    public void Init(IInStream stream) {
        _stream = stream;
    }
        
    public synchronized int read(long startPos, byte  [] data, int off, int size) throws java.io.IOException {
        // NWindows::NSynchronization::CCriticalSectionLock lock(_criticalSection);
        _stream.Seek(startPos, IInStream.STREAM_SEEK_SET);
        return _stream.read(data,off, size);
    }
}
