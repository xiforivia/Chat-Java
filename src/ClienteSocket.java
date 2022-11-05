import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.io.PrintWriter;

public class ClienteSocket {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    private int acertos;

    public ClienteSocket(Socket socket) throws IOException
    {
        this.socket = socket;
        System.out.println("Cliente " + socket.getRemoteSocketAddress() + " conectou");
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //recebe mensagem do cliente
        this.out = new PrintWriter(socket.getOutputStream(), true); //envia mensagens para o servidor
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return socket.getRemoteSocketAddress();
    }

    public void close()
    {
        try {
            in.close();
            out.close();
            socket.close();
        } catch(IOException e)
        {
            System.out.println("Erro ao fechar socket: " + e.getMessage());
        }
    }

    public String getUsername()
    {
        try{
        return in.readLine();
        } catch(IOException e){
            return null;
        }

    }

    public String getMessage()
    {
        try {
            return in.readLine();
        } catch(IOException e){
            return null;
        }
    }

    public boolean sendMsg(String msg)
    {
        out.println(msg);
        return !out.checkError();
    }

    public void acertou(){
        acertos++;
    }

}
