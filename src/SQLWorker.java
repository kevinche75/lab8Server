
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArrayList;

public class SQLWorker {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/lab7";
    private static final String DB_Driver = "org.postgresql.Driver";
    private static final String USER = "postgres";
    private static final String PASS = "sa";
    private Connection connection;

    public  SQLWorker() throws ClassNotFoundException, SQLException {
        System.out.println("Testing connection to PostgreSQL JDBC");
            Class.forName(DB_Driver);
            System.out.println("PostgreSQL JDBC Driver successfully connected");
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public  boolean checkLoginAndPassword(String login, String password){
        try {
            PreparedStatement statement = connection.prepareStatement("select count(*) from main.users as t where t.login = ? and t.password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet set = statement.executeQuery();
            set.next();
            if (set.getInt(1) == 1){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkRegistration(String login){
        try {
            PreparedStatement statement = connection.prepareStatement("select count(*) from main.users as t where t.login = ?");
            statement.setString(1, login);
            ResultSet set = statement.executeQuery();
            set.next();
            if (set.getInt(1)==0){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createUser(String login, String password){
        try {
            PreparedStatement statement = connection.prepareStatement("insert into main.users (login, password) values (?, ?)");
            statement.setString(1, login);
            statement.setString(2, password);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Pair<Integer, Three<String, Integer, Alice>> add(Alice argument, String login) throws SQLException {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into main.collection (user_id, name, politeness, size, x, y, date)" +
                    " values ((select id from main.users where login = ?)" +
                    ", ?, ?, ?, ?, ?, ?)");
            statement.setString(1, login);
            statement.setString(2, argument.getName());
            statement.setString(3, argument.getPoliteness().toString());
            statement.setInt(4, argument.getSize());
            statement.setInt(5, argument.getX());
            statement.setInt(6, argument.getY());
            statement.setString(7, argument.getDate().toString());
            statement.execute();
            statement = connection.prepareStatement(" insert into main.cups (alice_id, fullness, teatype)" +
                            " values ((select max(id) from main.collection group by user_id" +
                            " having user_id = (select id from main.users where login = ?)), ?, ?)");
        statement.setString(1, login);
        statement.setInt(2, argument.getfullness());
        statement.setString(3, argument.getTeaType().name());
        statement.execute();
            PreparedStatement statement1 = connection.prepareStatement(
                    "select c.id, login, u.id from main.users as u inner join main.collection as c on u.id = c.user_id where c.id = (select max(id) from main.collection group by user_id" +
                            " having user_id = (select id from main.users where login = ?))");
            statement1.setString(1,login);
            ResultSet set = statement1.executeQuery();
            set.next();
            return new Pair<Integer, Three<String, Integer, Alice>>(new Integer(set.getInt(1)),
                    new Three<String, Integer, Alice>(set.getString(2), new Integer(set.getInt(3)), argument));
    }

    public HashMap<Integer, Three<String, Integer, Alice>> importCollection (String rawJson, String login) throws SQLException, JsonException {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Alice>>(){}.getType();
            ArrayList <Alice> collection = gson.fromJson(rawJson, type);
            HashMap<Integer, Three<String, Integer, Alice>> map = new HashMap<>();
            for(Alice argument : collection){
                Pair<Integer, Three<String, Integer, Alice>> get = add(argument, login);
                map.put(get.getKey(), get.getValue());
            }
            return map;
    }

    public ArrayList<Integer> remove_greater(Alice argument, String login) throws SQLException {

            PreparedStatement statement = connection.prepareStatement("select id from main.collection" +
                    " where size > ? and user_id = (select id from main.users where login = ?)");
            statement.setInt(1, argument.getSize());
            statement.setString(2, login);
            ResultSet set = statement.executeQuery();
            ArrayList<Integer> list = new ArrayList<>();
            while (set.next()){
                list.add(new Integer(set.getInt(1)));
            }
            PreparedStatement statement1 = connection.prepareStatement("delete from main.collection" +
                    " where size > ? and user_id = (select id from main.users where login = ?)");
            statement1.setInt(1, argument.getSize());
            statement1.setString(2, login);
            statement1.execute();
            return list;
    }

    public ArrayList<Integer> remove_all(Alice argument, String login) throws SQLException {
            PreparedStatement statement = connection.prepareStatement("select id from main.collection inner join main.cups on main.collection.id = main.cups.alice_id" +
                    " where user_id = (select id from main.users where login = ?) and name = ?" +
                    " and politeness = ?" +
                    " and size = ?" +
                    " and x = ?" +
                    " and y = ?" +
                    " and fullness = ?" +
                    " and teatype = ?" +
                    "");
            statement.setString(1, login);
            statement.setString(2, argument.getName());
            statement.setString(3, argument.getPoliteness().toString());
            statement.setInt(4, argument.getSize());
            statement.setInt(5, argument.getX());
            statement.setInt(6, argument.getY());
            statement.setInt(7, argument.getfullness());
            statement.setString(8, argument.getTeaType().name());
            ResultSet set = statement.executeQuery();
            ArrayList<Integer> list = new ArrayList<>();
            while (set.next()){
                list.add(new Integer(set.getInt(1)));
            }
            for(int k = 0; k < list.size(); k++) {
                PreparedStatement statement1 = connection.prepareStatement("delete from main.collection" +
                        " where id = ?");
                statement1.setInt(1,list.get(k));
                statement1.execute();
                PreparedStatement statement2 = connection.prepareStatement("delete from main.cups" +
                        " where alice_id = ?");
                statement1.setInt(1,list.get(k));
                statement1.execute();
            }
            return list;
    }

    public Integer remove(Integer i) throws SQLException {
//            PreparedStatement statement = connection.prepareStatement("select id from main.collection" +
//                    " where user_id = (select id from main.users where login = ?) and name = ?" +
//                    " and politeness = ?" +
//                    " and size = ?" +
//                    " and x = ?" +
//                    " and y = ?" +
//                    " and fullness = ?" +
//                    " and teatype = ?" +
//                    " inner join main.caps on main.collection.id = main.cups.alice_id");
//            statement.setString(1, login);
//            statement.setString(2, argument.getName());
//            statement.setString(3, argument.getPoliteness().toString());
//            statement.setInt(4, argument.getSize());
//            statement.setInt(5, argument.getX());
//            statement.setInt(6, argument.getY());
//            statement.setInt(7, argument.getfullness());
//            statement.setString(8, argument.getTeaType().name());
//            ResultSet set = statement.executeQuery();
//            set.next();
//            int i = set.getInt(1);
            PreparedStatement statement1 = connection.prepareStatement("delete from main.collection where id = ?");
            statement1.setInt(1, i);
            statement1.execute();
            PreparedStatement statement2 = connection.prepareStatement("delete from main.cups where alice_id = ?");
            statement1.setInt(1, i);
            statement1.execute();
            return i;
    }

    private Alice getAlice(String name, String politeness, int size, int x, int y, String date, int fullness, String teaType){
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date.trim());
        Politeness politeness1;
        if(politeness.equals("POLITE")) politeness1 = Politeness.POLITE; else politeness1 = Politeness.RUDE;
        TeaType teaType1;
        switch (teaType){
            case "GREEN":
                teaType1 = TeaType.GREEN;
                break;
            case "RED":
                teaType1 = TeaType.RED;
                break;
            case "BLACK":
                teaType1 = TeaType.BLACK;
                break;
                default:
                    teaType1 = TeaType.BLACK;
        }
        return new Alice(name, politeness1, x, y, size, fullness, teaType1, zonedDateTime);
    }

    public HashMap<Integer, Three<String, Integer, Alice>> getCollection() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select c.id, login, u.id, name, politeness, size, x, y, date , fullness, teatype" +
                "  from main.users as u inner join main.collection as c on u.id = c.user_id inner join main.cups as cup on c.id = cup.alice_id");
        ResultSet set = statement.executeQuery();
        HashMap<Integer, Three<String, Integer, Alice>> map = new HashMap<>();
        while (set.next()){
            map.put(new Integer(set.getInt(1)), new Three<>(set.getString(2), new Integer(set.getInt(3)), getAlice(
                    set.getString(4), set.getString(5), set.getInt(6), set.getInt(7), set.getInt(8),
                    set.getString(9), set.getInt(10), set.getString(11)
            )));
        }
        return map;
    }

    public Pair<Integer, Three<String, Integer, Alice>> change(Pair<Integer, Alice> pair) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("update main.collection set " +
                "name = ?, " +
                "politeness = ?, " +
                "size = ?, " +
                "x = ?, " +
                "y = ?, " +
                "date = ?" +
                " where id = ?;");
        statement.setString(1, pair.getValue().getName());
        statement.setString(2, pair.getValue().getPoliteness().toString());
        statement.setInt(3, pair.getValue().getSize());
        statement.setInt(4, pair.getValue().getX());
        statement.setInt(5, pair.getValue().getY());
        statement.setString(6, pair.getValue().getDate().toString());
        statement.setInt(7,pair.getKey());
        statement.execute();
        statement = connection.prepareStatement("update main.cups set " +
                "fullness = ?, " +
                "teatype = ?" +
                " where alice_id = ?;");
        statement.setInt(1, pair.getValue().getfullness());
        statement.setString(2, pair.getValue().getTeaType().name());
        statement.setInt(3, pair.getKey());
        statement.execute();
        statement = connection.prepareStatement("select main.users.id, login from main.users" +
                " inner join main.collection on main.users.id = main.collection.user_id" +
                " where main.collection.id = ? ");
        statement.setInt(1, pair.getKey());
        ResultSet set = statement.executeQuery();
        set.next();
        return new Pair<>(pair.getKey(), new Three<>(set.getString(2),new Integer(set.getInt(1)), pair.getValue()));
    }
}
