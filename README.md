watcher
=======

Small lib to monitor whole directory structure via Java 7 Watch Service API.
	  
Following code:
	  
    public void testWatching() throws Exception {
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
  
with some events:

	mkdir /tmp/root/x
	mkdir /tmp/root/y
	touch /tmp/root/y/a
	mv /tmp/root/y /tmp/root/x
	rm -fr /tmp/root/x

will generate output like this:

	New watcher for /tmp/root
	New watcher for /tmp/root/x
	ENTRY_CREATE /tmp/root/x
	New watcher for /tmp/root/y
	ENTRY_CREATE /tmp/root/y
	ENTRY_CREATE /tmp/root/y/a
	ENTRY_MODIFY /tmp/root/y/a
	New watcher for /tmp/root/x/y
	Watcher removed: /tmp/root/y
	ENTRY_DELETE /tmp/root/y
	ENTRY_CREATE /tmp/root/x/y
	Watcher removed: /tmp/root/x
	ENTRY_DELETE /tmp/root/x
	ENTRY_DELETE /tmp/root/x/y/a

