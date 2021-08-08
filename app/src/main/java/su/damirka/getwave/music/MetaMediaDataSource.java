package su.damirka.getwave.music;

import android.media.MediaDataSource;

public class MetaMediaDataSource extends MediaDataSource
{
    private final String FilePath;
    private int BufferSize;
    private byte[] pData;
    private boolean InitializeBuffer;

    protected native int GetStreamFromServer(String Path, byte[] buffer, int offset, long position, int size);
    //public native byte[] GetFileFromServer(String Path);

    public MetaMediaDataSource(String Path)
    {
        FilePath = Path;
        BufferSize = 32768;
        pData = new byte[BufferSize];
        InitializeBuffer = true;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size)
    {
        if(InitializeBuffer)
        {
            GetStreamFromServer(FilePath, pData, 0, 0, BufferSize);
            InitializeBuffer = false;
        }

        while(position + BufferSize > pData.length)
        {
            byte[] temp = new byte[pData.length + BufferSize];
            System.arraycopy(pData, 0, temp, 0, pData.length);
            GetStreamFromServer(FilePath, temp, pData.length, pData.length, BufferSize);
            pData = temp;
        }

        System.arraycopy(pData, (int) position, buffer, offset, size);
        return size;

    }

    @Override
    public long getSize()
    {
        return 0L;
    }

    @Override
    public void close()
    {

    }
}