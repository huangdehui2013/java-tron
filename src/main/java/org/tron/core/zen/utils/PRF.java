package org.tron.core.zen.utils;

import org.tron.common.zksnark.Librustzcash;
import org.tron.common.zksnark.Libsodium;
import org.tron.common.zksnark.Libsodium.ILibsodium.crypto_generichash_blake2b_state;
import org.tron.core.Constant;

public class PRF {

  public static byte[] prfAsk(byte[] sk) {
    byte[] ask = new byte[32];
    byte t = 0x00;
    byte[] tmp = prfExpand(sk, t);
    Librustzcash.librustzcashToScalar(tmp, ask);
    return ask;
  }

  public static byte[] prfNsk(byte[] sk) {
    byte[] nsk = new byte[32];
    byte t = 0x01;
    byte[] tmp = prfExpand(sk, t);
    Librustzcash.librustzcashToScalar(tmp, nsk);
    return nsk;
  }

  public static byte[] prfOvk(byte[] sk) {
    byte[] ovk = new byte[32];
    byte t = 0x02;
    byte[] tmp = prfExpand(sk, t);
    System.arraycopy(tmp, 0, ovk, 0, 32);
    return ovk;
  }

  private static byte[] prfExpand(byte[] sk, byte t) {
    byte[] res = new byte[64];
    byte[] blob = new byte[33];
    System.arraycopy(sk, 0, blob, 0, 32);
    blob[32] = t;
    crypto_generichash_blake2b_state.ByReference state = new crypto_generichash_blake2b_state.ByReference();
    Libsodium.cryptoGenerichashBlake2bInitSaltPersonal(
        state, null, 0, 64, null, Constant.ZCASH_EXPANDSEED_PERSONALIZATION);
    Libsodium.cryptoGenerichashBlake2bUpdate(state, blob, 33);
    Libsodium.cryptoGenerichashBlake2bFinal(state, res, 64);

    return res;
  }
}