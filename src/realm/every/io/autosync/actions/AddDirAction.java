package realm.every.io.autosync.actions;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import realm.every.io.autosync.DirWatcher;
import realm.every.io.autosync.Server;

public class AddDirAction extends Action {

    String[] directories;

    public AddDirAction() {}

    // pipe-delimited list; absolute paths
    // or paths relative to server's repo
    public AddDirAction(String dirs) {
        this.directories = parse(dirs);
    }

    @Override
    public void decode(byte[] buf) {
        // ensure correct #
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int n = bb.getInt();
        if (n != 1) {
            throw new IllegalArgumentException("Recieved: " + n + ", Expected: 1");
        }

        byte[] rem = new byte[bb.remaining()];
        bb.get(rem);
        directories = parse(new String(rem));
    }

    @Override
    public byte[] encode() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        for (String s : directories) {
            baos.write(s.getBytes());
            baos.write("|".getBytes());
        }
        return baos.toByteArray();
    }

    @Override
    public void runAction(Server server) {
        for (String dir : directories) {
            if (server.dMap.containsKey(dir)) {
                System.out.println("Directory [" + dir + "] already watched.");
            } else {
                DirWatcher watcher = new DirWatcher(dir, server);
                server.dMap.put(dir, watcher);
                new Thread(watcher).start();
            }
        }
    }

    private String[] parse(String dirs) {
        return dirs.split("|");
    }
}
