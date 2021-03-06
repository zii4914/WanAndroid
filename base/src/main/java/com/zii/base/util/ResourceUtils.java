package com.zii.base.util;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * MARK mark-zii : 2018/11/11 Assets使用相对路径如，在Assets目录下存在test文件夹，test文件夹下存在文件test.txt，该文件相对路径为，"test/test.txt"
 * MARK mark-zii : 2018/11/11  Raw可以允许直接使用资源id的形式，如R.raw.test
 * ResourceUtils
 * Create by zii at 2018/11/11.
 */
public class ResourceUtils {

  private static final int BUFFER_SIZE = 8192;

  private ResourceUtils() {
    throw new UnsupportedOperationException("u can't instantiate me...");
  }

  public static InputStream readAssets2Stream(final String assetsFilePath) {
    try {
      return Utils.getApp().getAssets().open(assetsFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static InputStream readRaw2Stream(@RawRes final int resId) {
    try {
      return Utils.getApp().getResources().openRawResource(resId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Copy the file from assets.
   *
   * @param assetsFilePath The path of file in assets.
   * @param destFilePath The path of destination file.
   * @return {@code true}: success<br>{@code false}: fail
   */
  public static boolean copyFileFromAssets(final String assetsFilePath, final String destFilePath) {
    boolean res = true;
    try {
      String[] assets = Utils.getApp().getAssets().list(assetsFilePath);
      if (assets.length > 0) {
        for (String asset : assets) {
          res &= copyFileFromAssets(assetsFilePath + "/" + asset, destFilePath + "/" + asset);
        }
      } else {
        res = writeFileFromIS(
            destFilePath,
            Utils.getApp().getAssets().open(assetsFilePath),
            false
        );
      }
    } catch (IOException e) {
      e.printStackTrace();
      res = false;
    }
    return res;
  }

  /**
   * Return the content of assets.
   *
   * @param assetsFilePath The path of file in assets.
   * @return the content of assets
   */
  public static String readAssets2String(final String assetsFilePath) {
    return readAssets2String(assetsFilePath, null);
  }

  /**
   * Return the content of assets.
   *
   * @param assetsFilePath The path of file in assets.
   * @param charsetName The name of charset.
   * @return the content of assets
   */
  public static String readAssets2String(final String assetsFilePath, final String charsetName) {
    InputStream is;
    try {
      is = Utils.getApp().getAssets().open(assetsFilePath);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    byte[] bytes = is2Bytes(is);
    if (bytes == null) {
      return null;
    }
    if (isSpace(charsetName)) {
      return new String(bytes);
    } else {
      try {
        return new String(bytes, charsetName);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        return "";
      }
    }
  }

  /**
   * Return the content of file in assets.
   *
   * @param assetsPath The path of file in assets.
   * @return the content of file in assets
   */
  public static List<String> readAssets2List(final String assetsPath) {
    return readAssets2List(assetsPath, null);
  }

  /**
   * Return the content of file in assets.
   *
   * @param assetsPath The path of file in assets.
   * @param charsetName The name of charset.
   * @return the content of file in assets
   */
  public static List<String> readAssets2List(final String assetsPath,
      final String charsetName) {
    try {
      return is2List(Utils.getApp().getResources().getAssets().open(assetsPath), charsetName);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Copy the file from raw.
   *
   * @param resId The resource id.
   * @param destFilePath The path of destination file.
   * @return {@code true}: success<br>{@code false}: fail
   */
  public static boolean copyFileFromRaw(@RawRes final int resId, final String destFilePath) {
    return writeFileFromIS(
        destFilePath,
        Utils.getApp().getResources().openRawResource(resId),
        false
    );
  }

  /**
   * Return the content of resource in raw.
   *
   * @param resId The resource id.
   * @return the content of resource in raw
   */
  public static String readRaw2String(@RawRes final int resId) {
    return readRaw2String(resId, null);
  }

  /**
   * Return the content of resource in raw.
   *
   * @param resId The resource id.
   * @param charsetName The name of charset.
   * @return the content of resource in raw
   */
  public static String readRaw2String(@RawRes final int resId, final String charsetName) {
    InputStream is = Utils.getApp().getResources().openRawResource(resId);
    byte[] bytes = is2Bytes(is);
    if (bytes == null) {
      return null;
    }
    if (isSpace(charsetName)) {
      return new String(bytes);
    } else {
      try {
        return new String(bytes, charsetName);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        return "";
      }
    }
  }

  /**
   * Return the content of resource in raw.
   *
   * @param resId The resource id.
   * @return the content of file in assets
   */
  public static List<String> readRaw2List(@RawRes final int resId) {
    return readRaw2List(resId, null);
  }

  /**
   * Return the content of resource in raw.
   *
   * @param resId The resource id.
   * @param charsetName The name of charset.
   * @return the content of file in assets
   */
  public static List<String> readRaw2List(@RawRes final int resId,
      final String charsetName) {
    return is2List(Utils.getApp().getResources().openRawResource(resId), charsetName);
  }

  ///////////////////////////////////////////////////////////////////////////
  // other utils methods
  ///////////////////////////////////////////////////////////////////////////

  private static boolean writeFileFromIS(final String filePath,
      final InputStream is,
      final boolean append) {
    return writeFileFromIS(getFileByPath(filePath), is, append);
  }

  private static boolean writeFileFromIS(final File file,
      final InputStream is,
      final boolean append) {
    if (!createOrExistsFile(file) || is == null) {
      return false;
    }
    OutputStream os = null;
    try {
      os = new BufferedOutputStream(new FileOutputStream(file, append));
      byte[] data = new byte[BUFFER_SIZE];
      int len;
      while ((len = is.read(data, 0, BUFFER_SIZE)) != -1) {
        os.write(data, 0, len);
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        if (os != null) {
          os.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static File getFileByPath(final String filePath) {
    return isSpace(filePath) ? null : new File(filePath);
  }

  private static boolean createOrExistsFile(final File file) {
    if (file == null) {
      return false;
    }
    if (file.exists()) {
      return file.isFile();
    }
    if (!createOrExistsDir(file.getParentFile())) {
      return false;
    }
    try {
      return file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private static boolean createOrExistsDir(final File file) {
    return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
  }

  private static boolean isSpace(final String s) {
    if (s == null) {
      return true;
    }
    for (int i = 0, len = s.length(); i < len; ++i) {
      if (!Character.isWhitespace(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static byte[] is2Bytes(final InputStream is) {
    if (is == null) {
      return null;
    }
    ByteArrayOutputStream os = null;
    try {
      os = new ByteArrayOutputStream();
      byte[] b = new byte[BUFFER_SIZE];
      int len;
      while ((len = is.read(b, 0, BUFFER_SIZE)) != -1) {
        os.write(b, 0, len);
      }
      return os.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        if (os != null) {
          os.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static List<String> is2List(final InputStream is,
      final String charsetName) {
    BufferedReader reader = null;
    try {
      List<String> list = new ArrayList<>();
      if (isSpace(charsetName)) {
        reader = new BufferedReader(new InputStreamReader(is));
      } else {
        reader = new BufferedReader(new InputStreamReader(is, charsetName));
      }
      String line;
      while ((line = reader.readLine()) != null) {
        list.add(line);
      }
      return list;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static String getString(@StringRes int strRes) {
    return Utils.getApp().getString(strRes);
  }

  /**
   * 获取Drawable
   *
   * @param resId 资源id，可以是R.drawable.xxx，或者R.color.xxx
   * @return Drawable
   */
  public static Drawable getDrawable(@DrawableRes int resId) {
    return ContextCompat.getDrawable(Utils.getApp(), resId);
  }

  public static int getColor(String color) {
    return Color.parseColor(color);
  }

  public static int getColor(@ColorRes int resId) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return Utils.getApp().getColor(resId);
    } else {
      return ContextCompat.getColor(Utils.getApp(), resId);
    }
  }
}
