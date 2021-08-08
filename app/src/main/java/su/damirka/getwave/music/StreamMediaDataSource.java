package su.damirka.getwave.music;

import android.media.MediaDataSource;
import android.support.v4.app.INotificationSideChannel;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Objects;

public class StreamMediaDataSource extends MediaDataSource
{
    private final String FilePath;
    private final int ChunkSize;
    private byte[] pData;
    private long FileSize;
    private long Iterator;
    private boolean Running;
    private boolean Closed;
    private Thread Downloader;

    private native int GetStreamFromServer(String Path, byte[] buffer, int offset, long position, int size);
    private native long GetSizeofFile(String Path);
    //public native byte[] GetFileFromServer(String Path);

    public StreamMediaDataSource(String Path)
    {
        FilePath = Path;
        ChunkSize = 32768;
        Downloader = null;
        Iterator = 0;
        FileSize = -1;
        Closed = false;
    }

    public void PrepareASync()
    {
        while(Running)
        {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Running = true;
        Closed = false;
        Downloader = new Thread(this::Run);
        Downloader.start();
    }

    public long GetFileSize()
    {
        return FileSize;
    }

    private void Run()
    {
        System.out.println("Stream thread started!");
        if(Iterator == FileSize)
            return;

        if(Objects.isNull(pData))
        {
            FileSize = GetSizeofFile(FilePath);
            pData = new byte[(int) FileSize];
            Iterator = 0;
        }

        int ReadSize = ChunkSize;

        while(Running && Iterator != FileSize)
        {
            if(Iterator + ReadSize > FileSize)
            {
                ReadSize = (int) (FileSize - Iterator);
                if(ReadSize <= 0)
                    break;
            }
            GetStreamFromServer(FilePath, pData, (int) Iterator, Iterator, ReadSize);
            Iterator += ReadSize;
        }

        Running = false;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size)
    {
        while (Objects.isNull(pData) || position + size > Iterator)
        {
            try {
                Thread.sleep(50);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            if(FileSize != -1)
            {
                if(position + size > FileSize)
                {
                    size = (int) (FileSize - position);
                    break;
                }
            }

            if(!Running)
                return -1;
        }

        System.arraycopy(pData, (int) position, buffer, offset, size);
        return size;
    }

    public boolean Equals(StreamMediaDataSource another)
    {
        boolean Result = true;

        if(!FilePath.equals(another.FilePath))
            Result = false;

        if(FileSize != another.FileSize)
            Result = false;

        return Result;
    }

    public void CloseAsync()
    {
        if(!Closed)
        {
            Closed = true;
            Running = false;
            try {
                Downloader.join();
                Downloader = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public long getSize()
    {
        return 0L;
    }


    // We don't use default close because we need to close stream at specific time
    @Override
    public void close()
    {
    }
}