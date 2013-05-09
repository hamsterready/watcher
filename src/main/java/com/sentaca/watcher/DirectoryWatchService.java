package com.sentaca.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DirectoryWatchService {

  private final class DirectoryWatchThread extends Thread {
    private final Path dir;
    private final WatchService watcher;

    private DirectoryWatchThread(Path dir, WatchService watcher) {
      this.dir = dir;
      this.watcher = watcher;
    }

    public void run() {
      try {
        // -- code from
        // http://docs.oracle.com/javase/tutorial/essential/io/notification.html
        for (;;) {

          // wait for key to be signaled
          WatchKey key;
          try {
            key = watcher.take();
          } catch (InterruptedException x) {
            directoryWatchers.remove(dir);
            listener.onInterruptedException(x);
            return;
          } catch (ClosedWatchServiceException x) {
            directoryWatchers.remove(dir);
            listener.onClosedWatchServiceException(x);
            return;
          }

          for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            // This key is registered only
            // for ENTRY_CREATE events,
            // but an OVERFLOW event can
            // occur regardless if events
            // are lost or discarded.
            if (kind == StandardWatchEventKinds.OVERFLOW) {
              continue;
            }
            final Path context = (Path) event.context();
            final Path contextChild = dir.resolve(context);
            final File contextFile = contextChild.toFile();
            final String contextDirPath = contextFile.getAbsolutePath();
            if (contextFile.isDirectory()) {
              if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                createWatchersForSubdirectories(contextDirPath);
              }
            }

            if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
              WatchService removed = directoryWatchers.remove(contextDirPath);
              if (removed != null) {
                removed.close();
                listener.onWatcherClosed(contextDirPath);
              }
            }
            listener.onEvent((Kind<Path>) kind, contextChild);
          }

          // Reset the key -- this step is critical if you want to
          // receive further watch events. If the key is no longer valid,
          // the directory is inaccessible so exit the loop.
          boolean valid = key.reset();
          if (!valid) {
            break;
          }
        }
      } catch (IOException e) {
        listener.onIOException(e);
      }

    }
  }

  private boolean watchSubdirectories = true;

  private String rootDirectory;

  private Map<String, WatchService> directoryWatchers = Collections.synchronizedMap(new HashMap<String, WatchService>());

  private DirectoryEventListener listener;

  public DirectoryWatchService() {
  }

  public DirectoryWatchService(DirectoryEventListener listener, String rootDirectory) {
    this.listener = listener;
    this.rootDirectory = rootDirectory;
  }

  public DirectoryWatchService(DirectoryEventListener listener, String rootDirectory, boolean watchSubdirectories) {
    this(listener, rootDirectory);
    this.watchSubdirectories = watchSubdirectories;
  }

  private void createWatchersForSubdirectories(String dir) throws IOException {
    final DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(dir));
    createWatcherWithWorkingThreadForDirectory(dir);
    for (Path p : ds) {
      if (p.toFile().isDirectory()) {
        createWatchersForSubdirectories(p.toString());
      }
    }
  }

  private void createWatcherWithWorkingThreadForDirectory(String dirPath) throws IOException {
    final Path dir = Paths.get(dirPath);

    final WatchService watcher = FileSystems.getDefault().newWatchService();
    directoryWatchers.put(dirPath, watcher);

    listener.onNewWatcher(dirPath);

    dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.OVERFLOW);

    new DirectoryWatchThread(dir, watcher).start();
  }

  public void setListener(DirectoryEventListener listener) {
    this.listener = listener;
  }

  public void setRootDirectory(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  public void setWatchSubdirectories(boolean watchSubdirectories) {
    this.watchSubdirectories = watchSubdirectories;
  }

  public void watch() throws IOException {
    if (listener == null) {
      throw new IllegalArgumentException("Listener must not be null, use constructor or setListener() method to set one.");
    }
    if (rootDirectory == null) {
      throw new IllegalArgumentException("RootDirectory must not be null, use constructor or setRootDirectory method to set one.");
    }

    if (!watchSubdirectories) {
      createWatcherWithWorkingThreadForDirectory(rootDirectory);
    } else {
      createWatchersForSubdirectories(rootDirectory);
    }
  }
}
