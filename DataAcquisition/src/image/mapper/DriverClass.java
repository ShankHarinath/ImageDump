package image.mapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverClass {

	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	static Logger logger = Logger.getLogger(DriverClass.class.getName());

	public static String gridHubUrl = null;
	public static WebDriver driver = null;

	public static final String imageUrl = "https://www.google.com/search?q=@@@@&rls=en&source=lnms&tbm=isch";

	public static String extractImageUrl(String url) {
		return url.split("imgurl=")[1].split("&imgrefurl=")[0];
	}

	public static String buildImgUrl(String queryStr) {
		return imageUrl.replace("@@@@", queryStr);
	}

	public static String formatPlaces(String place) {
		return place.replaceAll("\\d+\\s", "").replaceAll("\\s", "+")
				.toLowerCase();
	}

	public static WebDriver getDriver(String browserName, String version) throws Exception{
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

		switch(browserName.toUpperCase())
		{
		case "FIREFOX":
			if (null==gridHubUrl) {
				//running on local
				desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			}
			else {
				desiredCapabilities.setPlatform(Platform.MAC);
				//			desiredCapabilities.setVersion(version);
				desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			}

			try {
				if (null==gridHubUrl) {
					FirefoxProfile firefoxProfile = new FirefoxProfile();
					firefoxProfile.setPreference("browser.download.dir", "/Users/" + System.getProperty("user.name") + "/Downloads");
					firefoxProfile.setPreference("browser.download.folderList", 2);
					firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream,application/x-octet-stream,text/har,application/binary");
					firefoxProfile.setEnableNativeEvents(false);
					desiredCapabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
					driver = new FirefoxDriver(desiredCapabilities);
				}
				else{
					//running on Selenium Grid
					desiredCapabilities.setBrowserName(DesiredCapabilities.firefox().getBrowserName());
					driver = new RemoteWebDriver(new URL(gridHubUrl), desiredCapabilities);
				}
				break;
			} catch (Exception e) {
				e.printStackTrace();
				driver = null;
			}
		case "HTMLUNIT":
			if (null==gridHubUrl) {
				driver = new HtmlUnitDriver(true);
			}
			else{
				desiredCapabilities.setBrowserName(DesiredCapabilities.htmlUnit().getBrowserName());
				driver = new RemoteWebDriver(new URL(gridHubUrl), desiredCapabilities);
			}
			break;
		default:
			throw new Exception("You must choose one of the defined driver types only");
		}
		return driver;
	}

	public static List<String> getImageUrls(String place, WebDriver driver)
			throws InterruptedException {
		logMe("==> Obtaining image urls for: " + place);

		List<String> elems = new ArrayList<String>();

		driver.get(place);
		sleep(1);
		((JavascriptExecutor) driver)
		.executeScript("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,"
				+ "document.body.scrollHeight,document.documentElement.clientHeight));");
		sleep(2);
		driver.findElement(By.id("smb")).click();
		sleep(2);
		((JavascriptExecutor) driver)
		.executeScript("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,"
				+ "document.body.scrollHeight,document.documentElement.clientHeight));");
		sleep(2);
		((JavascriptExecutor) driver)
		.executeScript("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,"
				+ "document.body.scrollHeight,document.documentElement.clientHeight));");

		try {
			(new WebDriverWait(driver, 30)).until(ExpectedConditions
					.presenceOfElementLocated(By.cssSelector("#rg_s a")));
		} catch (Exception e) {
		}

		List<WebElement> webElems = driver.findElements(By
				.cssSelector("#rg_s a"));

		for (WebElement elem : webElems) {
			if (elems.size() < 725) {
				elems.add(extractImageUrl(elem.getAttribute("href")));
			} else {
				break;
			}
		}

		logMe(place + " " + elems.size());

		logMe("<== Obtaining image urls");

		return elems;
	}

	public static List<String> getPlcaeNames() throws Exception {
		logMe("==> Obtaining place names");

		List<String> elems = new ArrayList<String>();
		WebDriver driver = getDriver("HTMLUNIT", "24");

		driver.get("http://www.absolutevisit.com/top-100-places-in-the-world");

		for (WebElement elem : driver.findElements(By
				.cssSelector("#tabarea1 a"))) {
			elems.add(elem.getText());
		}

		driver.close();

		logMe("<== Obtained place names");

		return elems;
	}

	public static void logMe(String msg) {
		logger.info(msg);
	}

	public static void logErr(String msg) {
		logger.error(msg);
	}

	public static void sleep(long secs) throws InterruptedException {
		Thread.sleep(secs * 1000);
	}
}
