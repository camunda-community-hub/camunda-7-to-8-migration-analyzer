package org.camunda.community.migration.converter;

public class DefaultConverterProperties implements ConverterProperties {
  private String scriptHeader;
  private String resultVariableHeader;
  private String defaultJobType;
  private String scriptJobType;
  private String resourceHeader;
  private String scriptFormatHeader;
  private String platformVersion;
  private String dataMigrationExecutionListenerJobType;

  private Boolean keepJobTypeBlank;
  private Boolean alwaysUseDefaultJobType;
  private Boolean addDataMigrationExecutionListener;
  private Boolean appendDocumentation;
  private Boolean appendElements;

  @Override
  public Boolean getAppendDocumentation() {
    return appendDocumentation;
  }

  public void setAppendDocumentation(Boolean appendDocumentation) {
    this.appendDocumentation = appendDocumentation;
  }

  @Override
  public String getPlatformVersion() {
    return platformVersion;
  }

  public void setPlatformVersion(String platformVersion) {
    this.platformVersion = platformVersion;
  }

  @Override
  public String getScriptHeader() {
    return scriptHeader;
  }

  public void setScriptHeader(String scriptHeader) {
    this.scriptHeader = scriptHeader;
  }

  @Override
  public String getResultVariableHeader() {
    return resultVariableHeader;
  }

  public void setResultVariableHeader(String resultVariableHeader) {
    this.resultVariableHeader = resultVariableHeader;
  }

  @Override
  public String getDefaultJobType() {
    return defaultJobType;
  }

  public void setDefaultJobType(String defaultJobType) {
    this.defaultJobType = defaultJobType;
  }

  @Override
  public String getScriptJobType() {
    return scriptJobType;
  }

  public void setScriptJobType(String scriptJobType) {
    this.scriptJobType = scriptJobType;
  }

  @Override
  public String getResourceHeader() {
    return resourceHeader;
  }

  public void setResourceHeader(String resourceHeader) {
    this.resourceHeader = resourceHeader;
  }

  @Override
  public String getScriptFormatHeader() {
    return scriptFormatHeader;
  }

  public void setScriptFormatHeader(String scriptFormatHeader) {
    this.scriptFormatHeader = scriptFormatHeader;
  }

  @Override
  public Boolean getAppendElements() {
    return appendElements;
  }

  public void setAppendElements(Boolean appendElements) {
    this.appendElements = appendElements;
  }

  public Boolean getKeepJobTypeBlank() {
    return keepJobTypeBlank;
  }

  public void setKeepJobTypeBlank(Boolean keepJobTypeBlank) {
    this.keepJobTypeBlank = keepJobTypeBlank;
  }

  public Boolean getAlwaysUseDefaultJobType() {
    return alwaysUseDefaultJobType;
  }

  public void setAlwaysUseDefaultJobType(Boolean alwaysUseDefaultJobType) {
    this.alwaysUseDefaultJobType = alwaysUseDefaultJobType;
  }

  public Boolean getAddDataMigrationExecutionListener() {
    return addDataMigrationExecutionListener;
  }

  public void setAddDataMigrationExecutionListener(Boolean addDataMigrationExecutionListener) {
    this.addDataMigrationExecutionListener = addDataMigrationExecutionListener;
  }

  public String getDataMigrationExecutionListenerJobType() {
    return dataMigrationExecutionListenerJobType;
  }

  public void setDataMigrationExecutionListenerJobType(
      String dataMigrationExecutionListenerJobType) {
    this.dataMigrationExecutionListenerJobType = dataMigrationExecutionListenerJobType;
  }
}
