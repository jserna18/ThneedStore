import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * ServerMaster dedicates a given port. A server socket is created when a new Client
 * is received on the port.
 */
public class ServerMaster
{
  private ServerSocket serverSocket;
  private LinkedList<ServerWorker> allConnections = new LinkedList<ServerWorker>();
  private ThneedStore store;
  private long startNanoSec;

  public ServerMaster(int portNumber)
  {
    store = new ThneedStore(1000.00, this);
    startNanoSec = System.nanoTime();


    try
    {
      serverSocket = new ServerSocket(portNumber);
    }
    catch (IOException e)
    {
      System.err.println("Server error: Opening socket failed.");
      e.printStackTrace();
      System.exit(-1);
    }

    waitForConnection(portNumber);
  }

  /**
   * Accepts new clients on the current open port. Creates a ServerWorker for communication
   * from Client to ServerMaster.
   * @param port
   */
  public void waitForConnection(int port)
  {
    String host = "";
    try
    {
      host = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e)
    {
      e.printStackTrace();
    }
    while (true)
    {
      System.out.println("ServerMaster("+host+"): waiting for Connection on port: "+port);
      try
      {
        Socket client = serverSocket.accept();
        ServerWorker worker = new ServerWorker(client, this, store);
        worker.start();
        System.out.println("ServerMaster: *********** new Connection");
        allConnections.add(worker);
        worker.workerId = allConnections.size();
      }
      catch (IOException e)
      {
        System.err.println("Server error: Failed to connect to client.");
        e.printStackTrace();
      }

    }
  }

  /**
   * removes the worker from the ServerWorker ArrayList
   * @param worker
   */
  public void cleanConnectionList(ServerWorker worker)
  {
    allConnections.remove(worker);
    System.out.println("cleared worker: " + worker.workerId);
    System.out.println("Number of worker's connected = " + allConnections.size());

  }

  /**
   * sends the current Thneeds inventory to all clients
   * @param thneeds
   */
  public void broadcast(int thneeds, double treasury, int id)
  {
    for (ServerWorker workers : allConnections)
    {
      workers.send("time(" +timeDiff() + "): inventory=" + thneeds +
                   " : treasury=" + String.format("%.2f", treasury));
    }
  }

  private String timeDiff()
  {
    long namoSecDiff = System.nanoTime() - startNanoSec;
    double secDiff = (double) namoSecDiff / 1000000000.0;
    return String.format("%.3f", secDiff);

  }

  public static void main(String args[])
  {
    //Valid port numbers are Port numbers are 1024 through 65535.
    //  ports under 1024 are reserved for system services http, ftp, etc.
    int port = 5555; //default
    if (args.length > 0)
      try
      {
        port = Integer.parseInt(args[0]);
        if ((port < 1024) || (port > 65535)) throw new Exception();
      }
      catch (Exception e)
      {
        System.out.println("Usage: ServerMaster portNumber");
        System.exit(0);
      }

    new ServerMaster(port);
  }
}
