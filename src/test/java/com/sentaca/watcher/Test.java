package com.sentaca.watcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

import junit.framework.TestCase;

public class Test extends TestCase {

  public void testname() throws Exception {
    new DirectoryWatchService(new DirectoryEventListenerAdapter() {

      @Override
      public void onEvent(Kind<Path> kind, Path path) {
        System.out.println(kind + " " + path);
      }

      @Override
      public void onNewWatcher(String dir) {
        System.out.println("New watcher for " + dir);
      }

      @Override
      public void onWatcherClosed(String dir) {
        System.out.println("Watcher removed: " + dir);
      }
    }, "/tmp/root").watch();

    Thread.sleep(10000000);
  }
}
