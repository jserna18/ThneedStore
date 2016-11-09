import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

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

    //closeAll();
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
//      System.out.println("Thneeds in Inventory = " + ThneedsInStore);
//      System.out.println("Enter Command (buy: # | sell: #):");
      String cmd = inputStream.nextLine();
      if (cmd == null) continue;
      if (cmd.length() < 1) continue;
      char c = cmd.charAt(0);

      if(c == 'b')
      {
        String sub = cmd.substring(5);
        int number = Integer.parseInt(sub.substring(0, sub.indexOf(' ')));
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

      else if (c == 'q') break;

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
//        clientSocket.close();
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

      System.out.println("Client Closing");
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
          System.out.println("Current Inventory of Thneeds (" + timeDiff()
                  + ") = " + ThneedsInStore);
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
