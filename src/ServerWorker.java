import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker extends Thread
{
  public int workerId;

  private Socket client;
  private PrintWriter clientWriter;
  private BufferedReader clientReader;
  private ServerMaster master;
  private ThneedStore store;

  public ServerWorker(Socket client, ServerMaster master, ThneedStore store)
  {
    this.client = client;
    this.master = master;
    this.store = store;

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

  //Called by ThneedStore
  public void requestFailed()
  {

  }

  public void run()
  {
    synchronized (this)
    {
      try
      {
        while (!client.isClosed() && clientReader.ready())
        {
          System.out.println("Listening to Client");
          String msg = clientReader.readLine();

          if (msg.startsWith("quit:"))
          {
            master.cleanConnectionList(this);
          }
          else if (msg.startsWith("buy:") || msg.startsWith("sell:"))
          {
            int idxNumber = msg.indexOf(':');
            String sub = msg.substring(idxNumber + 2);
            int numberEnds = sub.indexOf(' ');
            int number = Integer.parseInt(sub.substring(0, numberEnds));
            double price = Double.parseDouble(sub.substring(numberEnds + 1));

            System.out.println(number + " " + price);

            if (msg.charAt(0) == 'b')
            {
              store.buyThneeds(this, number, price);
            }
            else
            {
              store.sellThneeds(this, number, price);
            }
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }


}
