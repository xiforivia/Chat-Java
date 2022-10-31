import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

	public static final int PORT = 4000;
	private ServerSocket serverSocket;

	public void start() throws IOException //inicializa o server socket
	{
		serverSocket = new ServerSocket(PORT);
		System.out.println("Servidor iniciado na porta: " + PORT);
		clientConnectionLoop();
	}

	private void clientConnectionLoop() throws IOException
	{
		while(true)
		{
			Socket clientSocket = serverSocket.accept();
			System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " conectou");
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String msg = in.readLine(); //sabe que a mensagem está completa quando vê uma quebra de linha
			System.out.println("Mensagem recebida do cliente " + clientSocket.getRemoteSocketAddress() + ": " + msg);
		}
	}

	public static void main(String[] args) {
		try {
			Servidor server = new Servidor();
			server.start();		
			
		} catch (IOException ex) {
			System.out.println("Erro ao iniciar o servidor: " + ex.getMessage());
		}

		System.out.println("Servidor finalizado");
	}

}
