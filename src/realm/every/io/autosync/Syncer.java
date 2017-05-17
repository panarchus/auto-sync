package realm.every.io.autosync;

public interface Syncer {
    void syncCreate(String local, String remote);
    void syncModify(String local, String remote);
    void syncDelete(String local, String remote);
}
