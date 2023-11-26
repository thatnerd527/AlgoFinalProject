package Server;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;

public class WrappedReader extends Reader {

    private Reader _reader;

    private Consumer<IOException> _onDisconnect;

    public WrappedReader(Reader reader, Consumer<IOException> onDisconnect) {
        assertNotNull(reader);
        assertNotNull(onDisconnect);
        _reader = reader;
        _onDisconnect = onDisconnect;
    }

    @Override
    public void close() {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _reader.close();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        this._onDisconnect.accept(cE);
    }

    @Override
    public int read(char[] arg0, int arg1, int arg2) {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                return _reader.read(arg0, arg1, arg2);
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        this._onDisconnect.accept(cE);
        return 0;
    }

    @Override
    public int read(char[] arg0) {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                return _reader.read(arg0);
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        this._onDisconnect.accept(cE);
        return 0;
    }

}

