import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

//implements ChangeableCondition, FieldSeller, SayPhraseSeller, DrinkableTea,

public class Alice extends Human implements Comparable<Alice>, Serializable {
    private Politeness politeness;
    private CupOfTea cup;
    private ZonedDateTime date;
    private int size;
    static final long serialVersionUID = 2;
    private int y;

    public int getY() {
        return y;
    }
    
    public ZonedDateTime getDate() {
        return date;
    }

    public int getSize() {
        return size;
    }

    public int getfullness(){
        return cup.getFullness();
    }
    
    public TeaType getTeaType(){
        return cup.getTeaType();
    }
    
    public Politeness getPoliteness() {
        return this.politeness;
    }
    
    @Override
    public void sayPhrase() {
        System.out.println(getName() + ": \"Ах ты, старая карга\"");
    }

    public Alice(String name, Politeness politeness, int x, int y, int size, int fullness,TeaType teaType, ZonedDateTime date) {
        setName(name);
        setX(x);
        this.y = y;
        this.politeness = politeness;
        cup = new CupOfTea(teaType, fullness);
        this.date = date;
        this.size = size;
    }
    
    public Alice(String name, Politeness politeness, int x,int y, int size, int fullness,TeaType teaType) {
        setName(name);
        setX(x);
        this.y = y;
        this.politeness = politeness;
        cup = new CupOfTea(teaType, fullness);
        date = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alice)) return false;
        if (!super.equals(o)) return false;
        Alice alice = (Alice) o;
        return getSize() == alice.getSize() &&
                getPoliteness() == alice.getPoliteness() &&
                Objects.equals(getfullness(), alice.getfullness()) &&
                Objects.equals(getDate(), alice.getDate()) &&
                Objects.equals(getName(), alice.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPoliteness(), getX(), getY(), getfullness(),getTeaType(), getDate(), getSize(), getName());
    }

    @Override
    public String toString() {
        return "\nКласс: Alice\n" + 
                "Имя: " + getName() + 
                "\nВежливость: politeness = " + politeness + 
                "\nDate: " + date+
                "\nSize: "+ size + 
                "\nHashcode: " + Integer.toHexString(hashCode())+ 
                "\nX: "+getX()+"\nY: "+getY()+
                "\ncap: " + 
                "\n\tTeaType: " + cup.getTeaType() +
                "\n\tFullness :"+ cup.getFullness()+'\n';
    }

    @Override
    public int compareTo(Alice alice) {
        return getSize()-alice.getSize();
    }
    
    private static class CupOfTea implements Serializable{
        public CupOfTea() {
           teaType = TeaType.GREEN;
            //System.out.println(nameOfUser + ": Беру я чаёк, наливаю в стакан, щас мне будет легко");
        }
        public CupOfTea(int par){
            teaType = TeaType.GREEN;
            fullness = par;
        }

        public CupOfTea(TeaType teaType, int fullness) {
            this.teaType = teaType;
            this.fullness = fullness;
        }

        private int getFullness(){
            return fullness;
        }

        public TeaType getTeaType() {
            return teaType;
        }
        
        private TeaType teaType;
        private int fullness;
    }
}
