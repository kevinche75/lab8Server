import javax.mail.MessagingException;
import java.net.SocketException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Server {

    public static void main(String[] args) throws MessagingException, SQLException, ClassNotFoundException {
        ServerReceiver receiver = null;
        try {
            receiver = new ServerReceiver();
            System.out.println("Сервер запущен");
            receiver.work();
        } catch (SocketException e) {
            System.out.println("Порт занят");
        } catch(SQLException e){
            System.out.println("SQLServer не найден");
        } catch (ClassNotFoundException e){
            System.out.println("Дравйвер не найден");
        }
    }
}
