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
  private volatile double treasury = 1000.00;

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

  public Client(String host, int portNumber)
  {
    startNanoSec = System.nanoTime();
    System.out.println("Starting Client: " + timeDiff());

    inputStream = new Scanner(System.in);

    start(host, portNumber);
  }

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

  private void listenToUserRequests()
  {
    while (inputStream.hasNext())
    {
      String cmd = inputStream.nextLine();
      if (cmd == null) continue;
      if (cmd.length() < 1) continue;
      char c = cmd.charAt(0);

      if(c == 'b')
      {
        String sub = cmd.substring(5);
        int number = Integer.parseInt(sub.substring(0, sub.indexOf(' ')));
        double price = Double.parseDouble(sub.substring(sub.indexOf(' ')+1, sub.length()-1));
        double finalPrice = number*price;
        if(finalPrice > treasury)
        {
          continue;
        }
        ThneedsInStore += number;
      }

      else if(c == 's')
      {
        String sub = cmd.substring(6);
        int number = Integer.parseInt(sub.substring(0, sub.indexOf(' ')));
        if((ThneedsInStore - number) < 0)
        {
          continue;
        }
        ThneedsInStore -= number;
      }
      else if(c == 'i')
      {
        System.out.println("Current local Inventory: " + ThneedsInStore);
      }

//      else if (c == 'q') break;

      write.println(cmd);
    }
  }

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

  private String timeDiff()
  {
    long namoSecDiff = System.nanoTime() - startNanoSec;
    double secDiff = (double) namoSecDiff / 1000000000.0;
    return String.format("%.6f", secDiff);

  }

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
    if(args[2] != null)
    {
      new Client(host, port, args[2]);
    }
    else
    {
      new Client(host, port);
    }
  }

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

    private void read()
    {
      try
      {
        System.out.println("Client: listening to socket");
        String msg = reader.readLine();

        if (msg.startsWith("Thneeds:"))
        {
          int idxOfNum = msg.indexOf(':') + 1;
          ThneedsInStore = Integer.parseInt(msg.substring(idxOfNum));
//          System.out.println("Current Inventory of Thneeds (" + timeDiff()
//                  + ") = " + ThneedsInStore);
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
