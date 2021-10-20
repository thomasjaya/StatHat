package stathat.objects;

public class UserSettings {
    /* Class to locally store user preferences from file */

    private boolean toggled;
    private float height;
    private boolean shadow;
    private boolean personal;
    private String gamemode; // e.g bridge shows bridge stats

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    public String getGamemode() { return gamemode; }

    public void setGamemode(String gamemode) { this.gamemode = gamemode; }
}
