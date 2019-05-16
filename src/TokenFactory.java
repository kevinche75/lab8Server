import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenFactory {

    private ConcurrentHashMap<Integer, SubToken> tokens;
    private DatagramSocket socket;
    private final static long deleteTime = 60000;
    private AtomicInteger numberOfUsers = new AtomicInteger(0);
    /*
    Принимает сокет для отправки сообщений о выходе, а так же запускает новый поток, проверяющий токены
     */
    public TokenFactory(DatagramSocket socket){
        tokens = new ConcurrentHashMap<>();
        this.socket = socket;
        Thread checker = new Thread(this::deleteTokens);
        //checker.setDaemon(true);
        checker.start();
    }

    public DatagramSocket getSocket(){return socket;}

    /*
    Добавить пользователя, если залогинился, возвращает -1 - слишком много пользователей, 0 - уже пользователь есть на сервере
    иначе токен возвращает
     */

    public ConcurrentHashMap<Integer, InetAddress> getTokens(){
        ConcurrentHashMap<Integer, InetAddress> map = new ConcurrentHashMap<>();
        tokens.forEach((k,v)->{
            map.put(v.port,v.address);
        });
        return map;
    }

    public int addToken(String login, InetAddress address, int port){
        if(numberOfUsers.get() > 4) return -1;
        if(tokens.entrySet().stream().anyMatch((v)->v.getValue().login.equals(login))) return 0;
        int token = createToken();
        sendToEvery(login, MessageType.ADD_USER);
        tokens.put(token, new SubToken(login, address, port));
        numberOfUsers.incrementAndGet();
        return token;
    }

    private int createToken(){
        int token = (int)(Math.random()* +89999) + 10000;
        while(tokens.containsKey(token)){
            token = (int)(Math.random()* +89999) + 10000;
        }
        return token;
    }

    /*
    При выходе пользователя
    */
    public void exit(int token){
        if(tokens.containsKey(token)) {
            String login = tokens.get(token).login;
            tokens.remove(token);
            numberOfUsers.decrementAndGet();
            sendToEvery(login , MessageType.USERS);
            System.out.println("===\nПользователь " + login + " выешел");
        }
    }

    /*
    Возвращает логин, если если есть такой токен, иначе null
     */
    public String checkToken(int token){
        if(tokens.containsKey(token)) {
            tokens.get(token).setDate();
            return tokens.get(token).getLogin();
        } else return null;
    }

    private void deleteTokens(){
        while(true) {
            StringBuilder logins = new StringBuilder();
            tokens.forEach((k, v) -> {
                if (deleteToken(v)) {
                    logins.append(v.login).append("  ");
                    numberOfUsers.decrementAndGet();
                    sendDisconnect(v.port, v.address, null, MessageType.DISCONNECTION);
                    tokens.remove(k);
                }
            });
            if (logins.toString().length() > 0) {
                sendToEvery(logins.toString(), MessageType.USERS);
            }
        }
    }

    public void sendToEvery(String message, MessageType specialWord){
        tokens.forEach((k, v) -> sendDisconnect(v.port, v.address, message, specialWord));
    }


    private boolean deleteToken(SubToken token){
        Date currentdate = new Date();
        return Math.abs(token.date.getTime() - currentdate.getTime()) > deleteTime;
    }

    private void sendDisconnect(Integer port, InetAddress address, String message, MessageType specialWord){
        ByteArrayOutputStream bytte = new ByteArrayOutputStream();
        try (ObjectOutputStream sendstream = new ObjectOutputStream(bytte)) {
            sendstream.writeObject(new ServerMessage<String>(message, specialWord));
            sendstream.flush();
            DatagramPacket packet = new DatagramPacket(bytte.toByteArray(), bytte.toByteArray().length, address, port);
            socket.send(packet);
            System.out.println("===\nОтправлено");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SubToken{

        private String login;
        private Date date;
        public InetAddress address;
        public int port;

        private SubToken(String login, InetAddress address, int port){
            this.login = login;
            date = new Date();
            this.address = address;
            this.port = port;
        }

        private void setDate(){
            date = new Date();
        }

        public InetAddress getAddress(){
            return address;
        }

        public int getPort(){
            return port;
        }

        private String getLogin(){
            return login;
        }
    }
}
