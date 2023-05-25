package kr.goldenmine.inuminecraftlauncher.login

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import kr.goldenmine.launchercore.util.LoopUtil
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import java.io.IOException


class BrowserAuto(
    private val id: String,
    private val password: String
) : AuthorizationCodeInstalledApp.Browser {

    @Throws(IOException::class)
    override fun browse(url: String) {
        val driver = ChromeDriver()
        driver[url]
        Thread.sleep(1000L)
        Runtime.getRuntime().addShutdownHook(Thread { driver.quit() })

        val idElement = LoopUtil.waitWhile({
            driver.findElements(By.tagName("input")).stream().filter { it: WebElement ->
                "email" == it.getAttribute(
                    "type"
                )
            }
                .findFirst()
        }, 1000L, -1).get()

        val optionalSubmitElement = driver.findElements(By.tagName("input")).stream().filter { it: WebElement ->
            "submit" == it.getAttribute(
                "type"
            )
        }.findFirst()
        var submitElement = optionalSubmitElement.get()

        idElement.sendKeys(id)
        Thread.sleep(500L)
        submitElement.click()

        val passwordElement = LoopUtil.waitWhile({
            driver.findElements(By.tagName("input")).stream().filter { it: WebElement ->
                "password" == it.getAttribute(
                    "type"
                )
            }
                .findFirst()
        }, 1000L, -1).get()
        submitElement = driver.findElements(By.tagName("input")).stream().filter { it: WebElement ->
            "submit" == it.getAttribute(
                "type"
            )
        }.findFirst().get()

        passwordElement.sendKeys(password)
        Thread.sleep(500L)
        submitElement.click()

        val find = LoopUtil.waitWhile({
            driver.findElements(By.tagName("input")).stream().filter { it: WebElement ->
                "button" == it.getAttribute("type") && "아니요" == it.getAttribute(
                    "value"
                )
            }.findFirst()
        }, 1000L, 10)

        if (find.isPresent) {
            val nextButton = driver.findElements(By.tagName("input")).stream().filter { it: WebElement ->
                "button" == it.getAttribute(
                    "type"
                ) && "아니요" == it.getAttribute("value")
            }.findFirst().get()
            nextButton.click()
        }
    }
}