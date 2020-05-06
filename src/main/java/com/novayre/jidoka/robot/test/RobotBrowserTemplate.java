package com.novayre.jidoka.robot.test;

import org.apache.commons.lang.StringUtils;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Browser robot template. 
 */
@Robot
public class RobotBrowserTemplate implements IRobot {

	/**
	 * URL to navigate to.
	 */
	private static final String HOME_URL = "https://www.tesla.com/";
	
	/** The JidokaServer instance. */
	private IJidokaServer<?> server;
	
	/** The IClient module. */
	private IClient client;
	
	/** WebBrowser module */
	private IWebBrowserSupport browser;

	/** Browser type parameter **/
	private String browserType;

	/**
	 * Action "start"
	 * @throws Exception
	 */
	public void start() throws Exception {
		
		server = (IJidokaServer< ? >) JidokaFactory.getServer();

		client = IClient.getInstance(this);
	}

	/**
	 * Open Web Browser
	 * @throws Exception
	 */
	public void openBrowser() throws Exception  {

		browser = IWebBrowserSupport.getInstance(this, client);

		browserType = server.getParameters().get("Browser");
		
		// Select browser type
		if (StringUtils.isBlank(browserType)) {
			server.info("Browser parameter not present. Using the default browser CHROME");
			browser.setBrowserType(EBrowsers.CHROME);
			browserType = EBrowsers.CHROME.name();
		} else {
			EBrowsers selectedBrowser = EBrowsers.valueOf(browserType);
			browser.setBrowserType(selectedBrowser);
			server.info("Browser selected: " + selectedBrowser.name());
		}

		// Set timeout to 60 seconds
		browser.setTimeoutSeconds(60);

		// Init the browser module
		browser.initBrowser();

		server.setNumberOfItems(1);

	}

	/**
	 * Navigate to Web Page
	 * @throws Exception
	 */
	public void navigateToWeb() throws Exception  {
		
		server.setCurrentItem(1, HOME_URL);
		
		// Navigate to HOME_URL address
		browser.navigate(HOME_URL);

		//This command is uses to make visible in the desktop the page (IExplore issue)
		if (browserType.equals("IE")) {
			client.clickOnCenter();
			client.pause(3000);
		}
		
		// we save the screenshot, it can be viewed in robot execution trace page on the console
		server.sendScreen("Screen after load page: " + HOME_URL);
	}

	/**
	 * 
	 * We use the close method implemented in the driver. 
	 * In your robots you should use browser.close() 
	 */
	private void close() throws Exception {
		browser.getDriver().close();
	}
	
	/**
	 * Close Browser
	 * @throws Exception
	 */
	public void closeBrowser() throws Exception  {
		close();
		server.setCurrentItemResultToOK("Success");
	}

	
	/**
	 * Action "end"
	 * @throws Exception
	 */
	public void end() throws Exception {
	}
	

	
}
