import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {
	private static final String SERVER_ADDRESS = "127.0.0.1";
	private Socket clientSocket;
	private Scanner scanner;
	private PrintWriter out;

	public Cliente()
	{
		scanner = new Scanner(System.in);
	}

	public void start() throws IOException
	{
		clientSocket = new Socket(SERVER_ADDRESS, Servidor.PORT);
		this.out = new PrintWriter(clientSocket.getOutputStream(), true);
		
		System.out.println("Cliente conectado ao servidor em " + SERVER_ADDRESS + ":" + Servidor.PORT);
		messageLoop();
	}

	private void messageLoop() throws IOException
	{
		String msg;
		do{
			System.out.println("Digite uma mensagem (ou sair para finalizar): ");
			msg = scanner.nextLine();
			out.println(msg); //mandando mensagem pro servidor
			out.flush();
		} while(!msg.equalsIgnoreCase("sair"));
	}

	public static void main(String[] args) {
		try {
			Cliente client = new Cliente();
			client.start();
		
		} catch (IOException ex){
			System.out.println("Erro ao iniciar cliente: " + ex.getMessage());
		}
		System.out.println("Cliente finalizado!");
	}
}
