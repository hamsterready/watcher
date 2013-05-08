package com.sentaca.watcher;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;

public abstract class DirectoryEventListenerAdapter implements DirectoryEventListener {

  @Override
  public void onClosedWatchServiceException(ClosedWatchServiceException x) {
    // do nothing, directory has been removed and we have just closed watcher
    // @see WatchService#close()

  }

  @Override
  public void onInterruptedException(InterruptedException x) {
    // do nothing
  }

  @Override
  public void onIOException(IOException e) {
    // probably one should log as something wrong could just happened
  }

  @Override
  public void onNewWatcher(String dir) {

  }

  @Override
  public void onWatcherClosed(String dir) {

  }
}
