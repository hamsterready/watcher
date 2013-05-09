package com.sentaca.watcher;

import junit.framework.TestCase;

public class DirectoryWatchServiceTest extends TestCase {

  private DirectoryWatchService service;

  @Override
  protected void setUp() throws Exception {
    service = new DirectoryWatchService();
  }

  public void testInvalidSetup() throws Exception {
    try {
      service.watch();
      fail("IllegalArgumentException is expected when no params are provied.");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
