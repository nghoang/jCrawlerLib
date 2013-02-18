package com.ngochoang.CrawlerLib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ice.jni.registry.RegStringValue;
import com.ice.jni.registry.Registry;
import com.ice.jni.registry.RegistryKey;
import com.ice.jni.registry.RegistryValue;

public class Utilities {

	public static boolean IsFileExist(String p) {
		boolean exists = (new File(p)).exists();
		if (exists) {
			return true;
		} else {
			return false;
		}
	}
	
	public static double round(double unrounded, int precision, int roundingMode)
	{
	    BigDecimal bd = new BigDecimal(unrounded);
	    BigDecimal rounded = bd.setScale(precision, roundingMode);
	    return rounded.doubleValue();
	}
	
	public static String OnlineCaptchaSolving(String filename)
	{
		WebClientX client = new WebClientX();
		Vector<NameValuePair> prs = new Vector<NameValuePair>();
		prs.add(new BasicNameValuePair("i2ocr_options", "file"));
		prs.add(new BasicNameValuePair("i2ocr_url", "http://"));
		prs.add(new BasicNameValuePair("i2ocr_languages", "gb"));
		Vector<PostFileData> files = new Vector<PostFileData>();
		files.add(new PostFileData("i2ocr_uploadedfile", "/Users/hoangong/projects/Java/Bet/" + filename));
		String content = client.PostMethod("http://www.sciweavers.org/process_form_i2ocr", prs, files);
		content = Utilities.SimpleRegexSingle("#ocrTextBox\"\\).val\\(\"(.*?)(?=\"\\);\\$\\(\"#ocrTextBox\")", content, 1);
		return "";
	}
	
	public static boolean isWindows() {
		 
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		String os = System.getProperty("os.name").toLowerCase();
		// Mac
		return (os.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		String os = System.getProperty("os.name").toLowerCase();
		// linux or unix
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
 
	}
 
	public static boolean isSolaris() {
 
		String os = System.getProperty("os.name").toLowerCase();
		// Solaris
		return (os.indexOf("sunos") >= 0);
 
	}

	public static void SetSetting(String settingFile, String name, String value) {
		String content = readFileAsString(settingFile);
		String[] lines = content.split("\n");
		Boolean is_modifed = false;
		for (int i = 0; i < lines.length; i++) {
			String set_name = SimpleRegexSingle("([^=]*)", lines[i].trim(), 1);
			if (set_name.equals(name)) {
				lines[i] = name + "=" + value;
				is_modifed = true;
				break;
			}
		}
		content = "";
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].trim().equals(""))
				continue;
			content += lines[i].trim() + "\n";
		}
		if (is_modifed == false)
			content += name + "\n" + value + "\n";
		WriteFile(settingFile, content, false);
	}

	public static String GetSetting(String settingFile, String name) {
		String content = readFileAsString(settingFile);
		String[] lines = content.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String set_name = SimpleRegexSingle("([^=]*)=(.*)",
					lines[i].trim(), 1);
			String set_value = SimpleRegexSingle("([^=]*)=(.*)",
					lines[i].trim(), 2);
			if (set_name.equals(name)) {
				return set_value.trim();
			}
		}
		return "";
	}

	public static boolean IsNumber(String input) {
		try {
			Double i = Double.parseDouble(input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void WriteRegistry(String path, String name, String value) {
		try {
			Registry reg = new Registry();
			RegistryKey key = null;
			if (path.startsWith("HKEY_LOCAL_MACHINE\\")) {
				path = path.replace("HKEY_LOCAL_MACHINE\\", "");
				key = reg.openSubkey(reg.HKEY_LOCAL_MACHINE, path,
						RegistryKey.ACCESS_ALL);
			} else if (path.startsWith("HKEY_LOCAL_MACHINE\\")) {
				path = path.replace("HKEY_CURRENT_USER\\", "");
				key = reg.openSubkey(reg.HKEY_CURRENT_USER, path,
						RegistryKey.ACCESS_ALL);
			}
			RegStringValue keyvalue = new RegStringValue(key, name,
					RegistryValue.REG_SZ);
			keyvalue.setData(value);
			key.setValue(keyvalue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String ReadRegistry(String path, String name) {
		try {
			Registry reg = new Registry();
			RegistryKey key = null;
			if (path.startsWith("HKEY_LOCAL_MACHINE\\")) {
				path = path.replace("HKEY_LOCAL_MACHINE\\", "");
				key = reg.openSubkey(reg.HKEY_LOCAL_MACHINE, path,
						RegistryKey.ACCESS_ALL);
			} else if (path.startsWith("HKEY_LOCAL_MACHINE\\")) {
				path = path.replace("HKEY_CURRENT_USER\\", "");
				key = reg.openSubkey(reg.HKEY_CURRENT_USER, path,
						RegistryKey.ACCESS_ALL);
			}
			// = new RegStringValue(key,value,RegistryValue.REG_SZ);
			RegistryValue keyvalue = key.getValue(name);
			return new String(keyvalue.getByteData());// String.valueOf();
		} catch (Exception ex) {
			// ex.printStackTrace();
			return "";
		}
		// String value = null;
		// try {
		// at.jta.Regor reg = new at.jta.Regor();
		// at.jta.Key key = new at.jta.Key();
		// key.setPath(path);
		// if (key._isValidKey()) {
		// value = reg.readAnyValueString(key, name);
		// }
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// if (value == null) {
		// return "";
		//
		// }
		// return (value);
	}

	public static long GetWorldTime() {
		try {
			WebClientX client = new WebClientX();
			String content = client
					.GetMethod("http://www.unixtimestamp.com/index.php");
			content = Utilities.SimpleRegexSingle(
					"<font color=\"#CC0000\">([^<]*)</font>", content, 1);
			return Long.parseLong(content);
		} catch (Exception ex) {
			// ex.printStackTrace();
			return 0;
		}
	}

	public static long CurrentUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}

	public static long CurrentUnixTimeMili() {
		return System.currentTimeMillis();
	}

	public static boolean CompareApproximate(String group1, String group2) {
		boolean res = false;
		group1 = Utilities.RemoveSpace(group1.replaceAll("[^a-zA-Z0-9]", " ")
				.toLowerCase());
		group2 = Utilities.RemoveSpace(group2.replaceAll("[^a-zA-Z0-9]", " ")
				.toLowerCase());

		String[] terms1 = group1.split(" ");
		String[] terms2 = group2.split(" ");

		res = true;
		for (String term : terms1) {
			if (group2.indexOf(term) == -1) {
				res = false;
				break;
			}
		}

		if (res == false) {
			res = true;
			for (String term : terms2) {
				if (group1.indexOf(term) == -1) {
					res = false;
					break;
				}
			}
		}

		return res;
	}

	public static String md5(String i) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(i.getBytes(Charset.forName("UTF8")));
			byte[] resultByte = messageDigest.digest();
			return new String(Hex.encodeHex(resultByte));
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public static String HTMLDecode(String input) {
		String res = org.apache.commons.lang.StringEscapeUtils
				.unescapeHtml(input);
		return res;
	}

	public static String GetFileExt(String file) {
		String ext = "";
		int mid = file.lastIndexOf(".");
		ext = file.substring(mid + 1, file.length());
		return ext.toLowerCase();
	}

	public static String HTMLEncode(String input) {
		String res = org.apache.commons.lang.StringEscapeUtils
				.escapeHtml(input);
		return res;
	}

	public static String now(String format) {
		// format: yyyy-MM-dd HH:mm:ss
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());

	}

	public static String ReadAttrXPath(String file, String path, String attr) {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(file));
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(path);
			Element result = (Element) expr.evaluate(doc, XPathConstants.NODE);
			if (result == null) {
				return "";
			}
			return result.getAttribute(attr);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public static String ReadStringXPath(String file, String path) {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(file));

			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(path);
			String result = (String) expr.evaluate(doc, XPathConstants.STRING);
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public static Vector<String> ReadMulAttrXPath(String file, String path,
			String attr) {

		Vector<String> res = new Vector<String>();
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(file));
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(path);
			NodeList result = (NodeList) expr.evaluate(doc,
					XPathConstants.NODESET);
			for (int i = 0; i < result.getLength(); i++) {
				Element item = (Element) result.item(i);
				res.add(item.getAttribute(attr));
			}
			return res;
		} catch (Exception ex) {
			ex.printStackTrace();
			return res;
		}
	}

	public static Vector<String> ReadMulStringXPath(String file, String path) {
		Vector<String> res = new Vector<String>();
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(file));

			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(path);
			NodeList result = (NodeList) expr.evaluate(doc,
					XPathConstants.NODESET);
			for (int i = 0; i < result.getLength(); i++) {
				Element item = (Element) result.item(i);
				res.add(item.getTextContent());
			}
			return res;
		} catch (Exception ex) {
			ex.printStackTrace();
			return res;
		}
	}

	public static String readFileAsString(String filePath) {
		try {
			if (!IsFileExist(filePath)) {
				return "";
			}
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String ReadSetting(String settingFile, String settingname) {
		if (IsFileExist(settingFile + ".xml") == false) {
			WriteFile(settingFile + ".xml", "<settings></settings>", false);
		}
		return ReadStringXPath(settingFile + ".xml", "//settings/"
				+ settingname + "/text()");
	}

	public static String stripNonValidXMLCharacters(String inString) {
		if (null == inString) {
			return null;
		}
		byte[] byteArr = inString.getBytes();
		for (int i = 0; i < byteArr.length; i++) {
			byte ch = byteArr[i];
			// remove any characters outside the valid UTF-8 range as well as
			// all control characters
			// except tabs and new lines
			if (!((ch > 31 && ch < 253) || ch == '\t' || ch == '\n' || ch == '\r')) {
				byteArr[i] = ' ';
			}
		}
		return new String(byteArr);
	}

	public static Vector<String> SimpleRegex(String pattern, String content,
			int group) {
		Vector<String> ret = new Vector<String>();
		Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
		Matcher m = p.matcher(content);
		while (m.find()) {
			if (m.group(group) != null) {
				ret.add(m.group(group));
			}
		}
		return ret;
	}

	public static Vector<String> SimpleRegex(String pattern, String content,
			int group, int options) {
		Vector<String> ret = new Vector<String>();
		Pattern p = Pattern.compile(pattern, options);
		Matcher m = p.matcher(content);
		while (m.find()) {
			if (m.group(group) != null) {
				if (!m.group(group).equals("")) {
					ret.add(m.group(group));
				}
			}
		}
		return ret;
	}

	public static String RemoveSpace(String content) {
		content = content.replaceAll("\t", " ");
		while (content.indexOf("  ") >= 0) {
			content = content.replace("  ", " ");
		}
		content = content.trim();
		if (content.equals(" ")) {
			return "";
		}
		return content;
	}

	public static String URLDecode(String input) {
		try {
			return java.net.URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return "";
		}
	}

	public static String GetAbsoluteUrl(String domain, String lastReq,
			String url) {
		String res = "";
		if (url.startsWith(domain)) {
			res = url;
		} else if (url.startsWith("/")) {
			res = domain + url;
		} else if (url.startsWith("?")) {
			if (lastReq.indexOf("?") > 0) {
				lastReq = lastReq.substring(0, lastReq.indexOf("?"));
			}
			res = lastReq + url;
		}
		return res;
	}

	public static String SimpleRegexSingle(String pattern, String content,
			int group) {

		return SimpleRegexSingle(pattern, content, group, Pattern.DOTALL);
	}

	public static String SimpleRegexSingle(String pattern, String content,
			int group, int ptn) {
		String ret = "";
		Pattern p = Pattern.compile(pattern, ptn);
		Matcher m = p.matcher(content);
		if (m.find()) {
			if (m.groupCount() >= group) {
				ret = m.group(group);
			}
		}
		if (ret == null)
			return "";
		return ret;
	}

	public static String GetTextBetweenSingle(String begin, String end,
			String content, boolean inner) {
		Vector<String> t = GetTextBetween(begin, end, content, inner);
		if (t.size() > 0) {
			return t.get(0);
		} else {
			return "";
		}
	}

	public static Vector<String> GetTextBetween(String begin, String end,
			String content, boolean inner) {
		Vector<String> res = new Vector<String>();
		while (true) {
			int beginPos = content.indexOf(begin);
			if (beginPos < 0) {
				break;
			}
			if (inner == true) {
				beginPos += begin.length();
			}
			content = content.substring(beginPos, content.length());
			int endPos = content.indexOf(end);
			if (endPos < 0) {
				break;
			}
			endPos = content.indexOf(end) + end.length();
			if (inner == true) {
				endPos -= end.length();
			}
			res.add(content.substring(0, endPos));
			content = content.substring(endPos, content.length());
		}
		return res;
	}

	public static void WriteLog(String log) {
		WriteLog(log, -1);
	}

	public static void WriteLog(String log, int nbLines) {
		if (nbLines == -1) {
			WriteFile("log.txt", getDateTime() + " >> " + log + "\n", true);
		} else {
			try {
				FileInputStream fstream = new FileInputStream("log.txt");
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String strLine;
				Vector<String> totalLines = new Vector<String>();
				while ((strLine = br.readLine()) != null) {
					totalLines.add(strLine);
				}
				if (totalLines.size() > nbLines) {
					String content = "";
					for (int i = totalLines.size() - nbLines; i <= totalLines
							.size() - 1; i++) {
						content += totalLines.get(i) + "\n";
					}
					WriteFile("log.txt", content, false);
				}
				in.close();
			} catch (Exception e) {
				Utilities.WriteLogTrace(e);
			}
			WriteLog(log);
		}
	}

	public static void WriteLogTrace(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			WriteLog(sw.toString());
		} catch (Exception e2) {
			WriteLog("Error in writing log trace");
		}
	}

	public static void WriteXML(String file, Document doc, boolean append)
			throws Exception {
		// set up a transformer
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// create string from xml tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		String xmlString = sw.toString();

		WriteFile(file, xmlString, append);
	}

	public static void WriteFile(String file, String content, boolean append) {
		try {
			FileWriter fstream = new FileWriter(file, append);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static byte[] ReadBytesFromFile(String filename) {
		try {
			File file = new File(filename);
			InputStream is = new FileInputStream(file);
			long length = file.length();
			byte[] bytes = new byte[(int) length];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "
						+ file.getName());
			}
			is.close();
			return bytes;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void WriteFile(String file, byte[] content) {
		try {
			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			fos.write(content);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean SendEmail(String subject, String content,
			String SendTo, String CCList, String BCCList, String SendFrom,
			String AttachmentSource, String AttachmentFile,
			String SMTPusername, String SMTPpassword, String secureType,
			String SMTPHost, String SMTPPort) {

		Session session = null;

		if (secureType.equals("TLS")) {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			session = Session.getInstance(props);
		} else if (secureType.equals("SSL")) {
			Properties props = new Properties();
			props.put("mail.smtp.host", SMTPHost);
			props.put("mail.smtp.socketFactory.port", SMTPPort);
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", SMTPPort);

			final String SMTPUsername = SMTPusername;
			final String SMTPPassword = SMTPpassword;

			session = Session.getDefaultInstance(props,
					new javax.mail.Authenticator() {

						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(SMTPUsername,
									SMTPPassword);
						}
					});
		}

		if (session == null) {
			return false;
		}

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(SendFrom));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(SendTo));
			if (!CCList.equals("")) {
				message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(CCList));
			}
			if (!BCCList.equals("")) {
				message.setRecipients(Message.RecipientType.BCC,
						InternetAddress.parse(BCCList));
			}

			message.setSubject(subject);

			if (!AttachmentFile.equals("")) {
				// Create the message part
				BodyPart messageBodyPart = new MimeBodyPart();
				// Fill the message
				messageBodyPart.setText(content);
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				// Part two is attachment
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(AttachmentSource);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(AttachmentFile);
				multipart.addBodyPart(messageBodyPart);
				// Put parts in message
				message.setContent(multipart);
			} else {
				message.setText(content);
			}

			if (secureType.equals("TLS")) {
				Transport transport = session.getTransport("smtp");
				transport.connect(SMTPHost, Integer.parseInt(SMTPPort),
						SMTPusername, SMTPpassword);
			}

			Transport.send(message);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private static String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public String URLEncode(String input) {
		return EncodeQuery(input);
	}

	public static String EncodeQuery(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public static String GetPlainText(String input) {
		input = input.replace("-->", "");
		input = input.replace("<!--", "");
		return input.replaceAll("\\<[^>]*>|\\t|\\n|\\r", " ").trim();
	}

	public static String GetLocationInfo(String name, String content) {
		String res = "";
		Pattern p = Pattern.compile("\"" + name + "\": \"([^\"]*)\"");
		Matcher m = p.matcher(content);
		if (m.find()) {
			res = m.group(1);
		} else if (res.equals("")) {
			p = Pattern.compile("\"" + name + "\" : \"([^\"]*)\"");
			m = p.matcher(content);
			if (m.find()) {
				res = m.group(1);
			}
		}
		return res;
	}

	public static String Base64Encode(String input) {
		Base64 b = new Base64();
		return b.encodeToString(input.getBytes());
	}

	public static String Base64Decode(String input) {
		Base64 b = new Base64();
		return new String(b.decode(input));
	}

	public static void WriteSetting(String file, String name, String value) {
		try {
			if (IsFileExist(file + ".xml") == false) {
				WriteFile(file + ".xml", "<settings></settings>", false);
			}
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(file + ".xml"));
			NodeList list = doc.getElementsByTagName(name);
			if (list.getLength() > 0) {
				Node node = (Node) doc.createElement(name);
				node.setTextContent(value);
				doc.getElementsByTagName("settings").item(0)
						.replaceChild(node, list.item(0));
			} else {
				Element node = doc.createElement(name);
				node.setTextContent(value);
				doc.getElementsByTagName("settings").item(0).appendChild(node);
			}
			Utilities.WriteXML(file + ".xml", doc, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Vector<String> GetFileInFolder(String folder) {
		folder = folder.replace("\\", "/");
		folder = folder.replaceAll("/$", "");
		File dir = new File(folder);
		Vector<String> files = new Vector<String>();
		String[] children = dir.list();
		for (String c : children) {
			if (new File(folder + "/" + c).isFile() == true) {
				files.add(c);
			}
		}
		return files;
	}

	public static String RandomString(String characters, int length) {
		Random rng = new Random();
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	public static void CreateFolderIfNotExist(String folderPath) {
		File folder = new File(folderPath);
		if (folder.exists() == false) {
			folder.mkdirs();
		}
	}

	public static void DeleteFolder(File f) {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				DeleteFolder(c);
		}
		if (!f.delete())
			try {
				throw new FileNotFoundException("Failed to delete file: " + f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
