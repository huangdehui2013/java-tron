package org.tron.core.zen.address;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.tron.common.utils.ByteUtil;
import org.tron.common.zksnark.Librustzcash;

// Decryption using a Full Viewing Key

@AllArgsConstructor
public class FullViewingKey {

  @Getter
  @Setter
  private byte[] ak; // 256
  @Getter
  @Setter
  private byte[] nk; // 256
  @Getter
  @Setter
  private byte[] ovk; // 256,the outgoing viewing key

  public IncomingViewingKey inViewingKey() {
    byte[] ivk = new byte[32]; // the incoming viewing key
    Librustzcash.librustzcashCrhIvk(ak, nk, ivk);
    System.out.println("inviewkey.ivk is: " + ByteUtil.toHexString(ivk));
    return new IncomingViewingKey(ivk);
  }

  public boolean isValid() {
    byte[] ivk = new byte[32];
    Librustzcash.librustzcashCrhIvk(ak, nk, ivk);
    return !Arrays.equals(ivk, new byte[32]);
  }

  public byte[] encode() {
    byte[] m_bytes = new byte[96];
    System.arraycopy(ak, 0, m_bytes, 0, 32);
    System.arraycopy(nk, 0, m_bytes, 32, 32);
    System.arraycopy(ovk, 0, m_bytes, 64, 32);

    return m_bytes;
  }

  public static FullViewingKey decode(byte[] data) {
    byte[] ak = new byte[32];
    byte[] nk = new byte[32];
    byte[] ovk = new byte[32];
    System.arraycopy(data, 0, ak, 0, 32);
    System.arraycopy(data, 32, nk, 0, 32);
    System.arraycopy(data, 64, ovk, 0, 32);

    return new FullViewingKey(ak, nk, ovk);
  }
}