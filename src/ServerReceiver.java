import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerReceiver {

    private final static int port = 1488;
    private DatagramSocket socket;
    private SQLWorker collection;
    private  byte[] buffer;
    private TokenFactory tokenFactory;
    private RegistrationTokenFactory registrationTokenFactory;
    private SenderMails senderMails;

    public ServerReceiver() throws SocketException, SQLException, ClassNotFoundException {
        socket = new DatagramSocket(port);
        collection = new SQLWorker();
        tokenFactory = new TokenFactory(socket);
        registrationTokenFactory = new RegistrationTokenFactory();
        senderMails = new SenderMails();
    }

    private void shootDown(){
        Runtime.getRuntime().addShutdownHook(new Thread(()->tokenFactory.sendToEvery(null, MessageType.DISCONNECTION)));
    }

    public void work() {
        shootDown();
        while (true){
            buffer = new byte[8192];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(incoming);
                System.out.println("Получено сообщение");
                new Executor(senderMails, incoming, collection, buffer, tokenFactory, registrationTokenFactory).start();
            } catch (IOException e) {
                System.out.println("===\nОшбика получения пакета");
            }
        }
    }
}
