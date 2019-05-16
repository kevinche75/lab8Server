import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SenderMails {

    final static String  password = "";
    final static String login = "";
    final static String host = "smtp.rambler.ru";
    final static String port = "465";
    final static String from = "javaprojectlaba@rambler.ru";
    final static String ENCODING = "UTF-8";

    private Session session;

    public SenderMails(){
        Properties props = System.getProperties();

        props.put("mail.smtp.user", from);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });
    }

    public synchronized String sendMessage(String to, int token) {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject("Registration in Dimasik.System");
            msg.setText("Your token for registration is " + token);
            Transport.send(msg);
            return "TRUE_REGISTER_LOGIN";
        } catch (MessagingException e) {
           return null;
        }
    }
}
