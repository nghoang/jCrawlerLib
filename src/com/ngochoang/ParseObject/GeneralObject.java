package com.ngochoang.ParseObject;

public class GeneralObject {
	private String tagName;
	private String id;
	private String name;
	private String cls;
	private String value;
	private String innerText;
	private int BeginPos;
	private int EndPos;
	private String innerPlainText;
	
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getTagName() {
		return tagName;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}
	public String getInnerText() {
		return innerText;
	}
	public void setBeginPos(int beginPos) {
		BeginPos = beginPos;
	}
	public int getBeginPos() {
		return BeginPos;
	}
	public void setEndPos(int endPos) {
		EndPos = endPos;
	}
	public int getEndPos() {
		return EndPos;
	}
	public void setInnerPlainText(String innerPlainText) {
		this.innerPlainText = innerPlainText.trim();
	}
	public String getInnerPlainText() {
		return innerPlainText;
	}
	public void setCls(String cls) {
		this.cls = cls;
	}
	public String getCls() {
		return cls;
	}
}
