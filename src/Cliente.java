import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente implements Runnable{
	private static final String SERVER_ADDRESS = "127.0.0.1";
	private ClienteSocket clientSocket;
	private Scanner scanner;

	public Cliente()
	{
		scanner = new Scanner(System.in);
	}

	public void start() throws IOException
	{
		try {
			clientSocket = new ClienteSocket(new Socket(SERVER_ADDRESS, Servidor.PORT));
		
			new Thread(this).start();
			messageLoop();
		} finally {
			clientSocket.close();
		}
	}

	@Override
	public void run()
	{
		String msg;
		while((msg = clientSocket.getMessage()) != null)
		System.out.println(msg); //mensagem recebida do servidor
	}

	private void messageLoop() throws IOException
	{
		String msg;
		String username;
		System.out.println("Informe seu username: ");
		username = scanner.nextLine();

		clientSocket.sendMsg(username);
		System.out.println(username + " entrou no chat!");
		
		do{
			msg = scanner.nextLine();
			clientSocket.sendMsg(msg);

		} while(!msg.equalsIgnoreCase("sair"));
		System.out.println("Voce saiu do chat!");
		System.exit(0);
	}

	

	public static void main(String[] args) {
		try {
			Cliente client = new Cliente();
			client.start();
		
		} catch (IOException ex){
			System.out.println("Erro ao iniciar cliente: " + ex.getMessage());
		}
	}

}
