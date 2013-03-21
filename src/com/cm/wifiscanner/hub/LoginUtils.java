package com.cm.wifiscanner.hub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cm.wifiscanner.legacy.PrefsHelper;
import com.cm.wifiscanner.util.Constants;
import com.cm.wifiscanner.util.Logger;
import com.cm.wifiscanner.util.Utils;

public class LoginUtils {

	private static final String TAG = "LoginUtil";

	private static final String CHALLENGE = "GET / HTTP/1.0\nUser-Agent: Wget/1.12(cygwin)\r\nAccept: */*\r\nHost: 1.1.1.1\r\nConnection: Close\r\n\r\n";
	private static final String LOGIN_FMT = "GET http://%s:%d/logon?username=%s&response=%s&userurl=%s HTTP/1.0\r\nUser-Agent: Wget/1.12(cygwin)\r\nAccept: */*\r\nHost: %s:%d\r\nConnection: Close\r\n\r\n";
	private static final String LOGOUT_FMT = "GET /logoff HTTP/1.0\nUser-Agent: Wget/1.12(cygwin)\r\nAccept: */*\r\nHost: %s\r\nConnection: Close\r\n\r\n";
//	private static final String PRELOGIN = "GET /prelogin HTTP/1.0";

	private static final int CACHE_LEN = 4096;
	private byte[] CACHE = new byte[CACHE_LEN];
	private String mServer;
	private int mPort;
	private Context mContext;
	private static LoginUtils sInstance;

	private LoginUtils(Context context) {
		mContext = context.getApplicationContext();
	}

	public static synchronized LoginUtils getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new LoginUtils(context);
		}

		return sInstance;
	}

    private String talkWithServer(String host, int port, String content) {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        try {
            Logger.debug(TAG, "Service:" + content);
            socket = new Socket();
            InetSocketAddress add = new InetSocketAddress(host, port);
            // Socket socket = new Socket(host, port);
            Logger.debug(TAG, "Service: 10");
            socket.connect(add, 10000);
            socket.setSoTimeout(10000);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            int contentLen = content.length();
//            byte[] isbyte = new Byte[contentLen];
            //content.getBytes(0, contentLen, isbyte, 0);
            byte[] isbyte = content.getBytes();
            os.write(isbyte, 0, contentLen);
            os.flush();
            Logger.debug(TAG, "Service: 11");
            int recv = 0, received = 0;
            recv = is.read(CACHE, received, CACHE_LEN);
            if (recv != -1) {
                CACHE[recv] = 0;
            }
            Logger.debug(TAG, "Service: 12");
            if (recv == -1) {
                return null;
            }
        } catch (IOException ioe) {
            Logger.debug(TAG, ioe.toString());
            ioe.printStackTrace();
            return null;
        } catch (Exception e) {
            Logger.debug(TAG, e.toString());
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        try {
            return new String(CACHE, "GBK");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
	}

	private String getChallenage() {
		String result = talkWithServer("1.1.1.1", 80, CHALLENGE);
		if(result == null) {
		    return null;
		}
		try {
            result = URLDecoder.decode(result,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.debug(TAG, e.toString());
            e.printStackTrace();
        }
		if (TextUtils.isEmpty(result)) {
			return null;
		}

		final String LOCATION = "Location:";
		int index = result.indexOf(LOCATION);
		if (index < 0) {
			return null;
		}

		int startIndex = index + LOCATION.length();
		int endIndex = startIndex + 5;

		char[] buf = result.toCharArray();
		while (buf[startIndex] == ' ') {
			startIndex++;
			endIndex++;
		}

		String subString = result.substring(startIndex, endIndex);
		if (subString.compareToIgnoreCase("http:") == 0) {
			int serverStart = endIndex;
			int portStart = result.indexOf(":", endIndex + 1);
			int portEnd = result.indexOf("/", portStart + 1);
			if (portStart < 0 || portEnd < 0) {
				Log.e(TAG, "portStart < 0 || portEnd < 0");
				return null;
			}

			while (buf[serverStart] == ' ') {
				serverStart++;
			}

			while (buf[serverStart] == '/') {
				serverStart++;
			}

			mServer = result.substring(serverStart, portStart);
			Logger.debug(TAG, "mServer: " + mServer);
			while (buf[portStart] == ':') {
				portStart++;
			}
			String port = result.substring(portStart, portEnd);

			mPort = Integer.parseInt(port);
	        Logger.debug(TAG, "mPort: " + mPort);

		} else {
			return null;
		}

		final String CHALLENGE = "challenge";
		index = result.indexOf(CHALLENGE);
		index += CHALLENGE.length();
		endIndex = result.indexOf("\n", index + 1);
		if (endIndex < 0 || index < 0) {
			Log.e(TAG, "challenge:endIndex < 0 || index < 0");
			return null;
		}

		int start = index;
		while (buf[start] == '=') {
			start++;
		}

		String challenge = result.substring(start, start + 32);
		challenge = challenge.toUpperCase(Locale.getDefault());

		PrefsHelper.getInstance(mContext).saveServer(mServer);
		PrefsHelper.getInstance(mContext).savePort(mPort);

		return challenge;
	}

	private byte[] ASC2Hex(final char[] str) {
		int nstrlen = str.length;
		int nlen = 0;
		if (nstrlen % 2 != 0) {
			nlen = -1;
			return null;
		}

		nlen = nstrlen / 2;

		byte[] Hex = new byte[nlen];
		for (int i = 0; i < nlen; i++) {
			int pos = i * 2;

			int value = (int) (charToByte(str[pos]) << 4 | charToByte(str[pos + 1]));
			Hex[i] = (byte) (value & 0xFF);
		}

		return Hex;
	}

	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	private String getNewChallenage(String nameSecret, String challenge) {
		challenge = challenge.toUpperCase();
		byte[] src = ASC2Hex(challenge.toCharArray());

		byte[] newBytes = new byte[256];
		System.arraycopy(src, 0, newBytes, 0, 16);

		byte[] nameBytes = nameSecret.getBytes();
		System.arraycopy(nameBytes, 0, newBytes, 16, nameBytes.length);

		return toMd5(newBytes, 16 + nameBytes.length);
	}

	private String getResponse(String pwd, String newChallenge) {
		newChallenge = newChallenge.toUpperCase();
		byte[] src = ASC2Hex(newChallenge.toCharArray());

		byte[] newBytes = new byte[256];
		newBytes[0] = 0;

		byte[] pwdBytes = pwd.getBytes();
		System.arraycopy(pwdBytes, 0, newBytes, 1, pwdBytes.length);

		System.arraycopy(src, 0, newBytes, pwdBytes.length + 1, 16);
		return toMd5(newBytes, 16 + pwdBytes.length + 1);
	}

	private String toMd5(byte[] bytes, int len) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes, 0, len);

			byte[] result = algorithm.digest();
			StringBuilder hexString = new StringBuilder();

			for (byte b : result) {
				String hex = Integer.toHexString(b & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String loginHub(String name, String pwd, String url) {
		String content, newChanllenge, response;
		String challenge = getChallenage();
        Logger.debug(TAG, "loginHub: start to loginHub" );

		if (TextUtils.isEmpty(challenge)) {
			return Constants.LOGIN_ERROR_MESSAGE;
		}

		newChanllenge = getNewChallenage("591wificom", challenge);
		response = getResponse(pwd, newChanllenge);
		response = response.toLowerCase();
        Logger.debug(TAG, "loginHub: start to loginHub1" );

		content = String.format(Locale.getDefault(), LOGIN_FMT, mServer, mPort,
				name, response, url, mServer, mPort);

		String retStr = talkWithServer(mServer, mPort, content);
        Logger.debug(TAG, "loginHub: start to loginHub2" );

		String checkRet = checkLogin(retStr);
//		if (checkRet != null) {
//			Toast.makeText(mContext, checkRet, 5000).show();
//			return false;
//		}

//		talkWithServer(mServer, mPort, PRELOGIN);

		return checkRet;
	}

	private String checkLogin(String strReturn) {
	    Logger.debug(TAG, "strReturn: " + strReturn);
	    if(strReturn == null) {
	        return Constants.LOGIN_ERROR_MESSAGE;
	    }
		int httpIndex = strReturn.indexOf("Location:");
		if (httpIndex < 0)
			return Constants.LOGIN_ERROR_MESSAGE;
		else {
			int resIndex = strReturn.indexOf("res=", httpIndex);
			if (resIndex < 0) {
				return Constants.LOGIN_ERROR_MESSAGE;
			}
			String strFalse = strReturn.substring(resIndex + 4, resIndex + 10);
			if (strFalse.compareToIgnoreCase("failed") == 0) {
				int reasonEnd = strReturn.indexOf("&", resIndex + 11);
				// reason = strReturn.substring(resIndex + 17, reasonEnd -
				// resIndex - 17);
				String strReplyStart = "<ReplyMessage>";
				String strReplyEnd = "</ReplyMessage>";
				int messageIndex = strReturn.indexOf(strReplyStart, reasonEnd
						- resIndex - 17);
				if (messageIndex > 0) {
					int messageEndIndex = strReturn.indexOf(strReplyEnd,
							messageIndex);
					return strReturn.substring(
							messageIndex + strReplyStart.length(),
							messageEndIndex);
				}
				return Constants.LOGIN_ERROR_MESSAGE;
			}
		}
		return null;
	}

	public void logoutHub() {
	    Logger.debug(TAG, "LogoutHub");
		String content = String.format(LOGOUT_FMT, mServer);
		String gateway = Utils.getGateway(mContext);
		String [] res = gateway.split(":");
		if (res.length != 2)
		{
			return;
		}
	    Logger.debug(TAG, "server: " + res[0]);
	    Logger.debug(TAG, "port: " + res[1]);

		talkWithServer(res[0], Integer.valueOf(res[1]), content);
	}
}
