import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.atomic.DoubleAccumulator;

public class Client
{
  private Socket clientSocket;
  private PrintWriter write;
  private BufferedReader reader;
  private long startNanoSec;
  private Scanner keyboard;
  private ClientSocketListener listener;
  private Scanner inputStream;

  private volatile int ThneedsInStore;
  private volatile double Treasury = 1000.00;

  /**
   * Constructor which allows for an input file to be read as commands from the
   * user.
   * @param host
   * @param portNumber
   * @param file
   */
  public Client(String host, int portNumber, String file)
  {
    try
    {
      inputStream = new Scanner(new File(file));
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }

    start(host, portNumber);
  }

  /**
   * Constructor which sets the inputStream to accept from keyboard input
   * @param host
   * @param portNumber
   */
  public Client(String host, int portNumber)
  {
    startNanoSec = System.nanoTime();
    System.out.println("Starting Client: " + timeDiff());

    inputStream = new Scanner(System.in);

    start(host, portNumber);
  }

  /**
   *
   * @param host
   * @param portNumber
   */
  private void start(String host, int portNumber)
  {
    while (!openConnection(host, portNumber))
    {
      try
      {
        Thread.sleep(1000);
      } catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }

    listener = new ClientSocketListener();
    System.out.println("Client(): Starting listener = : " + listener);
    listener.start();

    listenToUserRequests();

    try
    {
      System.out.println("Client closing");
      Thread.sleep(10000);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    closeAll();
  }

  /**
   *
   * @param host
   * @param portNumber
   * @return
   */
  private boolean openConnection(String host, int portNumber)
  {

    try
    {
      clientSocket = new Socket(host, portNumber);
    }
    catch (UnknownHostException e)
    {
      System.err.println("Client Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open connection to " + host
              + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      write = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open output stream");
      e.printStackTrace();
      return false;
    }
    try
    {
      reader = new BufferedReader(new InputStreamReader(
              clientSocket.getInputStream()));
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open input stream");
      e.printStackTrace();
      return false;
    }
    return true;

  }

  private boolean enoughThneedsAndMoney(String cmd, char c)
  {
    synchronized (this)
    {
      if (c == 'b')
      {
        String sub = cmd.substring(5);
        int number = Integer.parseInt(sub.substring(0, sub.indexOf(' ')));
        double price = Double.parseDouble(sub.substring(sub.indexOf(' ') + 1, sub.length() - 1));
        double finalPrice = number * price;

        if ((finalPrice > Treasury) || (number < 1))
        {
          return false;
        }
        else ThneedsInStore += number;
      }
      else
      {
        String sub = cmd.substring(6);
        int number = Integer.parseInt(sub.substring(0, sub.indexOf(' ')));
        if (((ThneedsInStore - number) < 0) || (number < 1))
        {
          return false;
        }
        else ThneedsInStore -= number;
      }
      System.out.println("Local inventory: " + ThneedsInStore + " treasury: " + Treasury);
      return true;
    }
  }

  /**
   *
   */
  private void listenToUserRequests()
  {
    while (inputStream.hasNext())
    {
      String cmd = inputStream.nextLine();
      if (cmd == null) continue;
      if (cmd.length() < 1) continue;
      char c = cmd.charAt(0);


      if(c == 'b' || c == 's')
      {
        if(!enoughThneedsAndMoney(cmd, c))
        {
          continue;
        }
      }

      else if(c == 'i')
      {
        System.out.println("Current local Inventory: " + ThneedsInStore);
      }

      write.println(cmd);

      if(c == 'q') break;
    }
  }

  /**
   *
   */
  public void closeAll()
  {
    System.out.println("Client.closeAll()");

    if (write != null) write.close();
    if (reader != null)
    {
      try
      {
        reader.close();
        clientSocket.close();
      }
      catch (IOException e)
      {
        System.err.println("Client Error: Could not close");
        e.printStackTrace();
      }
    }

  }

  /**
   *
   * @return
   */
  private String timeDiff()
  {
    long namoSecDiff = System.nanoTime() - startNanoSec;
    double secDiff = (double) namoSecDiff / 1000000000.0;
    return String.format("%.6f", secDiff);

  }

  /**
   *
   * @param args
   */
  public static void main(String[] args)
  {

    String host = null;
    int port = 0;

    try
    {
      host = args[0];
      port = Integer.parseInt(args[1]);
      if (port < 1) throw new Exception();
    }
    catch (Exception e)
    {
      System.out.println("Usage: Client hostname portNumber");
      System.exit(0);
    }
    if(args.length == 3)
    {
      new Client(host, port, args[2]);
    }
    else
    {
      new Client(host, port);
    }
  }

  /**
   *
   */
  class ClientSocketListener extends Thread
  {

    public void run()
    {
      System.out.println("ClientSocketListener.run()");
      while (!clientSocket.isClosed())
      {
        try
        {
          if(reader.ready())
          {
            read();
          }
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }

      System.out.println("Client Master Socket Listener Closing");
    }

    /**
     * updates the clients copy of Thnneds and Treasury
     * @param msg
     */
    private  void updateThneeds(String msg)
    {
      synchronized (this)
      {
        String sub = msg.substring(msg.indexOf('=')+1);
        String amount = sub.substring(0, sub.indexOf(' '));
        String treasury = amount.substring(amount.indexOf('=')+1);

        ThneedsInStore = Integer.parseInt(amount);
        Treasury = Double.parseDouble(treasury);
      }
    }

    /**
     * reads from the sockets output stream and updates the Client's copy of inventory
     * and treasury.
     */
    private void read()
    {
      try
      {
        String msg = reader.readLine();

        if (msg.startsWith("time("))
        {
          updateThneeds(msg);

          // Print message for Professor/T.A. code evaluation
//          System.out.println(msg);
        }
        else
        {
          System.out.println("Unrecognized message from Server(" + timeDiff()
                  + ") = " + msg);
        }

      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

  }

}
