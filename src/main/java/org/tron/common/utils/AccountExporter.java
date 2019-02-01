package org.tron.common.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.db.BlockStore;
import org.tron.core.services.interfaceOnSolidity.WalletOnSolidity;

@Slf4j
@Component
public class AccountExporter {
  public static final String FILE_NAME = "accounts.csv";
  public static final AtomicLong EXPORT_NUM = new AtomicLong(0);
  private static WalletOnSolidity walletOnSolidity;

  @Autowired
  public void setWalletOnSolidity(WalletOnSolidity walletOnSolidity) {
    AccountExporter.walletOnSolidity = walletOnSolidity;
  }

  public static void export(BlockStore blockStore, Iterable<Entry<byte[], AccountCapsule>> iterable) {
    walletOnSolidity.futureGetWithoutTimeout(() -> {
      List<BlockCapsule> blockList = blockStore.getBlockByLatestNum(1);
      if (CollectionUtils.isNotEmpty(blockList) && blockList.get(0).getNum() == EXPORT_NUM.get()) {
        export(iterable);
      }
    });
  }

  private static void export(Iterable<Entry<byte[], AccountCapsule>> iterable) {
    AtomicLong total = new AtomicLong(0);
    Map<String, Long> accounts = Streams.stream(iterable)
        .map(e -> Maps.immutableEntry(Wallet.encode58Check(e.getKey()), e.getValue().getBalance()))
        .peek(e -> {
          if (e.getValue() >= 0) total.getAndAdd(e.getValue());
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));

    try (CSVPrinter printer = new CSVPrinter(new FileWriter("block_" + EXPORT_NUM.get() + "_" + FILE_NAME),
        CSVFormat.EXCEL.withHeader("address", "balance"))) {
      printer.printRecord("total", total.get());
      accounts.forEach((k, v) -> {
            String address = k;
            long balance = v;
            try {
              printer.printRecord(address, balance);
            } catch (Exception e1) {
              logger.error("address {} write error.", address);
            }
          });
    } catch (Exception e) {
      logger.error("export error", e);
    }
  }
}
