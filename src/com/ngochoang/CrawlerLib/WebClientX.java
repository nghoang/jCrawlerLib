package com.ngochoang.CrawlerLib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ngochoang.captcha.CaptchaForm;
import com.ngochoang.captcha.ICaptchForm;
import com.ngochoang.crawlerinterface.IWebClientX;

public class WebClientX implements ICaptchForm{

	public IWebClientX callback;
	String captcha_id = "";
	String curDir = "";
	String lastRequest = "";
	public Boolean ignore_cookie = false;
	DefaultHttpClient httpclient;
	Vector<String> HeaderName = new Vector<String>();
	Vector<String> HeaderValue = new Vector<String>();
	Header[] responseHeader;
	String proxyList = "";
	private boolean CacheEnable = false;
	private String proxyCheckPage = "";
	private String proxyChecktext = "";
	private int CacheExpireDays = 1;
	private int CacheExpireHours = 0;
	byte[] contentByte;
	private boolean isLoadedProxy = false;
	private String proxyType = "all"; // all http socks
	private String cacheFile = curDir + "\\caches";
	private boolean auto_redirect = false;
	private boolean is_multi_threading = false;

	public void EnableMultiThreading() {
		is_multi_threading = true;
	}

	public void DisableMultiThreading() {
		is_multi_threading = false;
	}

	public void EnableLogging() {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
				"true");
		System.setProperty(
				"org.apache.commons.logging.simplelog.log.httpclient.wire.header",
				"debug");
		System.setProperty(
				"org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
				"debug");
	}

	public static String FixUrl(String url) {
		if (url.toLowerCase().startsWith("http://") == false
				&& url.toLowerCase().startsWith("https://") == false)
			url = "http://" + url;

		String slash_check = Utilities.SimpleRegexSingle("http[s]*://(.*)",
				url.toLowerCase(), 1);
		if (slash_check.indexOf("/") == -1)
			url = url + "/";
		return url;
	}

	public static CookieStore SetCookieByString(String cookiestr, String domain) {
		CookieStore cookie = new BasicCookieStore();
		String qr = "^([^=]+)=(.*)";
		for (String t : cookiestr.split(";")) {
			BasicClientCookie2 c;
			c = new BasicClientCookie2(Utilities.SimpleRegexSingle(qr,
					t.trim(), 1).trim(), Utilities.SimpleRegexSingle(qr,
					t.trim(), 2).trim());
			c.setDomain(domain);
			c.setPath("/");
			cookie.addCookie(c);
		}
		return cookie;
	}

	public void SetRedirect(boolean r) {
		auto_redirect = r;
		HttpParams params = httpclient.getParams();
		params.setParameter("http.protocol.handle-redirects", r);
		if (is_multi_threading == true) {
			PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
			httpclient = new DefaultHttpClient(connectionManager, params);
		} else {
			httpclient = new DefaultHttpClient(params);
		}
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy() {
			public boolean isRedirected(HttpRequest request,
					HttpResponse response, HttpContext context) {
				boolean isRedirect = false;
				try {
					isRedirect = super.isRedirected(request, response, context);
				} catch (ProtocolException e) {
					e.printStackTrace();
				}
				if (!isRedirect) {
					int responseCode = response.getStatusLine().getStatusCode();
					if (responseCode == 301 || responseCode == 302) {
						return true;
					}
				}
				if (auto_redirect && isRedirect)
					return true;
				else
					return false;
			}
		});
	}

	public static Vector<NameValuePair> QueryStringToPostParams(String query) {
		Vector<NameValuePair> params = new Vector<NameValuePair>();
		String[] temp = query.split("&");
		String qr = "^([^=]+)=(.*)";
		for (String t : temp) {
			params.add(new BasicNameValuePair(Utilities.SimpleRegexSingle(qr,
					t, 1), Utilities.HTMLDecode(Utilities.SimpleRegexSingle(qr,
					t, 2))));
		}
		return params;
	}

	public String GetLastRequest() {
		return lastRequest;
	}

	public void SetCurrentPath(String p) {
		curDir = p;
	}

	public WebClientX(IWebClientX cb) {
		InitHeader();
		callback = cb;
	}

	public void SetProxyCheckInfo(String page, String text) {
		proxyCheckPage = page;
		proxyChecktext = text;
	}

	public void SetCacheFile(String file) {
		cacheFile = curDir + "\\" + file;
	}

	public void SetProxyType(String type) {
		proxyType = type;
	}

	public void DownloadFile(String url, String file) {
		try {
			HttpGet httpget = new HttpGet(url);
			for (int i = 0; i < HeaderName.size(); i++) {
				httpget.addHeader(HeaderName.get(i), HeaderValue.get(i));
			}
			LoadProxy();
			HttpResponse response = httpclient.execute(httpget);
			contentByte = EntityUtils.toByteArray(response.getEntity());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(contentByte);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.getLogger(WebClientX.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public void SetTimeout(int timeoutConnection, int timeoutSocket) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		if (is_multi_threading == true) {
			PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
			httpclient = new DefaultHttpClient(connectionManager, httpParameters);
		} else {
			httpclient = new DefaultHttpClient(httpParameters);
		}
	}

	public void SetCacheExpireDays(int days, int hours) {
		CacheExpireDays = days;
		CacheExpireHours = hours;
	}

	public void SetProxyList(String pxl) {
		isLoadedProxy = false;
		proxyList = pxl;
	}

	public byte[] GetContentByte() {
		return contentByte;
	}

	private void InitHeader() {
		HeaderName.add("User-Agent");
		HeaderValue
				.add("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
	}

	public WebClientX() {
		InitHeader();
		if (is_multi_threading == true) {
			PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
			httpclient = new DefaultHttpClient(connectionManager);
		} else {
			httpclient = new DefaultHttpClient();
		}
		httpclient
				.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
						0, false));
	}

	public void AddHeader(String name, String value) {
		for (int i = 0; i < HeaderName.size(); i++) {
			if (HeaderName.get(i).toLowerCase().equals(name.toLowerCase())) {
				HeaderValue.set(i, value);
				return;
			}
		}
		HeaderName.add(name);
		HeaderValue.add(value);
	}

	public void DeleteHeader(String name) {
		for (int i = 0; i < HeaderName.size(); i++) {
			if (HeaderName.get(i).toLowerCase().equals(name.toLowerCase())) {
				HeaderValue.remove(i);
				HeaderName.remove(i);
				return;
			}
		}
	}

	public Header[] GetRequestHeader() {
		Vector<BasicHeader> hs = new Vector<BasicHeader>();
		for (int i = 0; i < HeaderName.size(); i++) {
			BasicHeader h = new BasicHeader(HeaderName.get(i),
					HeaderValue.get(i));
			hs.add(h);
		}
		Header[] res = new Header[hs.size()];
		for (int i = 0; i < HeaderName.size(); i++) {
			res[i] = hs.get(i);
		}
		return res;
	}

	public String GetMethod(String url) {
		url = FixUrl(url);
		lastRequest = url;
		String responseBody = "";
		String resCache = "";
		String filename = org.apache.commons.codec.digest.DigestUtils
				.md5Hex(url);
		if (isCacheEnable() == true) {
			try {
				CacheInit();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Load Cache Error");
				return "";
			}
			resCache = Utilities.ReadAttrXPath(cacheFile + ".xml",
					"//caches/request[@file='" + filename + "']", "file");
		}
		if (resCache.equals("")) {
			boolean isGzip = false;

			HttpGet httpget = new HttpGet(url);
			if (ignore_cookie == true)
				httpget.getParams().setParameter(ClientPNames.COOKIE_POLICY,
						CookiePolicy.IGNORE_COOKIES);
			else
				httpget.getParams().setParameter(ClientPNames.COOKIE_POLICY,
						CookiePolicy.NETSCAPE);
			for (int i = 0; i < HeaderName.size(); i++) {
				httpget.addHeader(HeaderName.get(i), HeaderValue.get(i));
			}
			responseHeader = new Header[0];
			try {
				LoadProxy();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Load Proxy Error");
				return "";
			}
			HttpResponse response;
			try {
				response = httpclient.execute(httpget);
			} catch (Exception ex) {
				if (is_multi_threading == true)
					httpget.releaseConnection();
				if (callback != null)
					callback.DropConnection(url);
				else
					ex.printStackTrace();
				return "";
			}

			try {
				responseHeader = response.getAllHeaders();
				for (int g = 0; g < responseHeader.length; g++) {
					if (responseHeader[g].getName().equals("Content-Encoding")
							&& responseHeader[g].getValue().toLowerCase()
									.contains("gzip")) {
						isGzip = true;
					}
				}
				contentByte = EntityUtils.toByteArray(response.getEntity());

				if (is_multi_threading == true)
					httpget.releaseConnection();
				if (isGzip == true) {
					responseBody = GzipByteToString(contentByte);
				} else {
					responseBody = new String(contentByte, "UTF8");
				}
				if (isCacheEnable() == true) {
					boolean exists = (new File(cacheFile)).exists();
					if (exists == false) {
						(new File(cacheFile)).mkdir();
					}
					Utilities
							.WriteFile(cacheFile + "/" + filename, contentByte);
					WriteCache(url, contentByte.length, null, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Process response error");
			}
		} else {
			try {
				responseBody = LoadCache(filename);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Load Cache Error");
			}
		}
		return responseBody;
	}

	public String GzipByteToString(byte[] contentByte) {
		GZIPInputStream in;
		try {
			in = new GZIPInputStream(new ByteArrayInputStream(contentByte));

			int len;
			StringBuffer sb_result = new StringBuffer();
			byte[] buffer = new byte[512];
			while ((len = in.read(buffer)) > 0) {
				sb_result.append(new String(buffer, 0, len));
			}
			return sb_result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}

	public String PostMethod(String url, List<NameValuePair> formparams,
			Vector<PostFileData> uploadFiles) {
		return PostMethod(url, formparams, uploadFiles, "");
	}

	public String PostMethod(String url, List<NameValuePair> formparams,
			Vector<PostFileData> uploadFiles, String REST) {
		try {
			String responseBody = "";
			String resCache = "";
			String post = "";
			url = url.trim();
			lastRequest = url;

			if (formparams != null) {
				for (NameValuePair f : formparams) {
					post += f.getName() + "=" + f.getValue();
				}
			}

			String filename = org.apache.commons.codec.digest.DigestUtils
					.md5Hex(url + post);
			if (isCacheEnable() == true) {
				CacheInit();
				resCache = Utilities.ReadAttrXPath(cacheFile + ".xml",
						"//caches/request[@file='" + filename + "']", "file");
			}
			if (resCache.equals("")) {
				boolean isGzip = false;

				UrlEncodedFormEntity entity = null;
				if (formparams != null)
					entity = new UrlEncodedFormEntity(formparams, "UTF-8");
				HttpPost httppost = new HttpPost(url);
				if (ignore_cookie == true)
					httppost.getParams().setParameter(
							ClientPNames.COOKIE_POLICY,
							CookiePolicy.IGNORE_COOKIES);
				else
					httppost.getParams().setParameter(
							ClientPNames.COOKIE_POLICY, CookiePolicy.NETSCAPE);
				for (int i = 0; i < HeaderName.size(); i++) {
					httppost.addHeader(HeaderName.get(i), HeaderValue.get(i));
				}
				MultipartEntity multiPost = new MultipartEntity();
				if (uploadFiles != null) {
					for (PostFileData uf : uploadFiles) {
						ByteArrayBody cbFile = new ByteArrayBody(
								Utilities.ReadBytesFromFile(uf.getFileName()),
								uf.getFieldName());
						multiPost.addPart(uf.getFieldName(), cbFile);
					}
					for (NameValuePair p : formparams) {
						multiPost.addPart(p.getName(),
								new StringBody(p.getValue()));
					}
					httppost.setEntity(multiPost);
				} else if (REST.equals("") == false) {
					httppost.setHeader("Content-Type", "application/json");
					httppost.setEntity(new StringEntity(REST, "UTF-8"));
				} else if (entity != null) {
					httppost.setEntity(entity);
				}
				LoadProxy();
				HttpResponse response;
				try {
					response = httpclient.execute(httppost);
				} catch (Exception ex) {
					if (is_multi_threading == true)
						httppost.releaseConnection();
					if (callback != null)
						callback.DropConnection(url);
					else
						ex.printStackTrace();
						
					return "";
				}
				responseHeader = response.getAllHeaders();
				for (int g = 0; g < responseHeader.length; g++) {
					if (responseHeader[g].getName().equals("Content-Encoding")
							&& responseHeader[g].getValue().toLowerCase()
									.contains("gzip")) {
						isGzip = true;
					}
				}
				contentByte = EntityUtils.toByteArray(response.getEntity());
				if (is_multi_threading == true)
					httppost.releaseConnection();
				
				if (isGzip == true) {
					responseBody = GzipByteToString(contentByte);
				} else {
					responseBody = new String(contentByte, "UTF8");
				}
				// responseBody = new String(contentByte, "UTF8");//
				// EntityUtils.toString(responseEnt,
				// "utf-8");
				if (isCacheEnable() == true) {
					boolean exists = (new File(cacheFile)).exists();
					if (exists == false) {
						(new File(cacheFile)).mkdir();
					}
					Utilities
							.WriteFile(cacheFile + "/" + filename, contentByte);
					WriteCache(url, contentByte.length, formparams,
							responseHeader);
				}
			} else {
				responseBody = LoadCache(filename);
			}
			return responseBody;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public String PostMethod(String url, List<NameValuePair> formparams) {
		url = FixUrl(url);
		return PostMethod(url, formparams, null);
	}

	private String LoadCache(String filename) throws Exception {
		String responseBody = Utilities.readFileAsString(cacheFile + "/"
				+ filename);
		contentByte = Utilities.ReadBytesFromFile(cacheFile + "/" + filename);
		HeaderName.clear();
		HeaderValue.clear();

		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new File(cacheFile + ".xml"));
		XPath xpath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xpath.compile("//caches/request");
		NodeList request_items = (NodeList) expr.evaluate(doc,
				XPathConstants.NODESET);
		boolean isWrite = false;
		for (int i = 0; i < request_items.getLength(); i++) {
			Element item = (Element) request_items.item(i);
			String req_time = item.getAttribute("request_time");
			Calendar d2 = Calendar.getInstance();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar d1 = Calendar.getInstance();
			d1.setTime(sdf.parse(req_time));
			if (CacheExpireDays > 0) {
				d1.add(Calendar.DATE, CacheExpireDays);
			}
			if (CacheExpireHours > 0) {
				d1.add(Calendar.HOUR, CacheExpireHours);
			}

			if (d1.before(d2)) {
				isWrite = true;
				String req_file = item.getAttribute("file");
				new File(cacheFile + "/" + req_file).delete();
				doc.getElementsByTagName("caches").item(0)
						.removeChild(request_items.item(i));
			}
		}

		if (isWrite) {
			Utilities.WriteXML(cacheFile + ".xml", doc, false);
		}

		return responseBody;
	}

	private void CacheInit() throws Exception {
		boolean exists = (new File(cacheFile + ".xml")).exists();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		if (exists == false) {
			Document doc = builder.newDocument();
			Element root = doc.createElement("caches");
			doc.appendChild(root);
			Utilities.WriteXML(cacheFile + ".xml", doc, false);
		}
	}

	private void WriteCache(String url, int size,
			List<NameValuePair> formparams, Header[] resHeader)
			throws Exception {
		CacheInit();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new File(cacheFile + ".xml"));

		Element request = doc.createElement("request");

		String post = "";

		if (formparams != null) {
			for (NameValuePair f : formparams) {
				post += f.getName() + "=" + f.getValue();
			}
		}

		String filename = org.apache.commons.codec.digest.DigestUtils
				.md5Hex(url + post);

		request.setAttribute("file", filename);
		request.setAttribute("url", url);
		request.setAttribute("request_time",
				Utilities.now("yyyy-MM-dd HH:mm:ss"));

		doc.getElementsByTagName("caches").item(0).appendChild(request);

		Utilities.WriteXML(cacheFile + ".xml", doc, false);
	}

	public void SetCookie(CookieStore cookieStore) {
		httpclient.setCookieStore(cookieStore);
	}

	public CookieStore GetCookieStore() {
		return httpclient.getCookieStore();
	}

	public Header[] GetResponseHeader() {
		return responseHeader;
	}

	public void setCacheEnable(boolean cacheEnable) {
		CacheEnable = cacheEnable;
	}

	public boolean isCacheEnable() {
		return CacheEnable;
	}

	public void SetProxyForClient(String px, String type) {
		px = px.trim();
		String parseIp = "([^:]*):([^:]*):{0,1}([^:]*):{0,1}([^:]*)";
		String px_ip = Utilities.SimpleRegexSingle(parseIp, px, 1);
		String px_port = Utilities.SimpleRegexSingle(parseIp, px, 2);
		String px_un = Utilities.SimpleRegexSingle(parseIp, px, 3);
		String px_pass = Utilities.SimpleRegexSingle(parseIp, px, 4);

		if (!px_un.equals("")) {
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(px_ip, Integer.parseInt(px_port)),
					new UsernamePasswordCredentials(px_un, px_pass));
		}

		if (type.equals("socks")) {
			httpclient.getParams().setParameter("socks.host", px_ip);
			httpclient.getParams().setParameter("socks.port",
					Integer.parseInt(px_port));
			httpclient
					.getConnectionManager()
					.getSchemeRegistry()
					.register(
							new Scheme("http", 80,
									new CustomSchemeSocketFactory()));

		} else if (type.equals("http")) {
			HttpHost proxy = new HttpHost(px_ip, Integer.parseInt(px_port),
					"http");
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}
	}

	public void DebugFiddler() {
		this.SetProxyForClient("127.0.0.1:8888::", "http");
	}

	private void LoadProxy() throws Exception {
		if (isLoadedProxy == true) {
			return;
		}
		if (!proxyList.equals("")) {
			String proxyFile = "";
			if (new File(proxyList).exists()) {
				proxyFile = Utilities.readFileAsString(proxyList);

			} else {
				proxyFile = proxyList;
			}

			if (proxyFile.equals("")) {
				return;
			}

			proxyFile = Utilities.RemoveSpace(proxyFile);

			Vector<String> proxyItems = Utilities
					.SimpleRegex("([^:\\s]*):([^:\\s]*):([^:\\s]*):([^:\\s]*)",
							proxyFile, 0);
			if (proxyItems.isEmpty()) {
				proxyItems = Utilities
						.SimpleRegex(
								"([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}[:\\s][0-9]{2,})",
								proxyFile, 1);
			}
			while (true) {
				if (proxyItems.size() == 0) {
					throw new Exception("No proxy");
				}
				Random randomGenerator = new Random();
				int item = randomGenerator.nextInt(proxyItems.size());
				String proxyI = proxyItems.get(item);
				proxyI = proxyI.trim();
				proxyI = proxyI.replace(" ", ":");
				proxyItems.remove(item);
				if (!proxyI.equals("")) {
					String usingproxyI = proxyI;
					if (!this.proxyType.equals("all")) {
						proxyI = RefineProxy(usingproxyI,
								usingproxyI.split(":")[0], this.proxyType);
					} else {
						proxyI = RefineProxy(usingproxyI,
								usingproxyI.split(":")[0], "http");
						if (proxyI.equals("")) {
							proxyI = RefineProxy(usingproxyI,
									usingproxyI.split(":")[0], "socks");
						}
					}
				}
				if (!proxyI.equals("")) {
					if (callback != null) {
						callback.ProxySuccess(proxyI);
					}
					isLoadedProxy = true;
					System.out.println("Change proxy: " + proxyI);
					if (proxyI.split(":").length == 3) {
						this.SetProxyForClient(proxyI.split(":")[0] + ":"
								+ proxyI.split(":")[1], proxyI.split(":")[2]);
					} else if (proxyI.split(":").length == 5) {
						this.SetProxyForClient(
								proxyI.split(":")[0] + ":"
										+ proxyI.split(":")[1] + ":"
										+ proxyI.split(":")[2] + ":"
										+ proxyI.split(":")[3],
								proxyI.split(":")[4]);
					}
					break;
				}
			}
		}
	}
	
	public boolean CheckGoogleBlock(String key)
	{
		String content = GetMethod("http://www.google.com/search?q="
				+ Utilities.EncodeQuery(key));
		if (content.indexOf("('captcha')") > 0) {
			CaptchaForm form = new CaptchaForm(this);
			this.captcha_id = Utilities.SimpleRegexSingle(
					"name=\"id\" value=\"([^\"]+)\"", content, 1);
			String link = "http://www.google.com"
					+ Utilities
							.SimpleRegexSingle("<img src=\"([^\"]+)", content, 1);
			DownloadFile(link, "google_captcha.jpg");
			form.GetInput("google_captcha.jpg", 1);
			return false;
		}
		return true;
	}

	public void RotateProxy() {
		isLoadedProxy = false;
	}

	public String RefineProxy(String pxString, String ip, String type) {
		WebClientX clientproxy = new WebClientX();
		clientproxy.SetTimeout(4000, 6000);
		clientproxy.SetProxyForClient(pxString, type);
		try {
			if (!proxyCheckPage.equals("")) {
				String res = "";
				res = clientproxy.GetMethod(proxyCheckPage);
				if (res.indexOf(proxyChecktext) > 0) {
					return pxString + ":" + type;
				}
			} else {
				String res = "";
				res = clientproxy.GetMethod("http://ipcheckit.com/");
				if (res.indexOf(ip) > 0) {
					return pxString + ":" + type;
				}
			}
		} catch (Exception ex) {
			if (callback != null) {
				callback.ProxyFailed(pxString + ":" + type);
			}
			ex.printStackTrace();
			System.out.println("Proxy timeout: " + pxString + ":" + type);
		}
		return "";
	}

	static class CustomSchemeSocketFactory implements SchemeSocketFactory {

		public Socket createSocket(final HttpParams params) throws IOException {
			if (params == null) {
				throw new IllegalArgumentException(
						"HTTP parameters may not be null");
			}
			String proxyHost = (String) params.getParameter("socks.host");
			Integer proxyPort = (Integer) params.getParameter("socks.port");

			InetSocketAddress socksaddr = new InetSocketAddress(proxyHost,
					proxyPort);
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
			return new Socket(proxy);
		}

		public Socket connectSocket(final Socket socket,
				final InetSocketAddress remoteAddress,
				final InetSocketAddress localAddress, final HttpParams params)
				throws IOException, UnknownHostException,
				ConnectTimeoutException {
			if (remoteAddress == null) {
				throw new IllegalArgumentException(
						"Remote address may not be null");
			}
			if (params == null) {
				throw new IllegalArgumentException(
						"HTTP parameters may not be null");
			}
			Socket sock;
			if (socket != null) {
				sock = socket;
			} else {
				sock = createSocket(params);
			}
			if (localAddress != null) {
				sock.setReuseAddress(HttpConnectionParams
						.getSoReuseaddr(params));
				sock.bind(localAddress);
			}
			int timeout = HttpConnectionParams.getConnectionTimeout(params);
			try {
				sock.connect(remoteAddress, timeout);
			} catch (SocketTimeoutException ex) {
				ex.printStackTrace();
				throw new ConnectTimeoutException("Connect to "
						+ remoteAddress.getHostName() + "/"
						+ remoteAddress.getAddress() + " timed out");
			}
			return sock;
		}

		public boolean isSecure(final Socket sock)
				throws IllegalArgumentException {
			return false;
		}
	}

	public void WrappingClient() {
		httpclient = (DefaultHttpClient) wrapClient(httpclient);
	}

	public static HttpClient wrapClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public void ReturnForm(String captcha, int code) {
		GetMethod("http://www.google.com/sorry/Captcha?continue=abc&id="
				+ this.captcha_id + "&captcha=" + captcha + "&submit=Submit");

		if (callback != null)
			callback.FinishedCaptcha();
	}
}
