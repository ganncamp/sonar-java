package checks.spring.innovation;

import org.springframework.stereotype.Component;

@Component
public class B implements Named {
  /**
   * Return the component's name.
   *
   * @return the string {@code "B"} representing this component's name.
   */
  @Override
  public String getName() {
    return "B";
  }
}