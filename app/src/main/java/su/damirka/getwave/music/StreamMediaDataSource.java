//package su.damirka.getwave.music;
//
//import android.media.MediaDataSource;
//import java.util.Objects;
//
//public class StreamMediaDataSource extends MediaDataSource
//{
//    private final String FilePath;
//    private final int ChunkSize;
//    private byte[] pData;
//    private long FileSize;
//    private long Iterator;
//    private boolean Running;
//    private boolean Closed;
//    private Thread Downloader;
//
//    private native int GetStreamFromServer(String Path, byte[] buffer, int offset, long position, int size);
//    private native long GetSizeofFile(String Path);
//    //public native byte[] GetFileFromServer(String Path);
//
//    public StreamMediaDataSource(String Path)
//    {
//        FilePath = Path;
//        ChunkSize = 262144;
//        Downloader = null;
//        Iterator = 0;
//        FileSize = -1;
//        Closed = true;
//    }
//
//    public boolean IsDownloaded()
//    {
//        return Iterator == FileSize;
//    }
//
//    public long GetIterator()
//    {
//        return Iterator;
//    }
//
//    public void PrepareASync()
//    {
//        while(!Closed)
//        {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        Running = true;
//        Closed = false;
//        Downloader = new Thread(this::Run);
//        Downloader.start();
//    }
//
//    public long GetFileSize()
//    {
//        return FileSize;
//    }
//
//    private void Run()
//    {
//        System.out.println("Stream thread started!");
//        if(Iterator == FileSize)
//            return;
//
//        if(Objects.isNull(pData))
//        {
//            FileSize = GetSizeofFile(FilePath);
//            pData = new byte[(int) FileSize];
//            Iterator = 0;
//        }
//
//        int ReadSize = ChunkSize;
//
//        while(Running && Iterator != FileSize)
//        {
//            if(Iterator + ReadSize > FileSize)
//            {
//                ReadSize = (int) (FileSize - Iterator);
//                if(ReadSize <= 0)
//                    break;
//            }
//            GetStreamFromServer(FilePath, pData, (int) Iterator, Iterator, ReadSize);
//            Iterator += ReadSize;
//        }
//
//        Running = false;
//    }
//
//    @Override
//    public int readAt(long position, byte[] buffer, int offset, int size)
//    {
//        while (Objects.isNull(pData) || position + size > Iterator)
//        {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }
//
//            if(FileSize != -1)
//            {
//                if(position + size > FileSize)
//                {
//                    size = (int) (FileSize - position);
//                    break;
//                }
//            }
//
//            if(!Running)
//                return -1;
//        }
//
//        System.arraycopy(pData, (int) position, buffer, offset, size);
//        return size;
//    }
//
//    public boolean Equals(StreamMediaDataSource another)
//    {
//        boolean Result = true;
//
//        if(!FilePath.equals(another.FilePath))
//            Result = false;
//
//        if(FileSize != another.FileSize)
//            Result = false;
//
//        return Result;
//    }
//
//    public void CloseAsync()
//    {
//        if(!Closed)
//        {
//            Closed = true;
//            Running = false;
//            try {
//                Downloader.join();
//                Downloader = null;
//                pData = null;
//                Iterator = 0;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public long getSize()
//    {
//        if(FileSize > 0)
//            return FileSize;
//        return 0L;
//    }
//
//
//    // We don't use default close because we need to close stream at specific time
//    @Override
//    public void close()
//    {
//    }
//}


package su.damirka.getwave.music;

import android.media.MediaDataSource;

import java.util.Objects;

import su.damirka.getwave.files.CacheManager;
import su.damirka.getwave.files.TrackCacheFile;

public class StreamMediaDataSource extends MediaDataSource
{
    private final long Id;
    private final String FilePath;
    private int ChunkSize;
    private long FileSize;
    private long Iterator;
    private long CurrentPosition;

    private byte[] pData;

    private Thread Downloader;
    private boolean Exit;

    private boolean ErrorFlag;
    private String ErrorMessage;

    private native int GetStreamFromServer(String Path, byte[] buffer, int offset, long position, int size);
    private native long GetSizeofFile(String Path);

    public StreamMediaDataSource(String Path, long Id)
    {
        FilePath = Path;
        this.Id = Id;
        SetDefault();
    }

    private void SetDefault()
    {
        ChunkSize = 131072;
        pData = null;
        Downloader = null;
        FileSize = -1;
        Iterator = 0;
        CurrentPosition = 0;
        Exit = false;

        ErrorFlag = false;
        ErrorMessage = "";
    }

    public long GetIterator()
    {
        return Iterator;
    }

    public void PrepareASync()
    {
        if(!Objects.isNull(pData))
            return;
        else if(Iterator == FileSize)
            return;

        CacheManager CM = MusicService.GetCacheManager();

        TrackCacheFile TrackCache = CM.LoadTrackCache(Id);

        if(Objects.nonNull(TrackCache))
        {
            pData = TrackCache.GetData();
            FileSize = pData.length;
            Iterator = TrackCache.GetIterator();
        }
        else
        {
            FileSize = GetSizeofFile(FilePath);

            if(FileSize == -1)
            {
                ErrorFlag = true;
                ErrorMessage = "Can't get track size from server\n";
                return;
            }

            pData = new byte[(int)FileSize];
            Iterator = 0;
        }

        Downloader = new Thread(this::Run);
        Downloader.start();
    }

    public boolean CheckError()
    {
        return ErrorFlag;
    }

    public String GetErrorMessage()
    {
        return ErrorMessage;
    }

    private void Run()
    {
        if(Iterator == FileSize)
            return;

        int ReadSize = ChunkSize;

        while(Iterator != FileSize)
        {
            while(Iterator > CurrentPosition + ChunkSize * 20L)
            {
                if(Exit)
                    return;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            for(int i = 0; i < 30 && Iterator != FileSize; i++)
            {
                if(Iterator + ChunkSize > FileSize)
                {
                    ReadSize = (int) (FileSize - Iterator);

                    if(ReadSize == 0)
                        return;
                }

                int res = GetStreamFromServer(FilePath, pData, (int) Iterator, Iterator, ReadSize);

                if(res == -1)
                {
                    ErrorFlag = true;
                    ErrorMessage = "Can't receive data from server\n";
                    continue;
                }

                Iterator += ReadSize;

                if(Exit)
                    return;

//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ignored) {
//                }
            }
        }
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size)
    {
        CurrentPosition = position;

        if(CurrentPosition >= FileSize || Exit)
            return -1;

        while(CurrentPosition > Iterator)
        {
            if(Exit)
                return -1;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        if(CurrentPosition + size > FileSize)
            size -= (position + size) - FileSize;

        System.arraycopy(pData, (int) position, buffer, offset, size);
        return size;
    }

    public void CloseAsync()
    {
        CacheManager CM = MusicService.GetCacheManager();
        if(Objects.nonNull(CM))
        {
            TrackCacheFile TrackCache = new TrackCacheFile(Id, pData, Iterator);
            CM.SaveAsTrackCache(TrackCache);
        }

        Exit = true;

        try {
            if(Objects.nonNull(Downloader))
                Downloader.join();
        } catch (InterruptedException ignored) {
        }

        SetDefault();
    }

    @Override
    public long getSize()
    {
        if(FileSize >= 0)
            return FileSize;
        return 0L;
    }

    // We don't use default close because we need to close stream at specific time
    @Override
    public void close()
    {
    }
}