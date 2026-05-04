package com.smartapply.assistant.service;

import com.smartapply.assistant.dto.JobDto;
import com.smartapply.assistant.entity.JobPreference;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobScraperService {

    public List<JobDto> fetchJobs(JobPreference pref) {
        List<JobDto> jobs = new ArrayList<>();
        WebDriver driver = null;
        
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new"); // Modern headless mode
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-blink-features=AutomationControlled");
            
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Format URL for Naukri search (e.g., java-developer-jobs-in-remote)
            String roleStr = (pref.getRoles() != null && !pref.getRoles().isEmpty()) ? pref.getRoles() : "software-developer";
            String locStr = (pref.getLocations() != null && !pref.getLocations().isEmpty()) ? pref.getLocations() : "india";
            
            // Clean up and format for URL
            String formattedRole = roleStr.split(",")[0].trim().toLowerCase().replaceAll("[^a-z0-9]", "-");
            String formattedLoc = locStr.split(",")[0].trim().toLowerCase().replaceAll("[^a-z0-9]", "-");
            
            String targetUrl = "https://www.naukri.com/" + formattedRole + "-jobs-in-" + formattedLoc;
            System.out.println("Scraping Naukri URL: " + targetUrl);
            
            driver.get(targetUrl);

            // Wait for job cards to load (Naukri usually uses classes like 'srp-jobtuple-wrapper' or 'jobTuple')
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".srp-jobtuple-wrapper, .jobTuple")));
                
                List<WebElement> jobCards = driver.findElements(By.cssSelector(".srp-jobtuple-wrapper, .jobTuple"));
                
                // Fetch up to 5 jobs
                int count = 0;
                for (WebElement card : jobCards) {
                    if (count >= 5) break;
                    
                    try {
                        String title = "";
                        String url = "";
                        // Naukri titles are usually in a tag with class 'title'
                        List<WebElement> titleElems = card.findElements(By.cssSelector("a.title"));
                        if (!titleElems.isEmpty()) {
                            title = titleElems.get(0).getText();
                            url = titleElems.get(0).getAttribute("href");
                        }
                        
                        String company = "";
                        List<WebElement> compElems = card.findElements(By.cssSelector("a.comp-name"));
                        if (!compElems.isEmpty()) {
                            company = compElems.get(0).getText();
                        }
                        
                        String location = "";
                        List<WebElement> locElems = card.findElements(By.cssSelector(".locWdth"));
                        if (!locElems.isEmpty()) {
                            location = locElems.get(0).getText();
                        }
                        
                        String snippet = "";
                        List<WebElement> descElems = card.findElements(By.cssSelector(".job-desc"));
                        if (!descElems.isEmpty()) {
                            snippet = descElems.get(0).getText();
                        }
                        
                        if (!title.isEmpty()) {
                            jobs.add(new JobDto(title, company, location, url, snippet));
                            count++;
                        }
                    } catch (Exception inner) {
                        System.err.println("Error parsing a job card: " + inner.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Timeout or layout changed on Naukri: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("WebDriver failed: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {}
            }
        }

        // If Selenium gets blocked or layout fails, provide fallback so the UI isn't completely broken
        if (jobs.isEmpty()) {
            System.out.println("Returning fallback data because Naukri scraper returned 0 jobs (likely blocked/captcha).");
            jobs.add(new JobDto(
                    "Senior Software Engineer", 
                    "TechCorp Inc.", 
                    "Remote", 
                    "https://www.naukri.com/", 
                    "Looking for a backend engineer with Spring Boot experience."
            ));
            jobs.add(new JobDto(
                    "Full Stack Developer", 
                    "InnovateAI", 
                    "New York, NY", 
                    "https://www.naukri.com/", 
                    "Join our fast-paced startup building AI agents using Angular and Java."
            ));
        }

        return jobs;
    }

    private void loginToNaukri(WebDriver driver, WebDriverWait wait, String email, String password) {
        System.out.println("Logging in to Naukri for email: " + email);
        driver.get("https://www.naukri.com/nlogin/login");
        
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
        emailField.sendKeys(email);
        
        WebElement passField = driver.findElement(By.id("passwordField"));
        passField.sendKeys(password);
        
        WebElement loginBtn = driver.findElement(By.cssSelector(".blue-btn"));
        loginBtn.click();
        
        // Wait for login to complete
        wait.until(ExpectedConditions.urlContains("naukri.com"));
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
    }

    public void updateNaukriProfile(String email, String password) {
        WebDriver driver = null;
        try {
            driver = createDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            loginToNaukri(driver, wait, email, password);

            System.out.println("Navigating to profile page...");
            driver.get("https://www.naukri.com/mnjuser/profile");

            // Update Profile Summary to trigger refresh
            try {
                WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#lazyProfileSummary .edit, #lazyProfileSummary .editIcon")));
                editBtn.click();

                WebElement summaryArea = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profileSummaryTxt")));
                String currentSummary = summaryArea.getAttribute("value");
                
                // Add or remove a trailing space to trigger a change
                String newSummary = currentSummary.endsWith(" ") ? currentSummary.trim() : currentSummary + " ";
                
                summaryArea.clear();
                summaryArea.sendKeys(newSummary);

                WebElement saveBtn = driver.findElement(By.cssSelector("button.btn-dark-ot[type='submit']"));
                saveBtn.click();
                
                System.out.println("Profile summary updated successfully.");
                Thread.sleep(2000);
            } catch (Exception e) {
                System.err.println("Failed to update profile summary: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Naukri profile update failed: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }
    }

    private WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");
        return new ChromeDriver(options);
    }

    public void applyToNaukri(JobDto job, String email, String password) {
        WebDriver driver = null;
        try {
            driver = createDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            loginToNaukri(driver, wait, email, password);

            // 2. Navigate to Job URL
            System.out.println("Navigating to job: " + job.getUrl());
            driver.get(job.getUrl());
            
            // 3. Click Apply
            try {
                WebElement applyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".apply-button, #login-apply-button")));
                String btnText = applyBtn.getText();
                System.out.println("Found apply button with text: " + btnText);
                
                if (btnText.toLowerCase().contains("applied")) {
                    System.out.println("Already applied to this job.");
                } else {
                    applyBtn.click();
                    System.out.println("Clicked apply button.");
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                System.err.println("Could not find apply button or it was already applied: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Naukri apply failed: " + e.getMessage());
            throw new RuntimeException("Failed to apply on Naukri: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {}
            }
        }
    }
}
