package checks.spring.innovation;

import org.springframework.stereotype.Component;

@Component
public class A implements Named {
  /**
   * Provides the component name.
   *
   * @return the constant name {@code "A"} of this component.
   */
  @Override
  public String getName() {
    return "A";
  }
}