package image.mapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImageUrls {

	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	public static ConcurrentHashMap<String, List<String>> urls = new ConcurrentHashMap<String, List<String>>();
	static Logger logger = Logger.getLogger(ImageUrls.class.getName());

	@DataProvider(name = "ObtainPlaces", parallel = false)
	public static Object[][]  getPlcaeNames() throws Exception {
		logMe("==> Obtaining place names");

		List<String> elems = new ArrayList<String>();
		WebDriver driver = DriverClass.getDriver("HTMLUNIT", "");

		driver.get("http://www.absolutevisit.com/top-100-places-in-the-world");

		for (WebElement elem : driver.findElements(By
				.cssSelector("#tabarea1 a"))) {
			elems.add(elem.getText());
		}

		driver.close();

		logMe("<== Obtained place names");

		String[][] dataProv = new String[elems.size()][];  

		for (int i=0; i<elems.size(); i++) {  
			dataProv[i] = new String[]{elems.get(i)};  
		}

		return dataProv;
	}

	@Test(dataProvider = "ObtainPlaces", dataProviderClass = ImageUrls.class)
	public void runImageURLFetcher(String place) throws Exception {

		logMe("Starting data acquisition...");

		WebDriver driver = DriverClass.getDriver("Firefox", "24");
		urls.put(place.replaceAll("\\d+\\s", ""),
				DriverClass.getImageUrls(DriverClass.buildImgUrl(
						DriverClass.formatPlaces(place)), driver));

		driver.close();

		logMe("Data acquisition complete!!");
	}

	@AfterTest
	public static void update() throws IOException{

		FileOutputStream fos = new FileOutputStream("urlMap.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(urls);
		oos.close();
		fos.close();

		Runtime run = Runtime.getRuntime();
		run.exec("/usr/local/Cellar/hadoop/2.6.0/bin/hadoop fs -put urlMap.ser /user/Shank/");
	}

	public static void logMe(String msg) {
		logger.info(msg);
	}

	public static void logErr(String msg) {
		logger.error(msg);
	}
}
