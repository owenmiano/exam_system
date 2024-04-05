//package ke.co.skyworld;
//
//import javax.crypto.Cipher;
//import java.nio.charset.StandardCharsets;
//import java.security.*;
//import java.security.spec.InvalidKeySpecException;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//
//import java.util.Base64;
//
//import static ke.co.skyworld.Model.ConfigReader.charArrayToByteArray;
//
//public class RSA {
//    private PrivateKey privateKey;
//    private PublicKey publicKey;
//
//    public void init(){
//        try {
//            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//            generator.initialize(2048);
//            KeyPair pair = generator.generateKeyPair();
//            privateKey = pair.getPrivate();
//            publicKey = pair.getPublic();
//        } catch (Exception ignored) {
//        }
//    }
//
//    public void initFromStrings() {
//        try {
//            X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(new String(charArrayToByteArray(KeyManager.PUBLIC_SECRET_KEY))));
//
//            PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(decode(new String(charArrayToByteArray(KeyManager.PRIVATE_SECRET_KEY))));
//
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Specify RSA provider explicitly
//            publicKey = keyFactory.generatePublic(keySpecPublic);
//            privateKey = keyFactory.generatePrivate(keySpecPrivate);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//            System.out.println("Error from initFromStrings: " + e.getMessage());
//        }
//    }
//
//
//    public void printKeys(){
//        System.err.println("Public key\n"+ encode(publicKey.getEncoded()));
//        System.err.println("Private key\n"+ encode(privateKey.getEncoded()));
//    }
//
//    public String encryptData(String message) throws Exception {
//        byte[] messageToBytes = message.getBytes();
//        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
//        return encode(encryptedBytes);
//    }
//
//    private static String encode(byte[] data) {
//        return Base64.getEncoder().encodeToString(data);
//    }
//    private static byte[] decode(String data) {
//        return Base64.getDecoder().decode(data);
//    }
//
//
//    public String decryptData(String encryptedMessage) throws Exception {
//        byte[] encryptedBytes = decode(encryptedMessage);
//        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.DECRYPT_MODE, privateKey);
//        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
//        return new String(decryptedMessage, StandardCharsets.UTF_8);
//    }
//
//    public static void main(String[] args) {
//        RSA rsa = new RSA();
////        rsa.init();
//       rsa.initFromStrings();
//
//        try{
//            String encryptedMessage = rsa.encryptData("Hello World");
//            String decryptedMessage = rsa.decryptData(encryptedMessage);
//
//            System.err.println("Encrypted:\n"+encryptedMessage);
//            System.err.println("Decrypted:\n"+decryptedMessage);
//            rsa.printKeys();
//        }catch (Exception ignored){}
//
//
//
//    }
//}
