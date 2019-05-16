
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationTokenFactory {

    private ConcurrentHashMap<Integer, SubToken> tokens;
    private final static int deleteTime = 90000;

    public RegistrationTokenFactory(){
        tokens = new ConcurrentHashMap<>();
    }

    /*
    Если есть логин такой - 0, если нет то токен
     */
    public int addToken(String login, String password){
        if(tokens.entrySet().stream().anyMatch((v)->v.getValue().login.equals(login))) return 0;
        int token = createToken();
        tokens.put(token, new SubToken(login, password));
        return token;
    }

    private int createToken(){
        int token = (int)(Math.random()* +89999) + 10000;
        while(tokens.containsKey(token)){
            token = (int)(Math.random()* +89999) + 10000;
        }
        return token;
    }

    private void deleteTokens(){
        tokens.forEach((k,v) -> {
            if(deleteToken(v)) {
                tokens.remove(k);
            }
        });
    }
    /*
    Проверка токена - логин или null, если нет такого
     */
    public Pair<String, String> checkToken(int token, String login1){
        if(tokens.containsKey(token)){
            Pair<String, String> login = new Pair<>(tokens.get(token).login, tokens.get(token).password);
            tokens.remove(token);
            return login;
        }
        else tokens.entrySet().stream().forEach((v)->{
            if(v.getValue().login.equals(login1)) tokens.remove(v.getKey());
        });
        return null;
    }

    public boolean checkLogin(String login){
        deleteTokens();
        return tokens.entrySet().stream().anyMatch((v)->v.getValue().login.equals(login));
    }

    private boolean deleteToken(SubToken token){
        Date currentdate = new Date();
        return Math.abs(token.date.getTime() - currentdate.getTime()) > deleteTime;
    }

    private class SubToken{

        private String login;
        private Date date;
        private String password;

        private SubToken(String login, String password){
            this.login = login;
            date = new Date();
            this.password = password;
        }
    }
}
