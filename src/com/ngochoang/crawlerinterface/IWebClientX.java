package com.ngochoang.crawlerinterface;

public interface IWebClientX {
	public void ProxyFailed(String px);
	public void ProxySuccess(String px);
	public void DropConnection(String url);
	public void FinishedCaptcha();
}
