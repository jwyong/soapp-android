package com.soapp.global;

/*Created by Soapp on 04/11/2017. */

import java.io.File;
import java.io.UnsupportedEncodingException;

public class EncryptionHelper {

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


    /******** text , image , video , audio ********/

    //encrypt text message strings before sending out
    public String encryptText(String text) {
        // indi outgoing encrypt text message

//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        // indi chat encryption
//        Cipher cipher = null;
//        try {
//            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        String encryptbase64Txt = null;
//        try {
//            encryptbase64Txt = Base64.encodeToString(cipher.doFinal(text.getBytes("UTF-8")), Base64.DEFAULT);
//        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return encryptbase64Txt;

        //no encryption
        return text;
    }

    //encrypt image file before sending out
    public File encryptImage(File image, String imgPath, String ReimgPath) {

//        // image FileInputStream
//        FileInputStream imgfis = null;
//        try {
//            imgfis = new FileInputStream(image);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        Bitmap imgbm = BitmapFactory.decodeStream(imgfis); // image bitmap
//
//        ByteArrayOutputStream imgbaos = new ByteArrayOutputStream();
//        imgbm.compress(Bitmap.CompressFormat.JPEG, 100, imgbaos);
//        byte[] img = imgbaos.toByteArray(); // image byte
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        // indi image encryption
//        Cipher Imgcipher = null;
//        try {
//            Imgcipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Imgcipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        byte[] encryptImg = new byte[0];
//        try {
//            encryptImg = Imgcipher.doFinal(img);
//        } catch (IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
////
//        FileOutputStream Imgoutfile = null;
//
//        File ImgFileEncrypt;
//        // ryan change to cache
//        if(imgPath != null) {
//            ImgFileEncrypt = new File(GlobalVariables.IMAGES_SENT_PATH + "/test" + imgPath.substring(imgPath.indexOf('/') + 1));
//        } else {
//            ImgFileEncrypt = new File(GlobalVariables.IMAGES_SENT_PATH +"/reload.jpg");
//        }
//
//        try {
//            Imgoutfile = new FileOutputStream(ImgFileEncrypt);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            Imgoutfile.write(encryptImg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            Imgoutfile.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            Imgoutfile.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return ImgFileEncrypt;

        //no encryption
        return image;
    }

    //encrypt video file before sending out
    public File encryptVideo(File video, String VideoPath) {

        //   fileInputStream to get Bitmap and encrypt aes 128

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(video);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        byte[] buf = new byte[1024];
//
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
//        Cipher Vencipher = null;
//        try {
//            Vencipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        try {
//            Vencipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        byte[] encryptVideo = new byte[0];
//        try {
//            encryptVideo = Vencipher.doFinal(videoBytes);
//        } catch (IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//
//        FileOutputStream outfilebyte = null;
//        final File Vencryptedfile = new File(GlobalVariables.VIDEO_SENT_PATH + "/" + VideoPath + "Test.mp4");
//        try {
//            outfilebyte = new FileOutputStream(Vencryptedfile);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            outfilebyte.write(encryptVideo);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outfilebyte.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outfilebyte.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return Vencryptedfile;

        //no encryption
        return video;
    }

    //encrypt audio file before sending out
    public File encryptAudio(File audio, String audioPath) {


//        ByteArrayOutputStream Abaos = new ByteArrayOutputStream();
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(audio);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        byte[] buf = new byte[1024];
//
//        int n;
//        try {
//            while (-1 != (n = fis.read(buf)))
//                Abaos.write(buf, 0, n);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        byte[] AudioBytes = Abaos.toByteArray();
//
//        Cipher Aencipher = null;
//        try {
//            Aencipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(Aeskey(), "AES");
//        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Aesivs());
//
//        try {
//            Aencipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
//
//        byte[] encryptAudio = new byte[0];
//        try {
//            encryptAudio = Aencipher.doFinal(AudioBytes);
//        } catch (IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//
//        FileOutputStream outfilebyte = null;
//        final File Aencryptedfile = new File(GlobalVariables.AUDIO_SENT_PATH+"/"+"AUDString.m4a");
//        try {
//            outfilebyte = new FileOutputStream(Aencryptedfile);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            outfilebyte.write(encryptAudio);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outfilebyte.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outfilebyte.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return Aencryptedfile;

        //no encryption
        return audio;
    }

    /******** text , image , video , audio ********/

}
