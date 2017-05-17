package realm.every.io.autosync;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Simple Syncer that uses rsync. Useful for linux/cygwin,
 * not that useful if not installed or Windows/etc
 *
 * @author faym
 *
 */
public class Rsyncer implements Syncer {

    private final String remotePrefix;

    public Rsyncer(String remotePrefix) {
        this.remotePrefix = remotePrefix;
    }

    private void sync(String local, String remote) {
        ProcessBuilder pb = new ProcessBuilder(new String[] {
                    "rsync", "-aq", local, remotePrefix + remote
                });
        try {
            // TODO do stuff with process later
            @SuppressWarnings("unused")
            Process p = pb.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void syncDelete(String local, String remote) {
        // can be done by sync-ing directory and not the file itself
        // TODO: overkill, not performant
        String l = Paths.get(local).getParent().toString();
        String r = Paths.get(remote).getParent().toString();
        sync(l, r);
    }

    @Override
    public void syncCreate(String local, String remote) {
        sync(local, remote);
    }

    @Override
    public void syncModify(String local, String remote) {
        sync(local, remote);
    }
}
