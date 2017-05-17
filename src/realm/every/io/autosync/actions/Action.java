package realm.every.io.autosync.actions;

import java.nio.ByteBuffer;

import realm.every.io.autosync.Server;

public abstract class Action {

    public enum ACTIONS {
        ADD_DIR,
        REM_DIR,
        SAVE,
        LOAD
    }

    public static Action decodeAndCreateAction(byte[] buf) {
        Action ret = null;
        ByteBuffer bb = ByteBuffer.wrap(buf);
        try {
            if (bb.remaining() >= 4) {
                switch (bb.getInt()) {
                case 1: // add dir
                    ret = new AddDirAction();
                    ret.decode(buf);
                    break;
                case 2:

                }
            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
        return ret;
    }

    public abstract void decode(byte[] buf) throws Exception;
    public abstract byte[] encode() throws Exception;
    public abstract void runAction(Server server) throws Exception;
}

