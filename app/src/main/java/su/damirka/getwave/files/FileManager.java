package su.damirka.getwave.files;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import su.damirka.getwave.activities.MainActivity;

public class FileManager
{
    private File Directory;

    public FileManager(MainActivity MA)
    {
        ContextWrapper contextWrapper = new ContextWrapper(MA.getApplicationContext());
        Directory = contextWrapper.getDir(MA.getFilesDir().getName(), Context.MODE_PRIVATE);
    }

    public void Save(byte[] bytes, String Filename)
    {
        File file =  new File(Directory, Filename);
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] Read(String Filename)
    {
        File file =  new File(Directory, Filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            return data;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void SaveObj(Object obj, String Filename)
    {
        File file =  new File(Directory, Filename);
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object ReadObj(String Filename)
    {
        File file =  new File(Directory, Filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object data = ois.readObject();
            ois.close();
            fis.close();
            return data;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
