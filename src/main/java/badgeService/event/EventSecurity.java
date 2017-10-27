package badgeService.event;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.data.util.Pair;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by katharine on 07/04/2017.
 */
public class EventSecurity {

    private static Pair<Cipher, SecretKeySpec> getCipherAndSecret(String key) {

        SecretKeyFactory factory;
        Cipher cipher;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException na) {
            System.out.print(na);
            return null;
        } catch (NoSuchPaddingException pa) {
            System.out.print(pa);
            return null;
        }
        int iterations = 1000;
        int keyLength = 256;
        PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), key.getBytes(StandardCharsets.UTF_8), iterations, keyLength);
        SecretKey secretKey;
        try {
            secretKey = factory.generateSecret(spec);
        } catch (Exception e) {
            System.out.print(e);
            return null;
        }
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        return Pair.of(cipher, secret);
    }

    public static String encrypt(String key, String payload) {
        Pair<Cipher, SecretKeySpec> cipherAndSecret = getCipherAndSecret(key);
        Cipher cipher = cipherAndSecret.getFirst();
        SecretKeySpec secret = cipherAndSecret.getSecond();

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            byte[] data = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] iv = cipher.getIV();
            return Base64.encodeBase64URLSafeString(iv) + "|" + Base64.encodeBase64URLSafeString(data);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static String decrypt(String key, String payload) {
        try {
            Pair<Cipher, SecretKeySpec> cipherAndSecret = getCipherAndSecret(key);
            Cipher cipher = cipherAndSecret.getFirst();
            SecretKeySpec secret = cipherAndSecret.getSecond();

            int index = payload.indexOf("|");
            String ivC = payload.substring(0,index);
            String bodyC = payload.substring(index+1);

            byte[] iv = Base64.decodeBase64(ivC);
            byte[] body = Base64.decodeBase64(bodyC);
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(body);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
