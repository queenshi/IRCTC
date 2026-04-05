package testmini;



import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class IRCTCTest extends BaseTest {

    @Test
    public void flightSearchTest() throws IOException, InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get("https://www.air.irctc.co.in/");
        System.out.println("Opened: " + driver.getTitle());

        WebElement from = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stationFrom")));
        from.clear();
        from.sendKeys("Hyd");
        Thread.sleep(800);
        selectFromSuggestions(wait, "HYDERABAD (HYD)");
        String fromCity = from.getAttribute("value");

        WebElement to = driver.findElement(By.id("stationTo"));
        to.clear();
        to.sendKeys("Pune");
        Thread.sleep(800);
        selectFromSuggestions(wait, "PUNE (PNQ)");
        String toCity = to.getAttribute("value");

        WebElement dateInput = driver.findElement(By.id("originDate"));
        dateInput.click();
        int todayDay = LocalDate.now().getDayOfMonth();
        String xpath = "//td[@class='date']//span[contains(@class,'act') and normalize-space(text())='" + todayDay + "']";
        WebElement todayDate = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        todayDate.click();

        driver.findElement(By.id("noOfpaxEtc")).click();
        Thread.sleep(500);
        Select select = new Select(driver.findElement(By.id("travelClass")));
        select.selectByVisibleText("Business");

        try {
            driver.findElement(By.id("addModifyBtn")).click();
        } catch (NoSuchElementException ignored) {}

        driver.findElement(By.cssSelector("button.btn.btn-md.yellow-gradient.home-btn")).click();
        wait.until(ExpectedConditions.urlContains("search"));
        Thread.sleep(2000);

        String confFrom = driver.findElement(By.id("stationFrom")).getAttribute("value");
        String confTo = driver.findElement(By.id("stationTo")).getAttribute("value");

        if (confFrom.equals(fromCity) && confTo.equals(toCity)) {
            System.out.println("Validated City and Date");
        }

        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String shot = "target/irctc-results.png";
        FileUtils.copyFile(src, new File(shot));
        System.out.println("Screenshot saved: " + shot);
    }

    private void selectFromSuggestions(WebDriverWait wait, String mustContain) {
        List<WebElement> suggestions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("ul li div")));
        suggestions.stream()
                .filter(el -> el.getText() != null && el.getText().toUpperCase().contains(mustContain.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No suggestion like: " + mustContain))
                .click();
    }
}
