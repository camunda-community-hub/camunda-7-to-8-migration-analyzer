package org.camunda.community.migration.converter.convertible;

public class OutputValuesConvertible implements TextConvertible {
  protected String content;

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public void setContent(String content) {
    this.content = content;
  }
}
