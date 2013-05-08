package com.sentaca.watcher;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;

public interface DirectoryEventListener {

  /**
   * @see WatchService#close()
   * @param x
   */
  void onClosedWatchServiceException(ClosedWatchServiceException x);

  void onEvent(Kind<Path> kind, Path path);

  void onInterruptedException(InterruptedException x);

  void onIOException(IOException e);

  void onNewWatcher(String dir);

  void onWatcherClosed(String dir);

}
