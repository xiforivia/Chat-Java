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

	private int contador;

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

			new Thread(() -> clientMessageLoop(clientSocket)).start();
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

		while(true) {
            if((msg = clientSocket.getMessage()) != null){
				if(msg.equalsIgnoreCase("sair")){
					sendMsgToAll(clientSocket, String.format("%s saiu do chat!", username));
					System.out.printf("Cliente %s desconectou\n", clientSocket.getRemoteSocketAddress());
					clientes.remove(clientSocket);
					break;
				}

                if(getState() == State.CHAT) {
                    System.out.println("Recebeu uma mensagem no estado Chat");
					sendMsgToAll(clientSocket, username + ": " + msg);
                    if(msg.equalsIgnoreCase("Iniciar Quiz")) startQuiz();
                }
                else if(getState() == State.QUIZ) {
					contador += 1;
                    System.out.println("Recebeu uma mensagem no estado Quiz");
					verifyAnswer(clientSocket, msg);
					if(verifyEnd())
						startChat();
				}
                else if(getState() == State.BREAK) {
                    System.out.println("Recebeu uma mensagem no estado Break");
					sendMsgToAll(clientSocket, username + ": " + msg);
                    if(msg.equalsIgnoreCase("Continuar")) startQuiz();
                }
            }
        }
	}
	
	private void startBreak(){
		setState(State.BREAK);
		sendMsgToAll("Indo para o Intervalo! Digite 'continuar' quando quiser ir para a proxima pergunta!");
	}

	private void startQuiz(){
		setState(State.QUIZ);
		sendMsgToAll(getQuestion());
	}
	
	private void startChat(){
		setState(State.CHAT);
	}

	private void verifyAnswer (ClienteSocket clientSocket, String msg) {
		//Verifica se acertou resposta
		//Verifica se o player ja respondeu antes
		System.out.printf("respondeu");
		if(clientesRespondidos.contains(clientSocket))
		{
			sendMsg(clientSocket, "Voce ja respondeu!");
		} else {
			clientesRespondidos.add(clientSocket);
			if(msg.equals(getAnswer())) clientSocket.acertou();
		}
		if(haveAllAnswers()){
			sendMsgToAll(String.format("A resposta correta era: \"%s\"", getAnswer()));
			clientesRespondidos.clear();
			startBreak();
		}
	}	

	private boolean verifyEnd(){
		if(contador == 10){
			// mostrar ranking
			
			return true;
		}
		return false;
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
		for (ClienteSocket c : clientes)
			if(!clientesRespondidos.contains(c)) return false;

		return true;
	}
}
