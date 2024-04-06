package org.judexmars.imagecrud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Renderer for all outcoming messages.
 */
@Component
@RequiredArgsConstructor
public class MessageRenderer {

  private final MessageSource messageSource;

  /**
   * Render image depending on the current locale.
   *
   * @param messageCode message code from the property file
   * @param parameters  additional parameters which will be used in the message
   * @return final message
   */
  public String render(String messageCode, Object... parameters) {
    return messageSource.getMessage(messageCode, parameters, LocaleContextHolder.getLocale());
  }
}
