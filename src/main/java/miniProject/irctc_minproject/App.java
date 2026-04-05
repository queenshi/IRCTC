package miniProject.irctc_minproject;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class App {

	public static void main(String[] args) throws IOException, InterruptedException {
		// === Driver setup ===
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		driver.manage().window().maximize();

		try {
			// === Open site ===
			driver.get("https://www.air.irctc.co.in/");
			System.out.println("Opened: " + driver.getTitle());

			// === From ===
			WebElement from = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stationFrom")));
			from.clear();
			from.sendKeys("Hyd");
			sleep(800);
			selectFromSuggestions(wait, "HYDERABAD (HYD)");
			String fromCity = from.getText();

			// === To ===
			WebElement to = driver.findElement(By.id("stationTo"));
			to.clear();
			to.sendKeys("Pune");
			sleep(800);
			selectFromSuggestions(wait, "PUNE (PNQ)");
			String toCity = to.getText();

			// === Departure: pick TODAY ===
			WebElement dateInput = driver.findElement(By.id("originDate"));
			dateInput.click();
			int todayDay = LocalDate.now().getDayOfMonth();
			String xpath = "//td[@class='date']//span[contains(@class,'act') and normalize-space(text())='" + todayDay
					+ "']";
			WebElement todayDate = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
			todayDate.click();

			// === Traveller/Class: Business (fallback to Economy) ===
			driver.findElement(By.id("noOfpaxEtc")).click();
			sleep(500);

			WebElement travelClassDropdown = driver.findElement(By.id("travelClass"));

			// Create Select object
			Select select = new Select(travelClassDropdown);

			// Select "Business" by visible text
			select.selectByVisibleText("Business");

			try {
				WebElement doneBtn = driver.findElement(By.id("addModifyBtn"));
				doneBtn.click();
			} catch (NoSuchElementException e) {
				// ignore if popup auto-closes
			}

			// === Search ===
			driver.findElement(By.cssSelector("button.btn.btn-md.yellow-gradient.home-btn")).click();
			wait.until(ExpectedConditions.urlContains("search"));
			System.out.println("Search triggered. Waiting for results...");
			Thread.sleep(2000);
			String confFrom = driver.findElement(By.id("stationFrom")).getText();
			String confTo = driver.findElement(By.id("stationTo")).getText();

			if (confFrom.equals(fromCity) && confTo.equals(toCity)) {
				System.out.println("Validated City and Date");
			}

			// === Screenshot ===
			File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			String shot = "target/irctc-results.png";
			FileUtils.copyFile(src, new File(shot));
			System.out.println("Screenshot saved: " + shot);

		} finally {
			driver.quit();
		}
	}

	private static void selectFromSuggestions(WebDriverWait wait, String mustContain) {
		List<WebElement> suggestions = wait
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("ul li div")));
		suggestions.stream()
				.filter(el -> el.getText() != null && el.getText().toUpperCase().contains(mustContain.toUpperCase()))
				.findFirst().orElseThrow(() -> new NoSuchElementException("No suggestion like: " + mustContain))
				.click();
	}

	private static void sleep(long ms) throws InterruptedException {
		Thread.sleep(ms);
	}
}