package org.sectorzero.components;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;

@Slf4j
public class RocksDbTest {

  RocksDB db;
  Options options;

  @Before
  public void setup() throws Exception {
    RocksDB.loadLibrary();
    options = new Options().setCreateIfMissing(true);
    db = RocksDB.open(options, "/tmp/rocksdb");
  }

  @After
  public void teardown() {
    if (db != null) db.close();
    options.dispose();
  }

  @Ignore
  @Test
  public void foo() throws Exception {
    RocksDB.loadLibrary();
    Options options = new Options().setCreateIfMissing(true);
    RocksDB db = null;
    try {
      db = RocksDB.open(options, "/tmp/rocksdb");
    } catch (RocksDBException e) {
      log.error("Exception opening db", e);
      throw e;
    } finally {
      if (db != null) db.close();
      options.dispose();
    }
  }

  @Test
  public void write() throws Exception {
    byte[] key_1 = b();
    db.put(key_1, b());
    byte[] v_1 = db.get(key_1);

    byte[] key_2 = b();
    byte[] v_2 = db.get(key_2);
    if(v_2 == null) {
      db.put(key_2, v_1);
    }

    assertTrue(Arrays.equals(db.get(key_1), db.get(key_2)));
  }

  byte[] b() {
    byte[] key_1 = new byte[32];
    ThreadLocalRandom.current().nextBytes(key_1);
    return key_1;
  }


}
