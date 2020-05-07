package com.novayre.jidoka.robot.test;

import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider;
import com.novayre.jidoka.data.provider.api.IJidokaExcelDataProvider;
import org.apache.commons.lang.StringUtils;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Browser robot template.
 */
@Robot
public class RobotBrowserTemplate implements IRobot {

    /**
     * URL to navigate to.
     */
    private static final String HOME_URL = "https://www.google.com/";

    /**
     * The JidokaServer instance.
     */
    private IJidokaServer<?> server;

    /**
     * The IClient module.
     */
    private IClient client;

    /**
     * WebBrowser module
     */
    private IWebBrowserSupport browser;

    /**
     * Browser type parameter
     **/
    private String browserType;

    /**
     * The Constant EXCEL_FILENAME.
     */
    private static final String EXCEL_FILENAME = "input.xlsx";

    /**
     * The data provider.
     */
    private IJidokaExcelDataProvider<ExcelRow> dataProvider;

    /**
     * The current item.
     */
    private ExcelRow currentItem;

    /**
     * The excel file.
     */
    private String excelFile;


    /**
     * Action "start"
     *
     * @throws Exception
     */
    public void start() throws Exception {

        server = (IJidokaServer<?>) JidokaFactory.getServer();

        dataProvider = IJidokaDataProvider.getInstance(this, IJidokaDataProvider.Provider.EXCEL);

        client = IClient.getInstance(this);

        initDataProvider();
    }

    /**
     * Initializes the data provider.
     * <p>
     * Action "Open DataProvider".
     *
     * @throws Exception in case any exception is thrown during the initialization
     */
    public void initDataProvider() throws Exception {

        server.info("Initializing Data Provider with file: " + EXCEL_FILENAME);

        // Path (String) to the file containing the items to process
        excelFile = Paths.get(server.getCurrentDir(), EXCEL_FILENAME).toString();

        // Initialization of the Data Provider module using the RowMapper implemented
        dataProvider.init(excelFile, null, 0, new ExcelRowMapper());

        server.info("Total count in excel loaded :: " + dataProvider.count());

        // Set the number of items relying on the Data Provider module
        server.setNumberOfItems(dataProvider.count());
    }

    /**
     * Processes an item.
     * <p>
     * In this template example, the processing consists of concatenating the first
     * 3 columns to get the string result and update the last column.
     */
    public boolean processItem(String input) {

        boolean isMatchFound = false;

        // Get the current item through Data Provider
        while (dataProvider.nextRow()) {
            currentItem = dataProvider.getCurrentItem();

            server.info(" dataProvider row 1 : " + dataProvider.getCurrentItemNumber());
            server.info(" Current Item : " + currentItem);

            // The key to use is the literal "row" plus the number of the item
            String itemKey = "row " + dataProvider.getCurrentItemNumber();
            server.setCurrentItem(dataProvider.getCurrentItemNumber(), itemKey);

            if (currentItem.getCol1().equalsIgnoreCase(input)) {
                isMatchFound = true;
                currentItem.setResult("Match found!");
            }

            // Update the item in the Excel file through Data Provider
            dataProvider.updateItem(currentItem);

            // We consider this item is OK
            server.setCurrentItemResultToOK(currentItem.getResult());
        }

        return isMatchFound;
    }

    /**
     * Open Web Browser
     *
     * @throws Exception
     */
    public void openBrowser() throws Exception {

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
     *
     * @throws Exception
     */
    public void navigateToWeb() throws Exception {

        server.setCurrentItem(1, HOME_URL);

        // Navigate to HOME_URL address
        browser.navigate(HOME_URL);

        By searchBar = By.xpath("/html/body/div/div[4]/form/div[2]/div[1]/div[1]/div/div[2]/input");

        browser.textFieldSet(searchBar, "monzo sort code", true);

        By search = By.xpath("/html/body/div/div[4]/form/div[2]/div[1]/div[2]/div[2]/div[2]/center/input[1]");
        browser.clickOnElement(search);

        By sortCodePath = By.xpath("/html/body/div[7]/div[2]/div[9]/div[1]/div[2]/div/div[2]/div[2]/div/div/div[1]/div[1]/div/div[1]/div/div[2]/div/div[1]");
        String sortCode = browser.getText(sortCodePath,
                true);

        server.info(" Sort code from google :: " + sortCode);

        By searchText = By.xpath("/html/body/div[4]/form/div[2]/div[1]/div[2]/div/div[2]/input");

        boolean isMatchFound = processItem(sortCode);

        server.info(" Is Match found :: " + isMatchFound);

        if (isMatchFound) browser.textFieldSet(searchText, "Match Found!", true);
        else browser.textFieldSet(searchText, "Match Not Found!", true);


        //This command is uses to make visible in the desktop the page (IExplore issue)
        if (browserType.equals("IE")) {
            client.clickOnCenter();
            client.pause(3000);
        }

        // we save the screenshot, it can be viewed in robot execution trace page on the console
        server.sendScreen("Screen after load page: " + HOME_URL);
    }

    /**
     * We use the close method implemented in the driver.
     * In your robots you should use browser.close()
     */
    private void close() throws Exception {
        browser.getDriver().close();
    }

    /**
     * Close Browser
     *
     * @throws Exception
     */
    public void closeBrowser() throws Exception {
       // close();
        closeDataProvider();
        server.setCurrentItemResultToOK("Success");
    }

    /**
     * Method to close the data provider.
     * <p>
     * It's a private method to be called from the "End" action, but also from the
     * {@link #cleanUp()} method to assure the data provider is correctly closed.
     *
     * @throws IOException if an I/O error occurs
     */
    private void closeDataProvider() throws IOException {

        if (dataProvider != null) {

            server.info("Closing Data Provider...");

            dataProvider.close();
            dataProvider = null;
        }
    }

    /**
     * Clean up.
     * <p>
     * Besides returning the updated Excel file, it tries to close the data
     * provider. This is useful to assure that executions with problems close it
     * too.
     *
     * @return an array with the paths of the files to return
     * @throws Exception in case any exception is thrown
     * @see IRobot#cleanUp()
     */
    @Override
    public String[] cleanUp() throws Exception {

        closeDataProvider();

        if (new File(excelFile).exists()) {
            return new String[]{excelFile};
        }

        return IRobot.super.cleanUp();
    }

    /**
     * Manages exceptions that may arise during the robot execution.
     *
     * @see IRobot#manageException(String,
     * Exception)
     */
    @Override
    public String manageException(String action, Exception exception) throws Exception {

        // Optionally, we send the exception to the execution log
        server.warn(exception.getMessage(), exception);

        /*
         * We take advantage of the Apache Commons ExceptionUtils class to know if a
         * specific exception was thrown throughout the code.
         * Since we threw a JidokaItemException, it's the one to be searched in the
         * exceptions stack trace. If found, the flow goes to the next item by telling
         * the next method to execute is 'moreItems()'.
         * If another exception is found, it is propagated, so the robot ends with a
         * failure.
         */
        if (ExceptionUtils.indexOfThrowable(exception, JidokaItemException.class) >= 0) {
            server.setCurrentItemResultToWarn("Exception processing the item!");
            return "hasMoreItems";
        }

        // Unknown exception. Throw it
        throw exception;
    }


    /**
     * Action "end"
     *
     * @throws Exception
     */
    public void end() throws Exception {
    }


}
