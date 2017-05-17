package realm.every.io.autosync;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirWatcher implements Runnable {
    final Path path;

    private boolean running = true;
    private Thread myThread = null;
    private final Syncer sync;
    private final Server server;

    private Exception error = null;

    public DirWatcher(String path, Server server) {
        this.path = Paths.get(path);
        this.sync = new Rsyncer(server.getRemotePrefix());
        this.server = server;
    }

    @Override
    public void run() {
        myThread = Thread.currentThread();
        FileSystem fs = path.getFileSystem();
        try (WatchService watcher = fs.newWatchService()) {
            path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            WatchKey key = null;
            while (running) {
                key = watcher.take();

                Kind<?> kind = null;
                for (WatchEvent<?> event : key.pollEvents()) {
                    kind = event.kind();

                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    Path crPath = path.resolve(((WatchEvent<Path>)event).context()).toAbsolutePath();
                    String localPath = crPath.toString();
                    String remotePath = server.computeRemotePathFromLocal(crPath);

                    // TODO: use thread pool for these actions instead of relying on single thread?
                    if (kind == ENTRY_CREATE) {
                        sync.syncCreate(localPath, remotePath);
                    } else if (kind == ENTRY_MODIFY) {
                        sync.syncModify(localPath, remotePath);
                    }else if (kind == ENTRY_DELETE) {
                        sync.syncDelete(localPath, remotePath);
                    }
                }
            }
        } catch (Exception e) {
            error = e;
            e.printStackTrace();
        }
    }

    public synchronized void stop() {
        running = false;
        if (myThread != null) {
            myThread.interrupt();
        }
    }

    public Exception getErrorCause() {
        return error;
    }

    public Thread getRunningThread() {
        return myThread;
    }
}
