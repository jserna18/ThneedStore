import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class ServerMaster
{
  private ServerSocket serverSocket;
  private LinkedList<ServerWorker> allConnections = new LinkedList<ServerWorker>();
  private ThneedStore store;

  public ServerMaster(int portNumber)
  {
    store = new ThneedStore(1000.00, this);

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
        worker.send("ServerMaster in waitForConnection says hello!");
      }
      catch (IOException e)
      {
        System.err.println("Server error: Failed to connect to client.");
        e.printStackTrace();
      }

    }
  }

  public void cleanConnectionList(ServerWorker worker)
  {
    allConnections.remove(worker);
    System.out.println("cleared worker: " + worker.workerId);
    System.out.println("Number of worker's connected = " + allConnections.size());

  }

  public void broadcast(int thneeds)
  {
    for (ServerWorker workers : allConnections)
    {
      workers.send("Thneeds:" + thneeds+ " Id:" + workers.workerId);
    }
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
