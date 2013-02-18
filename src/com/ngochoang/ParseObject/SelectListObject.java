package com.ngochoang.ParseObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.htmlparser.util.ParserException;

import com.ngochoang.CrawlerLib.ParserLib;
import com.ngochoang.CrawlerLib.Utilities;

public class SelectListObject {
	private List<NameValuePair> items;
	private String webContent;
	
	private void Parse() throws ParserException
	{
		if (webContent.equals(""))
			return;
		ParserLib par = new ParserLib();
		Vector<GeneralObject> options = par.GetTagValue(webContent, "option", "", "", "", "value");
		items = new ArrayList<NameValuePair>();
		for(GeneralObject g : options)
		{
			items.add(new BasicNameValuePair(g.getValue(), g.getInnerText().trim()));
		}
	}
	
	public void setItems(List<NameValuePair> items) {
		this.items = items;
	}

	public List<NameValuePair> getItems() {
		return items;
	}

	public void setWebContent(String webContent) {
		this.webContent = webContent;
		try {
			Parse();
		} catch (ParserException e) {
			Utilities.WriteLogTrace(e);
			e.printStackTrace();
		}
	}
}
