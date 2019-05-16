import java.io.Serializable;
import java.util.Objects;

public abstract class Human implements Serializable {
    private String name ;
    private int x;
    static final long serialVersionUID = 2;
    public abstract void sayPhrase();
    
    public String getName (){
        return this.name;
    };
    public void setX (int x){
        this.x = x;
    };
    public int getX (){
        return this.x;
    };

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Human human = (Human) obj;
        return getX() == human.getX() &&
                getName().equals(human.getName());
    }

    @Override
    public String toString() {
        return "Класс: Human\n" +
                "Имя: name=" + name +
                "\nМестоположение: x=" + x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, x);
    }
}
