import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker extends Thread
{
  public int workerId = 1;

  private Socket client;
  private PrintWriter clientWriter;
  private BufferedReader clientReader;
  private ServerMaster master;

  public ServerWorker(Socket client, ServerMaster master)
  {
    this.client = client;
    this.master = master;

    try
    {
      //          PrintWriter(OutputStream out, boolean autoFlushOutputBuffer)
      clientWriter = new PrintWriter(client.getOutputStream(), true);
    }
    catch (IOException e)
    {
      System.err.println("Server Worker: Could not open output stream");
      e.printStackTrace();
    }
    try
    {
      clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

    }
    catch (IOException e)
    {
      System.err.println("Server Worker: Could not open input stream");
      e.printStackTrace();
    }
  }

  //Called by ServerMaster
  public void send(String msg)
  {
    System.out.println("ServerWorker.send(" + msg + ")");
    clientWriter.println(msg);
  }

  public void run()
  {
    try
    {
      System.out.println("Listening to Client");
      String msg = clientReader.readLine();
      if (msg.startsWith("quit:"))
      {
        master.cleanConnectionList(this);
      }
      else if (msg.startsWith("You just bought "))
      {
        System.out.println("Success: " + msg);
      }
      else if (msg.startsWith("Error"))
      {
        System.out.println("Failed: " + msg);
      }
      else
      {

      }

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }


}
