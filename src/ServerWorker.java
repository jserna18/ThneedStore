import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ServerWorker communicates information from the ServerMaster to the Client
 * and vice versa.
 */
public class ServerWorker extends Thread
{
  public int workerId;

  private Socket client;
  private PrintWriter clientWriter;
  private BufferedReader clientReader;
  private ServerMaster master;
  private ThneedStore store;

  /**
   * Constructor which creates a new ServerWorker and sets the PrintWriter
   * to the sockets output stream and a BufferedReader to the input stream
   * @param client
   * @param master
   * @param store
   */
  public ServerWorker(Socket client, ServerMaster master, ThneedStore store)
  {
    this.client = client;
    this.master = master;
    this.store = store;

    try
    {
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

  /**
   * Called by ServerMaster
   * @param msg
   */
  public void send(String msg)
  {
    clientWriter.println(msg);
  }

  /**
   * Converts the given string into an amount and value which will be
   * either bought or sold from the ThneedStore
   * @param msg
   */
  private void buyAndSell(String msg)
  {
    synchronized (this)
    {
      int idxNumber = msg.indexOf(':');
      String sub = msg.substring(idxNumber + 2);
      int numberEnds = sub.indexOf(' ');
      int number = Integer.parseInt(sub.substring(0, numberEnds));
      double price = Double.parseDouble(sub.substring(numberEnds + 1));

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

  /**
   * Reads input from the Sockets InputStream. Will either
   * buy or sell Thneeds from ThneedStore until told to "quit"
   */
  public void run()
  {
    try
    {
      int count = 0;
      while (true)
      {

        String msg = clientReader.readLine();

        if(msg == null)
        {
          break;
        }

        else if (msg.startsWith("quit:"))
        {
          master.cleanConnectionList(this);
        }
        else if (msg.startsWith("buy:") || msg.startsWith("sell:"))
        {
          buyAndSell(msg);
        }
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
