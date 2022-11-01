import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Servidor {

	public static final int PORT = 4000;
	private ServerSocket serverSocket;
	private final List<ClienteSocket> clientes = new LinkedList<>();

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
			ClienteSocket clientSocket = new ClienteSocket(serverSocket.accept());
			clientes.add(clientSocket); //adiciona cliente na lista de clientes
			// Socket clientSocket = serverSocket.accept();
			// System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " conectou");
			// BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //recebe mensagem do cliente
			// String msg = in.readLine(); //sabe que a mensagem está completa quando vê uma quebra de linha
			// new Thread(() -> in.readLine()).start();
			new Thread(() -> clientMessageLoop(clientSocket)).start();

			// System.out.println("Mensagem recebida do cliente " + clientSocket.getRemoteSocketAddress() + ": " + msg);
		}
	}

	private void clientMessageLoop(ClienteSocket clientSocket)
	{
		String msg;
		String username;
		username = clientSocket.getUsername();
		msg = username + " entrou no chat!";
		sendMsgToAll(clientSocket, msg);
		try {
		while((msg = clientSocket.getMessage()) != null)
			{
				if("sair".equalsIgnoreCase(msg))
				{
					sendMsgToAll(clientSocket, username + " saiu do chat!");
					System.out.printf("Cliente %s desconectou\n", clientSocket.getRemoteSocketAddress());
					return;
				}

				System.out.printf("Mensagem recebida do cliente %s: %s\n", clientSocket.getRemoteSocketAddress(), msg);
				msg = username + ": " + msg;
				sendMsgToAll(clientSocket, msg);
			}
		} finally {
			clientSocket.close();
		}
	}
	
	private void sendMsgToAll(ClienteSocket sender, String msg) //Servidor encaminha a mensagem para todos
	{
		Iterator<ClienteSocket> iterator = clientes.iterator();
		while(iterator.hasNext())
		{
			ClienteSocket clientSocket = iterator.next();
			if(!sender.equals(clientSocket)) //se quem enviou nao é igual ao cliente atual do loop (para nao enviar a mensagem para quem mandou)
			{
				if(!clientSocket.sendMsg(msg))
				{
					iterator.remove(); //quando a conexao de um cliente cair o servidor vai remover esse cliente
				}
			}
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
