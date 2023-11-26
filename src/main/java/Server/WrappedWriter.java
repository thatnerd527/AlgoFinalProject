package Server;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

public
class WrappedWriter extends Writer {
    private final Writer _writer;
    private final Consumer<IOException> _onDisconnect;

    public WrappedWriter(Writer writer, Consumer<IOException> onDisconnect) {
        assertNotNull(writer);
        assertNotNull(onDisconnect);
        _writer = writer;
        _onDisconnect = onDisconnect;
    }

    @Override
    public void close() {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _writer.close();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        _onDisconnect.accept(cE);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _writer.write(cbuf, off, len);
                _writer.flush();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        _onDisconnect.accept(cE);
    }

    @Override
    public void write(int c)  {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _writer.write(c);
                _writer.flush();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        _onDisconnect.accept(cE);
    }

    @Override
    public void write(String str, int off, int len) {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _writer.write(str, off, len);
                _writer.flush();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        _onDisconnect.accept(cE);
    }

    @Override
    public void write(String str) {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _writer.write(str);
                _writer.flush();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        _onDisconnect.accept(cE);
    }

    @Override
    public void flush() {
        int attempts = 0;
        IOException cE = null;
        while (attempts < 5) {
            try {
                _writer.flush();
                return;
            } catch (IOException e) {
                attempts++;
                cE = e;
            }
        }
        _onDisconnect.accept(cE);
    }
}