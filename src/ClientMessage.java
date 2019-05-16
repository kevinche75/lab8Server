import java.io.Serializable;

public class ClientMessage <T> implements Serializable {
    private MessageType message;
    private int token;
    private T argument;
    static final long serialVersionUID = 1;

    public ClientMessage(MessageType message, T argument, int token) {
        this.message = message;
        this.token = token;
        this.argument = argument;
    }

    public int getToken() {
        return token;
    }

    public MessageType getMessage() {
        return message;
    }

    public T getArgument() {
        return argument;
    }
}
