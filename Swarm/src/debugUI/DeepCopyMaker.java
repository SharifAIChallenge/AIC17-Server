package debugUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DeepCopyMaker
{
    private DeepCopyMaker()
    {
        //I made constructor private so that DeepCopyMaker could not
       // be created
    }

    static public Object makeDeepCopy(Object obj2DeepCopy) throws
            Exception
    {
        //obj2DeepCopy must be serializable
        ObjectOutputStream outStream = null;
        ObjectInputStream inStream = null;

        try
        {
            ByteArrayOutputStream byteOut =
                    new ByteArrayOutputStream();
            outStream = new
                    ObjectOutputStream(byteOut);
            // serialize and write obj2DeepCopy to
            //byteOut
            outStream.writeObject(obj2DeepCopy);
            //always flush your stream
            outStream.flush();

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            inStream = new ObjectInputStream(byteIn);
            // read the serialized, and deep copied,
            //object and return it
            return inStream.readObject();
        }
        catch(Exception e)
        {
            //handle the exception
            //it is not a bad idea to throw the exception, so
            //that the caller of the
            //method knows something went wrong
            throw(e);
        }
        finally
        {
            //always close your streams in finally clauses
            outStream.close();
            inStream.close();
        }
    }
}
