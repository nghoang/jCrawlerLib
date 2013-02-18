package com.ngochoang.ParseObject;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.htmlparser.util.ParserException;

import com.ngochoang.CrawlerLib.ParserLib;
import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;

public class FormObject {
	private String webContent;
	private WebClientX client;
	private List<NameValuePair> params;
	private List<NameValuePair> customparams;

	public void setWebContent(String webContent) {
		this.webContent = webContent;
	}

	public String getWebContent() {
		return webContent;
	}

	public void setClient(WebClientX client) {
		this.client = client;
	}

	public WebClientX getClient() {
		return client;
	}

	public void setParams(List<NameValuePair> params) {
		this.customparams = params;
	}

	public List<NameValuePair> getParams() {
		return customparams;
	}

	private void GetParams() throws ParserException {
		ParserLib par = new ParserLib();
		List<GeneralObject> go = par.GetTagValue(webContent, "input", "", "",
				"", "value");
		go.addAll(par.GetTagValue(webContent, "select", "", "", "", "value"));
		go.addAll(par.GetTagValue(webContent, "textarea", "", "", "", "value"));

		params = new ArrayList<NameValuePair>();

		for (GeneralObject g : go) {
			if (!g.getName().equals("")) {
				NameValuePair p = new BasicNameValuePair(g.getName(),
						g.getValue());
				if (customparams == null)
					params.add(p);
				else {
					boolean check = true;
					for (NameValuePair cp : customparams) {
						if (cp.getName().equals(p.getName())) {
							check = false;
							break;
						}
					}
					if (check) {
						if (g.getTagName().toUpperCase().equals("SELECT")) {
							List<GeneralObject> options = par.GetTagValue(
									g.getInnerText(), "option", "", "", "",
									"selected");
							for (GeneralObject option : options) {
								if (option.getValue() != null) {
									if (option.getValue().equals("selected"))
										params.add(new BasicNameValuePair(
												option.getName(),
												par.GetTagValue(
														option.getInnerText(),
														"option", "", "", "",
														"value").get(0)
														.getValue()));
								}
							}
						} else
							params.add(p);
					}
				}

			}
		}
		if (customparams != null)
			params.addAll(customparams);
	}

	public String Request(String base) throws Exception {
		String method = "";
		String url = "";
		ParserLib p = new ParserLib();
		GeneralObject block = p.GetTagValue(webContent, "form", "", "", "",
				"method").get(0);
		if (block.getValue() != null)
			method = block.getValue();
		block = p.GetTagValue(webContent, "form", "", "", "", "action").get(0);
		if (block.getValue() != null)
			url = block.getValue();
		if (method.equals(""))
			method = "GET";
		if (url.equals(""))
			return "";

		if (url.startsWith("http://"))
			return Request(url, method);
		else
			return Request(base + url, method);
	}

	public String Request(String url, String type) throws Exception {
		GetParams();
		String ret = "";
		if (type.toUpperCase().equals("POST"))
			ret = client.PostMethod(url, params);
		else if (type.toUpperCase().equals("GET")) {
			String paramLink = "";
			boolean first = true;
			for (NameValuePair p : params) {
				if (first == true) {
					paramLink += "?" + p.getName() + "="
							+ Utilities.EncodeQuery(p.getValue());
					first = false;
				} else
					paramLink += "&" + p.getName() + "="
							+ Utilities.EncodeQuery(p.getValue());
			}
			ret = client.GetMethod(url + paramLink);
		}
		return ret;
	}
}
