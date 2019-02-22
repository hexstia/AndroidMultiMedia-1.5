package com.genymobile.scrcpy;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public class IO {
    private IO() {
        // not instantiable
    }
    public static int  readFully(FileDescriptor fd, byte[] from,int offset,int len) throws IOException {
    int count =0;
    while(count <len ) {
        try {
          int p = Os.read(fd, from, count, len);
            if(p ==0||p==-1){
                return -1;
            }
            count += p;
        } catch (ErrnoException e) {
            if (e.errno != OsConstants.EINTR) {
                throw new IOException(e);
            }
            return -1;
        }
    }
    return count ;

    }
    public static void readFully(FileDescriptor fd, ByteBuffer from) throws IOException {
        while (from.hasRemaining()) {
            try {
                Os.read(fd, from);
            } catch (ErrnoException e) {
                if (e.errno != OsConstants.EINTR) {
                    throw new IOException(e);
                }
            }
        }
    }

    public static void writeFully(FileDescriptor fd, ByteBuffer from) throws IOException {
        while (from.hasRemaining()) {
            try {
                Os.write(fd, from);

            } catch (ErrnoException e) {
                if (e.errno != OsConstants.EINTR) {
                    throw new IOException(e);
                }

            }
        }
    }

    public static void writeFully(FileDescriptor fd, byte[] buffer, int offset, int len) throws IOException {
        writeFully(fd, ByteBuffer.wrap(buffer, offset, len));
    }

}
