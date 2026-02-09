package checks.spring.innovation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

  @Autowired
  Named nameSource; // Noncompliant

  @Autowired
  A a;

  @Autowired
  Named second; /**
   * Application entry point that starts the Spring Boot application.
   *
   * @param args command-line arguments passed to the application
   */

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  /**
   * Prints a greeting to standard output using the configured name.
   *
   * Writes "Hello,  {name}!" where {name} is obtained from the application's name source.
   */
  @Override
  public void run(String... args) throws Exception {
    System.out.println("Hello,  " + nameSource.getName() + "!");
  }
}