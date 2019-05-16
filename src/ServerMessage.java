import java.io.Serializable;

public class ServerMessage <T>implements Serializable {

    static final long serialVersionUID = 4;
    private T argument;
    private MessageType specialWord;

    public T getArgument() {
        return argument;
    }

    public MessageType getSpecialWord() {
        return specialWord;
    }

    public ServerMessage(T argument, MessageType specialWord){
        this.argument = argument;
        this.specialWord = specialWord;
    }
}