
import  java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;

public class Executor extends Thread {

    private DatagramPacket packet;
    private SQLWorker collection;
    private byte[] buffer;
    private DatagramSocket socket;
    private TokenFactory tokenFactory;
    private RegistrationTokenFactory registrationTokenFactory;
    private SenderMails senderMails;

public Executor(SenderMails senderMails, DatagramPacket packet, SQLWorker collection, byte[] buffer, TokenFactory tokenFactory, RegistrationTokenFactory registrationTokenFactory){
    this.packet = packet;
    this.collection = collection;
    this.buffer = buffer;
    socket = tokenFactory.getSocket();
    this.tokenFactory = tokenFactory;
    this.registrationTokenFactory = registrationTokenFactory;
    this.senderMails = senderMails;
    }

    @Override
    public void run() {
        try (ObjectInputStream receivedstream = new ObjectInputStream(new ByteArrayInputStream(buffer))) {
            analyze((ClientMessage) receivedstream.readObject());
        } catch (ClassNotFoundException e) {
            sendMessage(new ServerMessage(null, MessageType.ERROR));
        } catch (IOException e) {
            sendMessage(new ServerMessage(null, MessageType.ERROR));
        }
    }


    private void analyze(ClientMessage message){
        System.out.println("Сообщение прочитано");
        System.out.println(message.getMessage());
        String login;
        switch (message.getMessage()){
            case ADD:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                try {
                    ServerMessage<Pair<Integer, Three<String, Integer, Alice>>> message1 = new ServerMessage<>(collection.add((Alice) message.getArgument(), login), MessageType.ADD_ROW);
                    tokenFactory.getTokens().forEach((k,v)->{
                        sendToEvery(k, v, message1);
                    });
                } catch (SQLException e) {
                    sendMessage(new ServerMessage(null, MessageType.COMMAND_UNDONE));
                    e.printStackTrace();
                }
                break;
            case IMPORT:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                try {
                    ServerMessage<HashMap<Integer, Three<String, Integer, Alice>>> message1 = new ServerMessage<>(collection.importCollection((String) message.getArgument(), login), MessageType.ADD_ROWS);
                    tokenFactory.getTokens().forEach((k,v)->{
                        sendToEvery(k, v, message1);
                    });
                } catch (SQLException e) {
                    sendMessage(new ServerMessage(null, MessageType.COMMAND_UNDONE));
                    e.printStackTrace();
                } catch (JsonException e){
                    sendMessage(new ServerMessage(null, MessageType.JSON_EXCEPTION));
                }
                break;
            case REMOVE_GREATER:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                try {
                    ServerMessage<ArrayList<Integer>> message1 =  new ServerMessage<ArrayList<Integer>>(collection.remove_greater((Alice) message.getArgument(), login), MessageType.REMOVE_ROWS);
                    tokenFactory.getTokens().forEach((k,v)->{
                        sendToEvery(k, v, message1);
                    });
                } catch (SQLException e) {
                    sendMessage(new ServerMessage(null, MessageType.COMMAND_UNDONE));
                    e.printStackTrace();
                }
                break;
            case REMOVE_ALL:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                try {
                    ServerMessage<ArrayList<Integer>> message1 = new ServerMessage<>(collection.remove_all((Alice) message.getArgument(), login), MessageType.REMOVE_ROWS);
                    tokenFactory.getTokens().forEach((k,v)->{
                        sendToEvery(k, v, message1);
                    });
                } catch (SQLException e) {
                    sendMessage(new ServerMessage(null, MessageType.COMMAND_UNDONE));
                    e.printStackTrace();
                }
                break;
            case REMOVE:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                try {
                    ServerMessage<Integer> message1 = new ServerMessage<Integer>(collection.remove((Integer) message.getArgument()), MessageType.REMOVE_ROW);
                    tokenFactory.getTokens().forEach((k,v)->{
                        sendToEvery(k, v, message1);
                    });
                } catch (SQLException e) {
                    sendMessage(new ServerMessage(null, MessageType.COMMAND_UNDONE));
                    e.printStackTrace();
                }
                break;
            case CHANGE:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                try {
                    ServerMessage<Pair<Integer, Three<String, Integer, Alice>>> message1 = new ServerMessage<>(collection.change((Pair<Integer, Alice>) message.getArgument()), MessageType.CHANGE_ROW);
                    tokenFactory.getTokens().forEach((k,v)->{
                        sendToEvery(k, v, message1);
                    });
                } catch (SQLException e) {
                    sendMessage(new ServerMessage(null, MessageType.COMMAND_UNDONE));
                    e.printStackTrace();
                }
                break;
            case CONNECT:
                sendMessage(new ServerMessage(null, MessageType.CONNECTION));
                return;
            case EXIT:
                login = tokenFactory.checkToken(message.getToken());
                if(login == null){
                    sendMessage(new ServerMessage(null, MessageType.DISCONNECTION));
                    return;
                }
                tokenFactory.exit(message.getToken());
                break;
            case LOGIN:
                Pair<String, String> pair = (Pair<String, String>) message.getArgument();
                System.out.println(getHash(pair.getValue()));
                if(collection.checkLoginAndPassword(pair.getKey(), getHash(pair.getValue()))){
                    int token = tokenFactory.addToken(pair.getKey(), packet.getAddress(), packet.getPort());
                    System.out.println(token);
                    if(token<0){
                        sendMessage(new ServerMessage(null, MessageType.MAX_NUMBER));
                        return;
                    }
                    sendMessage(new ServerMessage<Integer>(new Integer(token), MessageType.TRUE_LOGIN));
                    try {
                        sendMessage(new ServerMessage<HashMap<Integer, Three<String, Integer, Alice>>>(collection.getCollection(), MessageType.COLLECTION));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                sendMessage(new ServerMessage(null, MessageType.FALSE_LOGIN));
                break;
            case REGISTER:
                Pair<String, String> registration = (Pair<String, String>) message.getArgument();
                if(!collection.checkRegistration(registration.getKey())){
                    sendMessage(new ServerMessage(null, MessageType.ALREADY_EXIST));
                    return;
                }
                int token = registrationTokenFactory.addToken(registration.getKey(), getHash(registration.getValue()));
                if(token>0){
                    if(senderMails.sendMessage(registration.getKey(), token)==null){
                        sendMessage(new ServerMessage(null, MessageType.FALSE_REGISTER_LOGIN));
                        return;
                    } else {
                        sendMessage(new ServerMessage(null, MessageType.TRUE_REGISTER_LOGIN));
                        return;
                    }
                } else {
                    sendMessage(new ServerMessage(null, MessageType.TOKEN_IS_ACTIVE));
                    return;
                }
            case TOKEN:
                if(registrationTokenFactory.checkLogin((String)message.getArgument())) {
                    Pair<String, String> forlogin = registrationTokenFactory.checkToken(message.getToken(), (String)message.getArgument());
                    if (forlogin == null) {
                        sendMessage(new ServerMessage(null, MessageType.FALSE_REGISTRATION));
                        return;
                    } else {
                        collection.createUser(forlogin.getKey(), forlogin.getValue());
                        sendMessage(new ServerMessage(null, MessageType.TRUE_REGISTRATION));
                        return;
                    }
                } else sendMessage(new ServerMessage(null, MessageType.TOKEN_UNREACHED));
                return;
            default:
                sendMessage(new ServerMessage(null, MessageType.ERROR));
                break;
        }
    }

    private void sendMessage(ServerMessage message){
        ByteArrayOutputStream bytte = new ByteArrayOutputStream();
        try (ObjectOutputStream sendstream = new ObjectOutputStream(bytte)) {
            sendstream.writeObject(message);
            sendstream.flush();
            DatagramPacket serverpacket = new DatagramPacket(bytte.toByteArray(), bytte.toByteArray().length, packet.getAddress(), packet.getPort());
            socket.send(serverpacket);
            System.out.println("Отправлено");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToEvery(Integer port, InetAddress address, ServerMessage message){
        ByteArrayOutputStream bytte = new ByteArrayOutputStream();
        try (ObjectOutputStream sendstream = new ObjectOutputStream(bytte)) {
            sendstream.writeObject(message);
            sendstream.flush();
            DatagramPacket serverpacket = new DatagramPacket(bytte.toByteArray(), bytte.toByteArray().length, address, port);
            socket.send(serverpacket);
            System.out.println("Отправлено");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getHash(String pass) {
        try {
            byte[] derived = SCrypt.scrypt(pass.getBytes(), "salt".getBytes(), 16, 16, 16, 32);
            return new String(Base64.encode(derived));
        } catch (Exception e) {
            return null;
        }
    }
}
