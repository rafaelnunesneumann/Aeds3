import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

class Game {

    private int id, lan_support, required_age;
    private String name;
    private Date date;
    private String[] developer, publisher, platforms, categories, genres;
    
    public Game() {
        String[] s = {""};
        setID(-1);
        setLanSupport(0);
        setRequiredAge(0);
        setName("");
        LocalDate local = LocalDate.now();
        setDate(Date.from(local.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        setDeveloper(s);
        setPublisher(s);
        setPlatforms(s);
        setCategories(s);
        setGenres(s);
    }

    public Game(int id, int lan_support, int required_age, String name, Date date, String[] developer, String[] publisher, String[] platforms, String[] categories, String[] genres) {
        setID(id);
        setLanSupport(lan_support);
        setRequiredAge(required_age);
        setName(name);
        setDate(date);
        setDeveloper(developer);
        setPublisher(publisher);
        setPlatforms(platforms);
        setCategories(categories);
        setGenres(genres);
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void setLanSupport(int lan_support) {
        this.lan_support = lan_support;
    }

    public int getLanSupport() {
        return lan_support;
    }

    public void setRequiredAge(int required_age) {
        this.required_age = required_age;
    }

    public int getRequiredAge() {
        return required_age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDeveloper(String[] developer) {
        this.developer = developer;
    }

    public String getDeveloper() {
        String s = "";
        String s2 = "";
        for (int i = 0; i < developer.length; i++) {
            s+=developer[i] + ",";
        }
        for (int i = 0; i < s.length() - 1; i++) {
            s2+=s.charAt(i);
        }
        return s2;
    }

    public void setPublisher(String[] publisher) {
        this.publisher = publisher;
    }

    public String getPublisher() {
        String s = "";
        String s2 = "";
        for (int i = 0; i < publisher.length; i++) {
            s+=publisher[i] + ",";
        }
        for (int i = 0; i < s.length() - 1; i++) {
            s2+=s.charAt(i);
        }
        return s2;
    }

    public void setPlatforms(String[] platforms) {
        this.platforms = platforms;
    }

    public String getPlatforms() {
        String s = "";
        String s2 = "";
        for (int i = 0; i < platforms.length; i++) {
            s+=platforms[i] + ",";
        }
        for (int i = 0; i < s.length() - 1; i++) {
            s2+=s.charAt(i);
        }
        return s2;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getCategories() {
        String s = "";
        String s2 = "";
        for (int i = 0; i < categories.length; i++) {
            s+=categories[i] + ",";
        }
        for (int i = 0; i < s.length() - 1; i++) {
            s2+=s.charAt(i);
        }
        return s2;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String getGenres() {
        String s = "";
        String s2 = "";
        for (int i = 0; i < genres.length; i++) {
            s+=genres[i] + ",";
        }
        for (int i = 0; i < s.length() - 1; i++) {
            s2+=s.charAt(i);
        }
        return s2;
    }

    public String toString() {
        String data = "";
        try {
            data = Main.dateToString(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "\nID: "+id +
                "\nName: "+name +
                "\nDate: "+ data +
                "\nEnglish Support: " + lan_support +
                "\nDeveloper(s): " + getDeveloper() +
                "\nPublisher(s): " + getPublisher() +
                "\nPlatform: " + getPlatforms() +
                "\nRequired Age: " + required_age +
                "\nCategories: " + getCategories() +
                "\nGenres: " + getGenres();
    }

    public byte[] toByteArray() throws Exception{

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(name);
        dos.writeUTF(Main.dateToString(date));
        dos.writeInt(lan_support);
        dos.writeUTF(getDeveloper());
        dos.writeUTF(getPublisher());
        dos.writeUTF(getPlatforms());
        dos.writeInt(required_age);
        dos.writeUTF(getCategories());
        dos.writeUTF(getGenres());

        return baos.toByteArray();
    }

    public void fromByteArray(byte ba[]) throws Exception{

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        String data = "";

        id=dis.readInt();
        name=dis.readUTF();
        data=dis.readUTF();
        date = Main.toDate(data);
        lan_support = dis.readInt();

        String[] developers = dis.readUTF().split(",");
        setDeveloper(developers);

        String[] publishers = dis.readUTF().split(",");
        setPublisher(publishers);

        String[] platform = dis.readUTF().split(",");
        setPlatforms(platform);

        required_age = dis.readInt();

        String[] categorie = dis.readUTF().split(",");
        setCategories(categorie);

        String[] genre = dis.readUTF().split(",");
        setGenres(genre);
    }
}