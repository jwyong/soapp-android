package com.soapp.global;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by jason on 13/02/2018.
 */

public class DecryptionHelper {

    // private Aeskey and Aesivs
    private byte[] Aeskey() {
        byte[] key = new byte[0];
        try {
            key = GlobalVariables.key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return key;
    }

    private byte[] Aesivs() {
        byte[] ivs = new byte[0];
        try {
            ivs = GlobalVariables.ivKey.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ivs;
    }

    /******** indi text , image , video , audio ********/

    //decrypt text message strings before get in
    public String decryptText(String Body) {

        // indi incoming Txt to decryption
//        byte[] decodedBytes = decodeBase64(Body.getBytes()); //decodebase64
//
//        // compile Aeskey & Aesivs
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        // indi chat decryption
//        Cipher cipher = null;
//        try {
//            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        byte[] decryptbase64 = new byte[0];
//        try {
//            decryptbase64 = cipher.doFinal(decodedBytes);
//        } catch (IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//
//        String decryptString = new String(decryptbase64);
//
//        return decryptString;

        return Body;
    }

    //decrypt image file before get in
    public File decryptImg(File image, String longs) {
//
//        InputStream is = null;
//        try {
//            is = new FileInputStream(image);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        long length = image.length();
//        if (length > Integer.MAX_VALUE) {
//            // File is too large
//        }
//        byte[] imagebytes = new byte[(int)length];
//        int offset = 0;
//        int numRead = 0;
//
//        try {
//            while (offset < imagebytes.length
//                    && (numRead=is.read(imagebytes, offset, imagebytes.length-offset)) >= 0) {
//                offset += numRead;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (offset < imagebytes.length) {
//            try {
//                throw new IOException("Could not completely read file " + image.getName());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            is.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Cipher imgcipher = null;
//        try {
//            imgcipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        try {
//            imgcipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        byte[] Imgdecrypt = null;
//        try {
//            Imgdecrypt = imgcipher.doFinal(imagebytes);
//        } catch (IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//
//        //testing the image for base64 or aes 128
//        FileOutputStream imageOutFile = null;
//        try {
//            imageOutFile = new FileOutputStream(GlobalVariables.IMAGES_PATH + longs + ".jpg");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            imageOutFile.write(Imgdecrypt);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return image;
    }


    public File decryptVideo(File Video, String longs) {

//        // decrypt for video
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(Video);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        byte[] buf = new byte[1024];
//        int n;
//        try {
//            while (-1 != (n = fis.read(buf)))
//                baos.write(buf, 0, n);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        byte[] videoBytes = baos.toByteArray();
//
//        Cipher Videocipher = null;
//        try {
//            Videocipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//        try {
//            Videocipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//        //decrypt
//        byte[] Videodecrypted = new byte[0];
//        try {
//            Videodecrypted = Videocipher.doFinal(videoBytes);
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        }
//        //testing the video for base64 or aes 128
//        FileOutputStream outFile = null;
//        try {
//            outFile = new FileOutputStream(GlobalVariables.VIDEO_PATH+"/"+longs+".mp4");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        // Converting a Base64 String into video byte array
//        try {
//            outFile.write(Videodecrypted);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return Video;
    }

    public File decryptAudio(File Audio, String longs) {
        // audio decrypt
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(Audio);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        byte[] buf = new byte[1024];
//        int n;
//        try {
//            while (-1 != (n = fis.read(buf)))
//                baos.write(buf, 0, n);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        byte[] AudioBytes = baos.toByteArray();
//
//        Cipher Acipher = null;
//        try {
//            Acipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        try {
//            Acipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        //decrypt
//        byte[] Adecrypted = new byte[0];
//        try {
//            Adecrypted = Acipher.doFinal(AudioBytes);
//        } catch (IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//
//        //testing the audio for base64 or aes 128
//        FileOutputStream outFile = null;
//        try {
//            outFile = new FileOutputStream(GlobalVariables.AUDIO_PATH+longs);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        // Converting a Base64 String into audio byte array
//        try {
//            outFile.write(Adecrypted);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return Audio;
    }

/******** END indi text , image , video , audio END ********/

}
