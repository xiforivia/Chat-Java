import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Servidor {

	enum State {
		CHAT, 
		QUIZ,
		BREAK
	}

	public static final int PORT = 4000;
	private ServerSocket serverSocket;
	private final List<ClienteSocket> clientes = new LinkedList<>();
	private final List<ClienteSocket> clientesRespondidos = new LinkedList<>();

	private State serverState = State.CHAT;

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

	private void chat(ClienteSocket clientSocket, String username){
		String msg;
		try {
			while((msg = clientSocket.getMessage()) != null)
			{
				if("sair".equalsIgnoreCase(msg))
				{
					sendMsgToAll(clientSocket, String.format("%s saiu do chat! %s", username, getState().toString()));
					System.out.printf("Cliente %s desconectou\n", clientSocket.getRemoteSocketAddress());
					return;
				}
				if("iniciar quiz".equalsIgnoreCase(msg))
				{
					sendMsgToAll(String.format("Iniciando QUIZ"));
					System.out.println("Mudando para o estado QUIZ");
					setState(State.QUIZ);
					quiz(clientSocket, username);
					break;
				}

				System.out.printf("Mensagem recebida do cliente %s: %s\n", clientSocket.getRemoteSocketAddress(), msg);
				msg = username + ": " + msg;
				sendMsgToAll(clientSocket, msg);
			}
		} finally {
			clientSocket.close();
		}
	}

	private void quiz(ClienteSocket clientSocket, String username){
		// LOGICA DO QUIZ
		String msg = null;
		try {
			System.out.printf("Antes de mandar pergunta");
			sendMsgToAll(getQuestion()); //mandar a pergunta para tds
			System.out.printf("Depois de mandar pergunta");
			while(!haveAllAnswers() && (msg = clientSocket.getMessage()) != null) {
				System.out.println(username + "entrou no while");
				System.out.printf("Ainda não respondeu");
				if(clientSocket.getRespondeu() == true)
				{
					sendMsg(clientSocket, "Voce ja respondeu!");
				} else {
					clientSocket.setRespondeu(true);
					clientesRespondidos.add(clientSocket);
					if(msg.equals(getAnswer())) 
						clientSocket.acertou();
				}
			}	
			sendMsgToAll(String.format("A resposta correta era %s", getAnswer()));
			setState(State.BREAK);
			intervalo(clientSocket, username);
		}
		finally {
			clientSocket.close();
		}
	}

	private void intervalo(ClienteSocket clientSocket, String username){
		String msg;
		try {
			sendMsgToAll(String.format("Digite 'continuar' quando quiser ir para a proxima pergunta!"));
			while((msg = clientSocket.getMessage()) != null)
			{
				if("sair".equalsIgnoreCase(msg))
				{
					sendMsgToAll(clientSocket, String.format("%s saiu do chat! %s", username, getState().toString()));
					System.out.printf("Cliente %s desconectou\n", clientSocket.getRemoteSocketAddress());
					return;
				}

				if("continuar".equalsIgnoreCase(msg))
				{
					sendMsgToAll(String.format("Proxima pergunta"));
					System.out.println("Mudando para o estado QUIZ");
					setState(State.QUIZ);
					break;
				}

				System.out.printf("Mensagem recebida do cliente %s: %s\n", clientSocket.getRemoteSocketAddress(), msg);
				msg = username + ": " + msg;
				sendMsgToAll(clientSocket, msg);
			}
		} finally {
			clientSocket.close();
		}
	}

	private void clientMessageLoop(ClienteSocket clientSocket)
	{ 
		String msg;
		String username;
		username = clientSocket.getUsername();
		msg = username + " entrou no chat!";
		sendMsgToAll(clientSocket, msg);
		System.out.println(username);
		// chat(clientSocket, username);
		if(getState() == State.CHAT){
			chat(clientSocket, username);
		} 
		if (getState() == State.QUIZ){ //quando o server vai para o modo quiz
			quiz(clientSocket, username);
		}
		if(getState() == State.BREAK){
			intervalo(clientSocket, username);
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


	private void sendMsg(ClienteSocket sender, String msg) //Servidor encaminha a mensagem somente para o cliente especifico
	{
		sender.sendMsg(msg);
	}

	private void sendMsgToAll(String msg) //Servidor encaminha a mensagem para todos
	{
		Iterator<ClienteSocket> iterator = clientes.iterator();
		while(iterator.hasNext())
		{
			ClienteSocket clientSocket = iterator.next();
			if(!clientSocket.sendMsg(msg))
			{
				iterator.remove(); //quando a conexao de um cliente cair o servidor vai remover esse cliente
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

	public State getState(){
		return this.serverState;
	}
	public void setState(State state){
		this.serverState = state;
	}

	public String getQuestion(){
		return "Pergunta";
	}

	public String getAnswer(){
		return "a";
	}

	public boolean haveAllAnswers(){
		if(clientesRespondidos.equals(clientes)) return true;
		return false; 
	}
}
