package com.ngochoang.CrawlerLib;

import java.util.Vector;

public class PublicProxy extends Thread {
	ParserLib parser = new ParserLib();
	WebClientX client = new WebClientX();
	private String SaveFile = "temp_proxy.txt";
	private Vector<String> proxyList = new Vector<String>();
	private int MaxPage = 10;

	public void CrawlProxy(String _SaveFile) throws Exception {
		_SaveFile = SaveFile;
		CrawlProxy();
	}
	
	public void CrawlProxy() throws Exception {
		int page = 1;
		setProxyList(new Vector<String>());
		String res = "";
		while (true) {
			res = client
					.GetMethod("http://www.cooleasy.com/?act=list&port=&type=&country=&page="
							+ page);
			res = parser.GetBlock(res, "body", "", "", "", "", "").get(0)
					.getInnerPlainText();
			getProxyList().addAll(ParseProxy(res));

			res = client
					.GetMethod("http://www.xroxy.com/proxylist.php?port=&type=&ssl=&country=&latency=&reliability=&sort=reliability&desc=true&pnum="
							+ page);
			res = parser.GetBlock(res, "body", "", "", "", "", "").get(0)
					.getInnerPlainText();
			getProxyList().addAll(ParseProxy(res));

			if (page < 10)
				res = client.GetMethod("http://www.samair.ru/proxy/proxy-0"
						+ page + ".htm");
			else
				res = client.GetMethod("http://www.samair.ru/proxy/proxy-"
						+ page + ".htm");
			res = parser.GetBlock(res, "body", "", "", "", "", "").get(0)
					.getInnerPlainText();
			getProxyList().addAll(ParseProxy(res));

			page++;
			if (page > getMaxPage())
				break;
		}

		res = client.GetMethod("http://proxy.antipalivo.ru/");
		res = parser.GetBlock(res, "body", "", "", "", "", "").get(0)
				.getInnerPlainText();
		getProxyList().addAll(ParseProxy(res));

		res = client
				.GetMethod("http://www.checker.freeproxy.ru/checker/last_checked_proxies.php");
		res = parser.GetBlock(res, "body", "", "", "", "", "").get(0)
				.getInnerPlainText();
		getProxyList().addAll(ParseProxy(res));
		
		SaveProxy();
	}

//	private Vector<String> ParseProxy(GeneralObject content) {
//		return ParseProxy(content.getInnerPlainText());
//	}

	private Vector<String> ParseProxy(String content) {
		content = Utilities.RemoveSpace(content);
		Vector<String> proxyItems = new Vector<String>();
		proxyItems = Utilities
				.SimpleRegex(
						"([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}[:\\s][0-9]{2,})",
						content, 1);

		Vector<String> newproxyItems = new Vector<String>();
		for (String p : proxyItems) {
			newproxyItems.add(p.replace(" ", ":"));
		}
		return newproxyItems;
	}

	public void SaveProxy() {
		String tr = "";
		for (String px : getProxyList()) {
			tr += px + "\n";
		}
		Utilities.WriteFile(getSaveFile(), tr, false);
	}

	public Vector<String> GetProxyList() {
		return getProxyList();
	}

	public void setMaxPage(int maxPage) {
		MaxPage = maxPage;
	}

	public int getMaxPage() {
		return MaxPage;
	}

	public void setSaveFile(String saveFile) {
		SaveFile = saveFile;
	}

	public String getSaveFile() {
		return SaveFile;
	}

	public void setProxyList(Vector<String> proxyList) {
		this.proxyList = proxyList;
	}

	public Vector<String> getProxyList() {
		return proxyList;
	}
}
